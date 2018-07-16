package easysqlite.annotations.handlers;

import easysqlite.annotations.core.ColumnHandler;
import easysqlite.annotations.declarations.AllFieldsTable;
import easysqlite.annotations.declarations.Column;
import easysqlite.core.SQLColumn;
import easysqlite.core.Scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Every field not annotated fields is considered as marked with @Column
 */
public class AllFieldsTableHandler extends DefaultTableHandler {

        private Wrapper annotation;;

    //region inheritance
    // =========================================== INHERITANCE ============================================

public AllFieldsTableHandler(){}

    @Override
    public void setAnnotation(Annotation annotation) {
        this.annotation = new Wrapper().setAnnotation(annotation);
        super.setWrapper(this.annotation);
    }

    /**
     * Generate a new @Column instance
     * @return a column annotation instance
     */
    Column newColumn(){
        return new Column(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return Column.class;
            }

            @Override
            public String value() {
                return "";
            }
        };
    }
    protected void setWrapper(Wrapper wrapper) {
        this.annotation = wrapper;
        super.setWrapper(wrapper);
    }

    //endregion

    /**
     * Find all default fields and add them as columns
     * @param clss the clss to test
     * @return List of not marked field as they were @Column annotated
     */

    List<SQLColumn> default_field_as_column(Class clss){

        List<SQLColumn> output = new ArrayList<>();
        ColumnHandler handler = Scanner.new_handler(newColumn());

        for (Field field: clss.getFields()){
            if (!Scanner.has_ESAnnotation(field))
                output.add(new SQLColumn(handler, field));
        }

        return output;
    }

    @Override
    protected List<SQLColumn> get_columns(Class clss) {
        List<SQLColumn> output = super.get_columns(clss);
        output.addAll(default_field_as_column(clss));
        return output;
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
