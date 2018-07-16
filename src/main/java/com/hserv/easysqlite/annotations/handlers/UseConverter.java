package easysqlite.annotations.handlers;

import easysqlite.serialization.Converter;
import easysqlite.serialization.FieldConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UseConverter {
    Class<? extends FieldConverter> value();
}
