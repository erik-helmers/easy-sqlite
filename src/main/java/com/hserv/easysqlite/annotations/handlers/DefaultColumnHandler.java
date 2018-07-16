package easysqlite.annotations.handlers;

import easysqlite.annotations.core.ColumnHandler;
import easysqlite.annotations.declarations.Column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class DefaultColumnHandler extends ColumnHandler {

    @Override
    public Object get_value_on_insert(Field field, Object object) throws IllegalAccessException {
        return field.get(object);
    }
    private Wrapper annotation = new Wrapper();

    /**
     * Called to create an handler
     */
    @Override
    public void setAnnotation(Annotation annotation){
        this.annotation.setAnnotation(annotation);
    }

    /**
     * Children classes should call this to set their custom wrapper
     */
    protected void setWrapper(Wrapper wrapper){
        this.annotation = wrapper;
    }

    @Override
    public String getName(Field field) {
        String name = annotation.name();
        return name.equals("") ? field.getName() : name;
    }

    //endregion
    
    //region LOGIC
    // ============================================== LOGIC =============================================== 
    
    //region inheritance-stuff
    // ======================================== INHERITANCE-STUFF =========================================
    public static class Wrapper {
        /**
         * A private access to the annotation
         */
        private Column annotation;

        /**
         * This is only called by a DefaultColumnHandler
         */
        private void setAnnotation(Annotation annotation){
            this.annotation = (Column)annotation;
        }

        /**
         * The default field for a column annotation
         */
        public String name(){
            return annotation.value();
        }
    }

    /**
     * A wrapper for DefaultColumnHandler
     * It's main purpose is to give access to Annotation values
     * Even if it's derived by another class
     * in which case, the children's call setWrapper() with a custom
     * wrapper extending DefaultColumnHandler.Wrapper
     */


    //endregion
}
