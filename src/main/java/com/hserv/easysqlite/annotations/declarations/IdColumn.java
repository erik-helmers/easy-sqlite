package easysqlite.annotations.declarations;


import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.ESAnnotation;
import easysqlite.annotations.handlers.IdColumnHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ESAnnotation(type=AnnotType.COLUMN, handler = IdColumnHandler.class)
public @interface IdColumn {
    public String value() default "id";
    static AnnotType type = AnnotType.COLUMN;
}
