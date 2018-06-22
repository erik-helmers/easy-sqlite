package easysqlite.annotations.handlers;

import easysqlite.annotations.declarations.IdColumn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class IdColumnHandler extends DefaultColumnHandler {

    //region inheritance
    // =========================================== INHERITANCE ============================================

    public Wrapper annotation;

    public void setAnnotation(Annotation annotation){
        this.annotation = new Wrapper().setAnnotation(annotation);
        super.setWrapper(this.annotation);
    }

    public void setWrapper(Wrapper wrapper){
        this.annotation = wrapper;
        super.setWrapper(this.annotation);
    }

    @Override
    public String getName(Field field) {
        return super.getName(field);
    }

    //endregion

    //region LOGIC
    // ============================================== LOGIC ===============================================

    @Override
    public Object get_value_on_insert(Field field, Object object) throws IllegalAccessException {
        return super.get_value_on_insert(field, object);
    }

    @Override
    public boolean required_on_query() {
        return true;
    }

    @Override
    public boolean required_on_insert() {
        return false;
    }

    public static class Wrapper extends DefaultColumnHandler.Wrapper{

        IdColumn annotation;

        public Wrapper setAnnotation(Annotation annotation){
            this.annotation = (IdColumn)annotation;
            return this;
        }

        @Override
        public String name() {
            return annotation.value();
        }
    }


    //endregion
}
