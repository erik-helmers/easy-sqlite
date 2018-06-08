package easysqlite.annotations.handlers;

import easysqlite.SQLColumn;
import easysqlite.annotations.declarations.AllFieldsTable;

import java.lang.annotation.Annotation;
import java.util.List;

public class AllFieldsTableHandler extends DefaultTableHandler {

    private Wrapper annotation;

    @Override
    public void setAnnotation(Annotation annotation) {
        this.annotation = new Wrapper().setAnnotation(annotation);
    }

    protected void setWrapper(Wrapper wrapper) {
        this.annotation = wrapper;
        super.setWrapper(wrapper);
    }

    @Override
    public List<SQLColumn> insert_columns(Class clss) {
        return super.insert_columns(clss);
    }

    @Override
    public List<SQLColumn> query_columns(Class clss) {
        return super.query_columns(clss);
    }

    public static class Wrapper extends DefaultTableHandler.Wrapper{
        AllFieldsTable annotation;
        private Wrapper setAnnotation(Annotation annotation){
            this.annotation = (AllFieldsTable) annotation;
            return this;
        }
        @Override
        public String name() {
            return annotation.value();
        }
    }
}
