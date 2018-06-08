package easysqlite;

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
    public static boolean is_ESAnnotation_present(Class clss){
        return getESAnnotation(clss).isPresent();
    }


    //endregion

    //region extractors
    // ============================================ EXTRACTORS ============================================

    /**
     * Create new handler from ESAnnotation
     * @param annotation an ESAnnotation
     * @param handler_target_type the expected returned handler class
     * @param <X> the expected returned handler type
     * @return a new handler of type @link handler_target_type
     */
    public static <X> X new_handler(Annotation annotation, Class<X> handler_target_type){
        Class<?> handler_class = annotation.annotationType().getAnnotation(ESAnnotation.class).handler();
        try {
            return (X)handler_class.getConstructor(Annotation.class).newInstance(annotation);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new Error(e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new Error("The handler should have a public default constructor !");
        }
    }



    /**
     * Get ESAnnotation
     * @param clss
     * @return optional ESAnnotation
     */

    public static Optional<Annotation> getESAnnotation(Class clss){
        return getAnnotation(clss, x -> x.getClass().isAnnotationPresent(ESAnnotation.class));
    }

    public static Optional<Annotation> getESAnnotation(Field field){
        return getAnnotation(field, x -> x.getClass().isAnnotationPresent(ESAnnotation.class));
    }

    /**
     * Search for annotation validating the predicate
     */

    public static Optional<Annotation> getAnnotation(Annotation[] annotations, Predicate<Annotation> matcher){
        for (Annotation annot : annotations){
            if (matcher.test(annot)) return Optional.of(annot);
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
