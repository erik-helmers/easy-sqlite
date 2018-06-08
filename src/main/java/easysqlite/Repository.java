package easysqlite;

import easysqlite.annotations.core.TableHandler;
import easysqlite.annotations.declarations.Column;
import easysqlite.annotations.declarations.ConverterTarget;
import easysqlite.annotations.declarations.IdColumn;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Repository<T> {


    private Connection connection;
    private Class<T> clss;
    private String table_name;

    private TableHandler<Annotation> table_handler;
    private List<Field> columns;
    private List<String> columns_name;
    private String formatted_columns_name;

    private HashMap<Class, List<Deserializer>> deserializers = new HashMap<>();


    //region inits
    // ============================================== INITS ===============================================

    //region constructors
    // =========================================== CONSTRUCTORS ===========================================


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
        this.table_handler =  extract_handler(getESAnnotation(clss).orElseThrow(MissingAnnotationException::new), TableHandler.class);

        this.table_name = table_handler.getName();
        this.columns = this.table_handler.columns(clss);
        this.columns_name = this.table_handler.columns_name(this.columns);
        

        this.formatted_columns_name = format_columns_name();
        set_default_deserializer();
        scan_deserializers();
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


    //endregion


    //endregion

    //region saves
    // ============================================== SAVES ===============================================

    /**
     * Save an object to the database
     * @param obj the object to save
     * @throws SQLException
     */
    public void save(T obj) throws SQLException {


        System.out.println("========== TRYING TO SAVE : "+obj.toString());
        List<Object> values = get_columns_values(columns, obj);

        String sql = forge_sql_insert_statement();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            for (int i=0; i<values.size(); i++) {
                if (values.get(i) != null) {
                    pstmt.setString(i+1, values.get(i).toString()); //TODO: upgrade this shit -> serializer instead of toString()
                } else { pstmt.setString(i+1, null); }
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

        String sql = forge_sql_insert_statement();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int tour = 0;
            for (T obj : objects) {

                List<Object> values = get_columns_values(columns, obj);
                for (int i = 0; i < values.size(); i++)
                    pstmt.setString(i + 1, values.get(i).toString());
                pstmt.addBatch();

                tour ++; // Some connections refuse more than 1000 at once a
                if (tour%1000==0)
                    pstmt.executeBatch();
            }
        }
    }


    //region utils
    // ============================================== UTILS ===============================================


    /**
     * Get fields anotated with @Column
     * @return List of fields
     */
    @Deprecated
    private List<Field> get_columns_field(){
        List<Field> output = new ArrayList<>();
        for (Field field: clss.getFields()){
            if (field.isAnnotationPresent(Column.class)){
                output.add(field);
            }
        }
        return output;
    }

    @Deprecated
    private List<Field> get_all_fields(){
        // TODO: 05/06/18 add @Exclude ?
        return new ArrayList<>(Arrays.asList(clss.getFields()));
    }

    @Deprecated
    private List<String> get_columns_name_from_annot(){
        List<String> output = new ArrayList<>();
        for (Field field : columns){
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.value().equals(""))
                    output.add(field.getName());
                else output.add(column.value());
            }
            else {
                IdColumn column = field.getAnnotation(IdColumn.class);
                if (column.value().equals(""))
                    output.add(field.getName());
                else output.add(column.value());
            }
        }
        return output;
    }

    /**
     * Get fields anotated with @Column
     * @return List of fields
     */
    @Deprecated
    private List<Field> get_columns_field_incld_id(){
        List<Field> output = new ArrayList<>();
        for (Field field: clss.getFields()){
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(IdColumn.class)){
                output.add(field);
            }
        }
        return output;
    }

    @Deprecated
    private List<String> get_columns_name_from_field(){
        return columns.stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }
    /**
     * Get the values of fields
     * @param fields the fields to test
     * @param obj the object to use
     * @return list of object: values of the fields
     */
    @Deprecated
    private List<Object> get_columns_values(List<Field> fields, T obj){
        List<Object> output = new ArrayList<>();
        for (Field field : fields) {
            System.out.println("Testing field : "+field.getName());
            try {
                output.add(field.get(obj));
            } catch (IllegalAccessException e) {
                throw new Error(e);
            }
        }
        return output;
    }

    private String format_columns_name(){
        return columns_name.stream()
                .collect(Collectors.joining(","));
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

        String sql = forge_sql_query_statement(column);

        List<Field> cols = get_columns_field_incld_id();
        List<String> nms = get_columns_name_from_annot();

        List<T> output = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                output.add(result_set_to_object(rs, cols, nms));
        }
        return output;
    }


    private T result_set_to_object(ResultSet rs, List<Field> columns, List<String> columns_name) throws Exception{

        T obj = get_no_argument_constructor().newInstance();
        Iterator<String> names = columns_name.iterator();
        for (Field field : columns){
            String result = rs.getString(names.next());
            Object converted = convert(result, field.getType());
            field.set(obj, converted);
        }
        return obj;
    }

    //endregion

    //region sql_forgers
    // =========================================== SQL_FORGERS ============================================


    private String forge_sql_query_statement(String column){

        String sql = "SELECT %s FROM %s WHERE %s=?";

        String output =  String.format(sql, formatted_columns_name, table_name,  column);
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

        String values_places = columns.stream()
                .map(x -> "?")
                .collect(Collectors.joining(","));


        return String.format(sql, table_name, formatted_columns_name, values_places);

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

    //region type conversion
    // ========================================= TYPE CONVERSION ==========================================

    //region type-conversion-inits
    // ====================================== TYPE-CONVERSION-INITS =======================================


    private void set_default_deserializer(){
        register_deserializers(int.class, Integer::parseInt);
        register_deserializers(float.class, Float::parseFloat);
        register_deserializers(String.class, x->x);
    }


    void register_deserializers(Class support, Deserializer converter){
        if (!deserializers.containsKey(support)){
            deserializers.put(support, new ArrayList<>());
        }
        deserializers.get(support).add(converter);
    }


    private void scan_deserializers(){
        for (Field field: clss.getFields()){
            if (field.isAnnotationPresent(ConverterTarget.class)) {

                Class[] supported = (field.getAnnotation(ConverterTarget.class)).value();

                for (Class support: supported){
                    try { register_deserializers(support, (Deserializer) field.get(null)); }
                    catch (IllegalAccessException e){
                        throw new Error("Deserializer field should be static should be static !");
                    }
                }

            }
        }
    }

    //endregion

    /**
     * Convert a String object to specified object type
     * @param from the original String object
     * @param target the target class
     * @return the converted object if successful
     * @throws ClassCastException
     */
    private Object convert(String from, Class target) throws ClassCastException{

        // First fallback : use scanned deserializers
        if (deserializers.containsKey(target)){
            List<Deserializer> adapted_converters = deserializers.get(target);
            for (int i=adapted_converters.size()-1; i>=0; i--)
            {
                Object x = adapted_converters.get(i).convert(from);
                if (x != null) {
                    return x;
                }
            }
        }

        // Second fallback : use constructor with signature (String);
        try {
            Object obj = target.getConstructor(String.class).newInstance(from);
            System.out.println("Used constructor new "+target.getName()+"(String)");
            return obj;
        } catch (NoSuchMethodException | IllegalAccessException ignored){}
        catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }

        throw new ClassCastException("No converter found");

    }




    //endregion


}
