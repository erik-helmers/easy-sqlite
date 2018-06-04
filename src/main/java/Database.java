import annotations.Column;
import annotations.Converter;
import annotations.Table;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Database<T> {


    private Connection connection;
    private Class<T> clss;
    private Table table;
    private List<Field> columns;
    private HashMap<Class, List<ConverterIntrf>> converters = new HashMap<>();


    //region inits
    // ============================================== INITS ===============================================


    public Database(Class<T> clss, String url, File populate_file){
        validate_class(clss);
        init_connection(url, populate_file);
    }

    public Database(Class<T> clss, String url, String populate){
        validate_class(clss);
        init_connection(url, populate);
    }
    public Database(Class<T> clss, Connection connection){
        validate_class(clss);
        this.connection = connection;
    }

    /**
     * Check class validity
     * @param clss the class to test
     * @return validity
     */
    private void validate_class(Class<T> clss){
        if (!clss.isAnnotationPresent(Table.class)){
            throw new IllegalArgumentException("Class should be annotated with @Table");
        }
        this.clss = clss;
        this.table = clss.getAnnotation(Table.class);
        this.columns = get_columns(clss);

        set_default_converters();
        scan_converters();

    }

    /**
     * Create a connection to path, and execute populate statement
     * @param path The path to the database
     * @param populate The populate query
     */
    private void init_connection(String path, String populate){

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

        this.connection = conn;
    }

    /**
     * Extract content from populate_file and passes it to init_connection(String, String)
     * @param path The path to the database
     * @param populate_file The populate file
     */

    private void init_connection(String path, File populate_file){
        String populate;
        try{
            populate = new String(Files.readAllBytes(populate_file.toPath()));
            System.out.println(populate);
        } catch (IOException e){
            throw new Error(e);
        }

        init_connection(path, populate);
    }

    //endregion

    //region saves
    // ============================================== SAVES ===============================================

    /**
     * Save an object to the database
     * @param obj the object to save
     * @throws SQLException
     */
    public void save(T obj) throws SQLException {

        List<Object> values = get_columns_values(columns, obj);

        String sql = forge_sql_insert_statement(table, columns);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            for (int i=0; i<values.size(); i++) {
                pstmt.setString(i+1, values.get(i).toString());
            }
            pstmt.execute();
        }
    }


    /**
     * Optimized save for multiples objects using batch
     * @param objects list of objects to save
     * @throws SQLException redirect SQLException
     */

    public void save(List<T> objects) throws SQLException {

        String sql = forge_sql_insert_statement(table, columns);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int tour = 0;
            for (T obj : objects) {

                List<Object> values = get_columns_values(columns, obj);
                for (int i = 0; i < values.size(); i++)
                    pstmt.setString(i + 1, values.get(i).toString());
                pstmt.addBatch();

                tour ++; // Some connections refuse more than 1000 at once
                if (tour%1000==0)
                    pstmt.executeBatch();
            }
        }
    }

    //endregion

    //region saves-utils
    // =========================================== SAVES-UTILS ============================================

    /**
     * Get fields anotated with @Column
     * @param clss the class to test
     * @return List of fields
     */
    private List<Field> get_columns(Class clss){
        List<Field> output = new ArrayList<>();
        for (Field field: clss.getFields()){
            if (field.isAnnotationPresent(Column.class)){
                output.add(field);
            }
        }
        return output;
    }


    /**
     * Get the values of fields
     * @param fields the fields to test
     * @param obj the object to use
     * @return list of object: values of the fields
     */
    List<Object> get_columns_values(List<Field> fields, T obj){
        List<Object> output = new ArrayList<>();
        for (Field field : fields) {
            try {
                output.add(field.get(obj));
            } catch (IllegalAccessException e) {
                throw new Error("Fields should be accessible");
            }
        }
        return output;
    }

    /**
     * Create an initial statement of form
     *  "INSERT INTO {tablename}({list of columns} VALUES(?,?,?...?)"
     * @param table annotation
     * @param columns fields annotated by column
     * @return sql statement ready for PreparedStatement
     */
    private String forge_sql_insert_statement(Table table, List<Field> columns){


        String sql = "INSERT into %s(%s) VALUES(%s)";

        String columns_name = columns.stream()
                .map(x -> x.getAnnotation(Column.class))
                .map(Column::name)
                .collect(Collectors.joining(","));

        String values_places = columns.stream()
                .map(x -> "?")
                .collect(Collectors.joining(","));


        return String.format(sql, table.name(), columns_name, values_places);

    }
    //endregion


    //region queries
    // ============================================= QUERIES ==============================================

    /**
     * Search an object by column and return the found object
     * @param column the name of the column to search in
     * @param value the value to match
     * @return return a list of matched object
     * @throws Exception SQLException, Instantation object ...
     */

    public List<T> search(String column, String value) throws Exception{

        String sql = forge_sql_query_statement(table, columns, column);

        List<T> output = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                output.add(result_set_to_object(rs, columns));
        }
        return output;
    }


    private String forge_sql_query_statement(Table table, List<Field> columns, String column){

        String sql = "SELECT %s FROM %s WHERE %s=?";

        String columns_name = columns.stream()
                .map(x -> x.getAnnotation(Column.class))
                .map(Column::name)
                .collect(Collectors.joining(","));

        String output =  String.format(sql, columns_name, table.name(),  column);
        System.out.println(output);
        return output;
    }

    private T result_set_to_object(ResultSet rs, List<Field> columns) throws Exception{

        T obj = get_no_argument_constructor().newInstance();
        for (Field field : columns){
            String result = rs.getString(field.getAnnotation(Column.class).name());
            Object converted = convert(result, field.getType());
            field.set(obj, converted);
        }
        return obj;
    }

    //endregion

    //region utils
    // ============================================== UTILS ===============================================

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

    //region type conversion
    // ========================================= TYPE CONVERSION ========================================== 


    private void set_default_converters(){
        register_converter(int.class, Integer::parseInt);
        register_converter(float.class, Float::parseFloat);
        register_converter(String.class, x->x);
    }


    void register_converter(Class support, ConverterIntrf converter){
        if (!converters.containsKey(support)){
            converters.put(support, new ArrayList<>());
        }
        converters.get(support).add(converter);
    }

    /**
     * Convert a String object to specified object type
     * @param from the original String object
     * @param target the target class
     * @return the converted object if successful
     * @throws ClassCastException
     */
    public  Object convert(String from, Class target) throws ClassCastException{
        // First try : use constructor with signature (String);
        try {
            return target.getConstructor(String.class).newInstance(from);
        } catch (NoSuchMethodException | IllegalAccessException ignored){}
        catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }

        // Second fallback : use scanned converters
        List<ConverterIntrf> adapted_converters = converters.get(target);
        for (int i=adapted_converters.size()-1; i>=0; i--)
        {
            Object x = adapted_converters.get(i).convert(from);
            if (x != null) {
                return x;
            }
        }
        throw new ClassCastException("No converter found");
    }

    void scan_converters(){
        for (Field field: clss.getFields()){
            if (field.isAnnotationPresent(Converter.class)) {
                Class support = (field.getAnnotation(Converter.class)).target();
                try { register_converter(support, (ConverterIntrf) field.get(null)); }
                catch (IllegalAccessException e){
                    throw new Error("ConverterIntrf should be static !");
                }
            }
        }
    }


    //endregion






}
