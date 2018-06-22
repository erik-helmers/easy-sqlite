package easysqlite.annotations.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ExcludeHandler extends DefaultColumnHandler {

    @Override
    public boolean required_on_query() {
        return false;
    }

    @Override
    public boolean required_on_insert() {
        return false;
    }

    @Override
    public void setAnnotation(Annotation annotation) {
    }

    @Override
    protected void setWrapper(Wrapper wrapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName(Field field) {
        throw new UnsupportedOperationException("Did you check required_on_query|insert ?");
    }

    @Override
    public Object get_value_on_insert(Field field, Object object) throws IllegalAccessException {
        throw new UnsupportedOperationException("Did you check required_on_query|insert ?");

    }
}
