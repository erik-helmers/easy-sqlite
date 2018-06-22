package easysqlite.annotations.core;


import java.lang.reflect.Field;

public abstract class ColumnHandler extends AnnotationHandler {

    public abstract String getName(Field field);
    public abstract Object get_value_on_insert(Field field, Object object) throws IllegalAccessException;

    public boolean required_on_query(){return true;}
    public boolean required_on_insert(){return true;}
    public boolean update_on_post_insert(){return false;}

}
