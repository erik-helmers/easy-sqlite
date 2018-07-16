package easysqlite.annotations.declarations;

import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.ESAnnotation;
import easysqlite.annotations.handlers.DefaultColumnHandler;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ESAnnotation(type=AnnotType.COLUMN, handler=DefaultColumnHandler.class)
public  @interface Column {
    String value() default "";
}
