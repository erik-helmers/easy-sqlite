package easysqlite.core;

import easysqlite.ColumnNotFoundError;
import easysqlite.MissingAnnotationException;
import easysqlite.annotations.SQLExecutor;
import easysqlite.annotations.core.TableHandler;
import easysqlite.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    //endregion

    //region init-utils
    // ============================================ INIT-UTILS ============================================

    public Repository(Class<T> clss, String url, String populate){
        validate_class(clss);
        connection = create_connection(url, populate);
    }

    public Repository(Class<T> clss, Connection connection){
        validate_class(clss);
        this.connection = connection;
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
            PreparedStatement stmt = conn.prepareStatement(populate);
            stmt.execute();
            System.out.println("======== Initialized table !");
        } catch (SQLException e) {
            throw new Error(e);
        }

        return conn;
    }


    //endregion


    //endregion

    //region saves
    // ============================================== SAVES ===============================================

    /**
     * Extract content from populate_file and passes it to create_connection(String, String)
     * @param path The path to the database
     * @param populate_file The populate file
     */

    public static Connection create_connection(String path, File populate_file){
        String populate;
        try{
            populate = new String(Files.readAllBytes(populate_file.toPath()));
            System.out.println(populate);
        } catch (IOException e){
            throw new Error(e);
        }

        return create_connection(path, populate);
    }

    /**
     * Check class validity
     * @param clss the class to test
     * @return validity
     */
     private void validate_class(Class<T> clss){

        this.clss = clss;

        //Get table annotation
        Annotation table_annotation = Scanner.getESAnnotation(clss).orElseThrow(MissingAnnotationException::new);
        this.table_handler =  Scanner.new_handler(table_annotation, TableHandler.class);

        this.table_name = table_handler.getName();

        this.inserts_columns = this.table_handler.inserts_columns(clss);
        this.queries_columns = this.table_handler.query_columns(clss);
        this.id_column = this.table_handler.id_column(clss);

        System.out.println("DETECTED AN ID COLUMN : " + id_column);

        this.convertion = new Serializer();
        this.convertion.scan_converters(clss);
    }

    //endregion

    //region save-utils
    // ============================================ SAVE-UTILS ============================================

    /**
     * Save an object to the database
     * @param obj the object to save
     * @throws SQLException
     */
    public void save(T obj) throws SQLException {
        String sql = forge_sql_insert_statement();

        SQLExecutor.PstmtSetter<T> x = (pstmt, object) -> {
            List<Object> values = extract_values(object);
            for (int i=0; i<values.size(); i++){
                pstmt.setString(i+1, serialize(values.get(i)));
            }
        };

        SQLExecutor executor = SQLExecutor.builder()
                                .sql(sql)
                                .connection(connection)
                                .pstmtSetter(x)
                                .build();

        executor.execute_insert(obj);
    }
    //endregion

    //region utils
    // ============================================== UTILS ===============================================

    /**
     * Optimized save for multiples objects using batch
     * @param objects list of objects to save
     * @throws SQLException redirect SQLException
     */

    public void save(List<T> objects) throws SQLException {

        String sql = forge_sql_insert_statement();

        SQLExecutor.PstmtSetter<T> x = (pstmt, object) -> {
            List<Object> values = extract_values(object);
            for (int i=0; i<values.size(); i++){
                pstmt.setString(i+1, serialize(values.get(i)));
            }
        };

        SQLExecutor executor = SQLExecutor.builder()
                                    .connection(connection)
                                    .pstmtSetter(x)
                                    .sql(sql)
                                    .build();

        executor.execute_batch_insert(objects);

    }

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
    }
    //endregion


    //region queries
    // ============================================= QUERIES ==============================================

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

    @Deprecated
    String serialize(Object object){
        return object.toString();
    }

    //endregion

    //region sql_forgers
    // =========================================== SQL_FORGERS ============================================

    /**
     * Search an object by field_name and return the found object
     * @param field_name the name of the field_name to search in
     * @param value the value to match
     * @return return a list of matched object
     * @throws Exception SQLException, Instantation object ...
     */

    public List<T> search(String field_name, String value) throws Exception{

        Field column_field = clss.getField(field_name);

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
        }
        return output;
    }

    private T result_set_to_object(ResultSet rs) throws Exception{

        T obj = get_no_argument_constructor().newInstance();
        for (SQLColumn column : inserts_columns){
            String result = rs.getString(column.getName());
            Object converted = convertion.deserialize(result, column.field.getType());
            column.field.set(obj, converted);
        }
        return obj;
    }
    //endregion

    //region lazy-loading
    // =========================================== LAZY-LOADING ===========================================

    private String forge_sql_query_statement(String column){

        String sql = "SELECT %s FROM %s WHERE %s=?";

        String output =  String.format(sql, formatted_insert_columns_name(), table_name,  column);
        System.out.println(output);
        return output;
    }

    /**
     * Create an initial statement of form
     *  "INSERT INTO {tablename}({list of columns} VALUES(?,?,?...?)"
     * @return sql statement ready for PreparedStatement
     */
    private String forge_sql_insert_statement(){


        String sql = "INSERT into %s(%s) VALUES(%s)";

        String values_places = inserts_columns.stream()
                .map(x -> "?")
                .collect(Collectors.joining(","));

        System.out.println(String.format(sql, table_name, formatted_insert_columns_name(), values_places));
        return String.format(sql, table_name, formatted_insert_columns_name(), values_places);

    }

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


    public Connection getConnection() {
        return connection;
    }
    //endregion


}
