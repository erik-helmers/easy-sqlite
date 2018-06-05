package easysqlite.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public  @interface Column {
    String value() default "";
}
