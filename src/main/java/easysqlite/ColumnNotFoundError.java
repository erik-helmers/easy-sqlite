package easysqlite;

import java.lang.reflect.Field;

public class ColumnNotFoundError extends Error {
    public ColumnNotFoundError(String name) {
        super("Column not found ! name is "+name);
    }
    public ColumnNotFoundError(Field field) {
        super("Column not found ! name is "+field.getName());
    }

}
