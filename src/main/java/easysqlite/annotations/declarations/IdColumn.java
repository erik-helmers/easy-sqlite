package easysqlite.annotations.declarations;


import easysqlite.annotations.core.AnnotType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IdColumn {
    public String value() default "id";
    static AnnotType type = AnnotType.COLUMN;
}
