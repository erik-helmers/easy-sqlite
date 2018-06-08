package easysqlite;

import easysqlite.annotations.core.ColumnHandler;

import java.lang.reflect.Field;

public class SQLColumn {
    public final ColumnHandler handler;
    public final Field field;

    public SQLColumn(ColumnHandler handler, Field field) {
        this.handler = handler;
        this.field = field;
    }
}
