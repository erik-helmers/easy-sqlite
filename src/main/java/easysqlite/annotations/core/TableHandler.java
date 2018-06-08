package easysqlite.annotations.core;

import easysqlite.SQLColumn;

import java.util.List;

public abstract class TableHandler extends AnnotationHandler {


    //region abstracts-methods
    // ======================================== ABSTRACTS-METHODS =========================================

    /**
     * Return the table name
     * @return name
     */
    abstract public String getName();

    /**
     * Return all the columns required for an insert
     * @param clss the clss to test
     * @return The list of Field
     */
    abstract public List<SQLColumn> insert_columns(Class clss);

    /**
     * Return all the columns required for a query
     * @param clss the columns
     * @return A list of names
     */
    abstract public List<SQLColumn> query_columns(Class clss);
    //endregion



    //endregion

}
