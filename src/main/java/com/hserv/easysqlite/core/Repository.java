package easysqlite.core;

import easysqlite.ColumnNotFoundError;
import easysqlite.MissingAnnotationException;
import easysqlite.annotations.core.TableHandler;
import easysqlite.serialization.Serializer;
import logger.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Repository<T> {


    private Connection connection;
    private Class<T> clss;
    private String table_name;

    private TableHandler table_handler;
    private Optional<SQLColumn> id_column;
    private List<SQLColumn> inserts_columns;
    private List<SQLColumn> queries_columns;


    private Serializer convertion;


    //region inits
    // ============================================== INITS ===============================================

    //region constructors
    // =========================================== CONSTRUCTORS ===========================================
    private String __formatted_insert_columns_name = null;
    private String __formated_queries_columns_name = null;
     public Repository(Class<T> clss, String url, File populate_file){
        validate_class(clss);
        connection = create_connection(url, populate_file);
    }

    public Repository(Class<T> clss, String url, String populate){
        validate_class(clss);
        connection = create_connection(url, populate);
    }

    public Repository(Class<T> clss, Connection connection){
        validate_class(clss);
        this.connection = connection;
    }


    //endregion

    //region init-utils
    // ============================================ INIT-UTILS ============================================



    /**
     * Check class validity
     * @param clss the class to test
     * @return validity
     */
    private void validate_class(Class<T> clss){

        this.clss = clss;

        //Get table annotation
        Annotation table_annotation = Scanner.getTableAnnotation(clss).orElseThrow(MissingAnnotationException::new);
        this.table_handler =  Scanner.new_handler(table_annotation);

        this.table_name = table_handler.getName();

        this.inserts_columns = this.table_handler.inserts_columns(clss);
        Log.debug_value("insert columns",inserts_columns);
        this.queries_columns = this.table_handler.query_columns(clss);
        Log.debug_value("query columns",queries_columns);
        this.id_column = this.table_handler.id_column(clss);


        this.convertion = new Serializer();
        this.convertion.scan_converters(clss);
    }


    /**
     * Create a connection to path, and execute populate statement
     * @param path The path to the database
     * @param populate The populate query
     */
    public static Connection create_connection(String path, String populate){

        String url = "jdbc:sqlite:"+path;
        Connection conn = null;


        try {
            conn = DriverManager.getConnection(url);
            for (String statement : populate.split(";")){
                if (!StringUtils.isBlank(statement)){
                    Log.debug_value("statement", statement);
                    PreparedStatement stmt = conn.prepareStatement(statement);
                    stmt.execute();
                }

            }

        } catch (SQLException e) {
            throw new Error("Error while creating the tables", e);
        }

        return conn;
    }

    /**
     * Extract content from populate_file and passes it to create_connection(String, String)
     * @param path The path to the database
     * @param populate_file The populate file
     */

    public static Connection create_connection(String path, File populate_file){
        String populate;
        try{
            populate = new String(Files.readAllBytes(populate_file.toPath()));
        } catch (IOException e){
            throw new Error(e);
        }

        return create_connection(path, populate);
    }


    //endregion


    //endregion

    //region saves
    // ============================================== SAVES ===============================================

    /**
     * Create save executor
     */
    private SQLExecutor save_executor(){
        String sql = forge_sql_insert_statement();

        SQLExecutor.PstmtSetter<T> x = (pstmt, object) -> {
            List<Object> values = extract_values(object);
            for (int i=0; i<values.size(); i++){
                pstmt.setString(i+1, convertion.serialize(values.get(i)));
            }
        };

        return SQLExecutor.builder()
                .sql(sql)
                .connection(connection)
                .pstmtSetter(x)
                .build();
    }
    /**
     * Save an object to the database
     * @param obj the object to save
     * @throws SQLException
     */
    public Object save(T obj) {
        int result = save_executor().execute_insert(obj);
        return id_column.map(sqlColumn -> convertion.deserialize(String.valueOf(result), sqlColumn, clss)).orElse(null);
    }

    /**
     * Optimized save for multiples objects using batch insert
     * @param objects list of objects to save
     * @throws SQLException redirect SQLException
     */

    public void save(List<T> objects) {
        save_executor().execute_batch_insert(objects);
    }




    //region utils
    // ============================================== UTILS ===============================================


    /**
     * Extract  the objects to save
     */

    List<Object> extract_values(T obj){

        List<Object> output = new ArrayList<>();
        try{
            for (SQLColumn column : inserts_columns){
                output.add(column.handler.get_value_on_insert(column.field, obj));
            }
        } catch (IllegalAccessException e){
            throw new Error("could not access a field value, check field AND class is public", e);
        }
        return output;
    }


    /**
    public void update_objects(List<T> objects, List<Integer> ids){
        if (!id_column.isPresent()) return;
        if(objects.size() != ids.size()) {
            System.out.println(ids);
            throw new Error(String.format("Eeeh ! %d != %d", objects.size(), ids.size()));
        }
        try {
            for (int i = 0; i < objects.size(); i++) {
                id_column.get().field.setInt(objects.get(i), ids.get(i));
            }
        } catch (IllegalAccessException e){
            System.out.println("warning ! couldn't set id");
        }
    }**/
    //endregion

    //endregion

    
    //region delete
    // ============================================== DELETE ============================================== 

    public void delete(String field_name, String value){

        Field column_field = getField(field_name);

        SQLColumn column = queries_columns
                .stream()
                .filter(x -> x.field.equals(column_field))
                .findFirst()
                .orElseThrow(() -> new ColumnNotFoundError(column_field));

        SQLExecutor.builder()
                .connection(connection)
                .pstmtSetter((pstmt, object) -> { pstmt.setString(1, value); })
                .sql(forge_sql_delete_statement(field_name))
                .build()
                .execute();




    }

    //endregion
    //region queries
    // ============================================= QUERIES ==============================================

    /**
     * Search an object by field_name and return the found object
     * @param field_name the name of the field_name to search in
     * @param value the value to match
     * @return return a list of matched object
     */

    public List<T> search(String field_name, String value) {

        Field column_field = getField(field_name);

        SQLColumn column = queries_columns
                .stream()
                .filter(x -> x.field.equals(column_field))
                .findFirst()
                .orElseThrow(() -> new ColumnNotFoundError(column_field));

        String sql = forge_sql_query_statement(column.getName());

        List<T> output = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                output.add(result_set_to_object(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private T result_set_to_object(ResultSet rs) throws SQLException {

        T obj = null;
        try {
            obj = get_no_argument_constructor().newInstance();
        } catch (InstantiationException e) {
            throw new Error("Something went wrong when initializing object !", e);
        } catch (IllegalAccessException e) {
            throw new Error("The constructor should be public !", e);
        } catch (InvocationTargetException e) {
            throw new Error("idk what this mean lol", e);
        }

        for (SQLColumn column : queries_columns){
            String result = rs.getString(column.getName());
            Log.debug_call("RS to Obj says", "I WANT A " + column.field.getType().getName());
            Object converted = convertion.deserialize(result, column, clss);
            Log.debug_value("converted", converted);
            try {
                column.field.set(obj, converted);
            } catch (IllegalAccessException e) {
                throw new Error("Couldn't access field !", e);
            }
        }
        return obj;
    }

    //endregion

    //region sql_forgers
    // =========================================== SQL_FORGERS ============================================


    private String forge_sql_query_statement(String column){

        String sql = "SELECT %s FROM %s WHERE %s=?";

        String output =  String.format(sql, formated_queries_columns_name(), table_name,  column);
        Log.debug_value("query statement", output);
        return output;
    }

    /**
     * Create an initial statement of form
     *  "INSERT INTO {tablename}({list of columns} VALUES(?,?,?...?)"
     * @return sql statement ready for PreparedStatement
     */
    private String forge_sql_insert_statement(){

        if (inserts_columns.isEmpty()) return String.format("INSERT into %s DEFAULT VALUES;", table_name);

        String sql = "INSERT into %s(%s) VALUES(%s)";

        String values_places = inserts_columns.stream()
                .map(x -> "?")
                .collect(Collectors.joining(","));

        Log.debug_value("insert statement", String.format(sql, table_name, formatted_insert_columns_name(), values_places));
        return String.format(sql, table_name, formatted_insert_columns_name(), values_places);

    }

    private String forge_sql_delete_statement(String column){
        String sql = "DELETE FROM %s WHERE %s=?";
        return  String.format(sql, table_name, column);
    }

    //endregion

    //region lazy-loading
    // =========================================== LAZY-LOADING ===========================================


    String formatted_insert_columns_name(){
        if (__formatted_insert_columns_name==null)
            __formatted_insert_columns_name = inserts_columns.stream()
                                                .map(SQLColumn::getName)
                                                .collect(Collectors.joining(","));
        return __formatted_insert_columns_name;
    }

    String formated_queries_columns_name(){
        if (__formated_queries_columns_name==null)
            __formated_queries_columns_name = queries_columns.stream()
                                                .map(SQLColumn::getName)
                                                .collect(Collectors.joining(","));
        return __formated_queries_columns_name;

    }
    //endregion

    //region generic-utils
    // ========================================== GENERIC-UTILS ===========================================


    private Constructor<T> get_no_argument_constructor()  {
        try { return clss.getConstructor(); }
        catch (NoSuchMethodException e){
            throw new Error("The class should have a default constructor!");
        }
    }

    private Field getField(String field_name){
        try {
            return clss.getField(field_name);
        } catch (NoSuchFieldException e) {
            throw new Error("field not found", e);
        }

    }

    public Connection getConnection() {
        return connection;
    }
    //endregion


}
