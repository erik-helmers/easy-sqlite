package easysqlite.core;

import easysqlite.annotations.core.AnnotType;
import easysqlite.annotations.core.AnnotationHandler;
import easysqlite.annotations.core.ESAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Predicate;

public class Scanner {


    //region presence
    // ============================================= PRESENCE =============================================

    /**
     * check ESAnnotation presence
     * @param clss to test
     * @return presence
     */
    public static boolean has_ESAnnotation(Class clss){
        return getTableAnnotation(clss).isPresent();
    }
    public static boolean has_ESAnnotation(Field field){
        return getColumnAnnotation(field).isPresent();
    }


    //endregion

    //region extractors
    // ============================================ EXTRACTORS ============================================

    /**
     * Create new handler from ESAnnotation with the included handler
     * @param annotation an ESAnnotation
     * @param <X> the expected returned handler type
     * @return a new handler of type @link handler_target_type
     */
    public static <X extends AnnotationHandler> X new_handler(Annotation annotation){
        Class<?> handler_class = annotation.annotationType().getAnnotation(ESAnnotation.class).handler();
        try {
            X obj = (X)handler_class.getConstructor().newInstance();
            obj.setAnnotation(annotation);
            return obj;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new Error(e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new Error(String.format("This class %s should have a public default constructor !", handler_class.getName()), e);
        }
    }



    /**
     * Get ESAnnotation
     * @param clss
     * @return optional ESAnnotation
     */


    //Be careful with this : use annotationType() and not getClass() !
    public static Optional<Annotation> getTableAnnotation(Class clss){
        return getAnnotation(clss, annotation ->
                annotation.annotationType().isAnnotationPresent(ESAnnotation.class)
                && annotation.annotationType().getAnnotation(ESAnnotation.class).type().equals(AnnotType.TABLE));
    }

    public static Optional<Annotation> getColumnAnnotation(Field field){
        return getAnnotation(field, annotation ->
                annotation.annotationType().isAnnotationPresent(ESAnnotation.class)
                && annotation.annotationType().getAnnotation(ESAnnotation.class).type().equals(AnnotType.COLUMN));
    }

    /**
     * Search for a table type annotation
     */


    /**
     * Search for annotation validating the predicate
     */

    public static Optional<Annotation> getAnnotation(Annotation[] annotations, Predicate<Annotation> matcher){
        for (Annotation annot : annotations){
            if (matcher.test(annot))
                return Optional.of(annot);
        }
        return Optional.empty();
    }

    /**
     * Search for an annotation validating the predicate
     * @param clss the class to test
     * @param matcher the predicate
     * @return wheter a matching annotation was found
     */
    public static Optional<Annotation> getAnnotation(Class clss, Predicate<Annotation> matcher){
        return getAnnotation(clss.getAnnotations(), matcher);
    }

    /**
     * Search for an annotation validating the predicate
     * @param field the field to test
     * @param matcher the predicate
     * @return an optional found matcher
     */
    public static Optional<Annotation> getAnnotation(Field field, Predicate<Annotation> matcher){
        return getAnnotation(field.getAnnotations(), matcher);
    }

    //endregion
}
