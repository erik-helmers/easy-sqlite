package easysqlite.annotations.core;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface ESAnnotation {
    AnnotType type();
    Class<?> handler();
}
