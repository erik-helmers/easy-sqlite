package easysqlite.annotations.declarations;


import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.ESAnnotation;
import easysqlite.annotations.handlers.ExcludeHandler;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
@ESAnnotation(type=AnnotType.COLUMN, handler = ExcludeHandler.class)
public @interface Exclude {
}
