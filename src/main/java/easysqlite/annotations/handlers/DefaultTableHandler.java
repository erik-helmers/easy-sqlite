package easysqlite.annotations.handlers;

import easysqlite.SQLColumn;
import easysqlite.Scanner;
import easysqlite.annotations.core.ColumnHandler;
import easysqlite.annotations.core.TableHandler;
import easysqlite.annotations.declarations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultTableHandler extends TableHandler {

    //region inheritance-stuff
    // ======================================== INHERITANCE-STUFF ========================================= 

    private Wrapper annotation;

    @Override
    public void setAnnotation(Annotation annotation){
        this.annotation = new Wrapper().setAnnotation(annotation);
    }

    protected void setWrapper(Wrapper wrapper){
        this.annotation = wrapper;
    }

    //region LOGIC
    // ============================================== LOGIC ===============================================
    @Override
    public String getName() {
        return annotation.name();
    }

    //endregion

    /**
     * Get the required columns to insert
     * @param clss the clss to test
     * @return
     */
    @Override
    public List<SQLColumn> insert_columns(Class clss) {
        List<SQLColumn> columns = new ArrayList<>();
        for (Field field: clss.getFields()) {
            Optional<Annotation> annot = Scanner.getESAnnotation(field);
            if (annot.isPresent()){
                ColumnHandler handler = Scanner.new_handler(annot.get(), ColumnHandler.class);
                if (handler.required_on_insert()) {
                    columns.add(new SQLColumn(handler, field));
                }
            }
        }
        return columns;
    }

    @Override
    public List<SQLColumn> query_columns(Class clss) {
        List<SQLColumn> columns = new ArrayList<>();
        for (Field field: clss.getFields()) {
            Optional<Annotation> annot = Scanner.getESAnnotation(field);
            if (annot.isPresent()){
                ColumnHandler handler = Scanner.new_handler(annot.get(), ColumnHandler.class);
                if (handler.required_on_query()) {
                    columns.add(new SQLColumn(handler, field));
                }
            }
        }
        return columns;
    }

    public static class Wrapper {
        private Table annotation;
        private Wrapper setAnnotation(Annotation annotation){
            this.annotation = (Table) annotation;
            return this;
        }
        public String name(){return annotation.value();}
    }

    //endregion


}
