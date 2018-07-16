package easysqlite.annotations.declarations;

import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.ESAnnotation;
import easysqlite.annotations.handlers.DefaultTableHandler;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ESAnnotation(type=AnnotType.TABLE, handler = DefaultTableHandler.class)
public @interface Table {

    String value();


}
