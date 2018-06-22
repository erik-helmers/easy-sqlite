package easysqlite.annotations.handlers;

import easysqlite.annotations.core.ColumnHandler;
import easysqlite.annotations.core.TableHandler;
import easysqlite.annotations.declarations.Table;
import easysqlite.core.SQLColumn;
import easysqlite.core.Scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Get the required columns to insert
     * @param clss the clss to test
     * @return
     */
    @Override
    public List<SQLColumn> inserts_columns(Class clss) {
        return get_columns(clss).stream()
                .filter(x -> x.handler.required_on_insert())
                .collect(Collectors.toList());
    }
    //region LOGIC
    // ============================================== LOGIC ===============================================

    @Override
    public String getName() {
        return annotation.name();
    }

    //endregion

    @Override
    public List<SQLColumn> query_columns(Class clss) {
        return get_columns(clss).stream()
                .filter(x -> x.handler.required_on_query())
                .collect(Collectors.toList());
    }

    @Override
    protected List<SQLColumn> get_columns(Class clss) {
        List<SQLColumn> columns = new ArrayList<>();
        for (Field field: clss.getFields()) {
            Optional<Annotation> annot = Scanner.getESAnnotation(field);
            if (annot.isPresent()){
                ColumnHandler handler = Scanner.new_handler(annot.get(), ColumnHandler.class);
                columns.add(new SQLColumn(handler, field));
            }
        }
        return columns;
    }

    //TODO: raise error on multiple @IdColumn
    @Override
    public Optional<SQLColumn> id_column(Class clss) {
        return get_columns(clss).stream()
                .filter(x -> x.handler instanceof IdColumnHandler)
                .findFirst();
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
