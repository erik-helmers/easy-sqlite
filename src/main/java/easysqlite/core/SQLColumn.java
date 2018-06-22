package easysqlite.core;

import easysqlite.annotations.core.ColumnHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class SQLColumn {
    public final ColumnHandler handler;
    public final Field field;

    public SQLColumn(ColumnHandler handler, Field field) {
        this.handler = handler;
        this.field = field;
    }

    public SQLColumn(Field field, Annotation annotation){
        this.handler = Scanner.new_handler(annotation, ColumnHandler.class);
        this.field = field;
    }

    public String getName(){
        return handler.getName(field);
    }

}
