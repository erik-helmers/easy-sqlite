package easysqlite.annotations.declarations;

import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.ESAnnotation;
import easysqlite.annotations.handlers.AllFieldsTableHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ESAnnotation(type=AnnotType.TABLE, handler=AllFieldsTableHandler.class)
public @interface AllFieldsTable {
    public String value();
}
