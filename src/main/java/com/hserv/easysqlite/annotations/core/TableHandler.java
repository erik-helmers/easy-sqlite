package easysqlite.annotations.core;

import easysqlite.annotations.handlers.IdColumnHandler;
import easysqlite.core.SQLColumn;

import java.util.List;
import java.util.Optional;

public abstract class TableHandler extends AnnotationHandler {

    protected IdColumnHandler id_column;

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
    abstract public List<SQLColumn> inserts_columns(Class clss);

    /**
     * Return all the columns required for a query
     * @param clss the columns
     * @return A list of names
     */
    abstract public List<SQLColumn> query_columns(Class clss);

    /**
     * Search for an Id Column and return it if found
     * @param clss
     * @return
     */
    abstract public Optional<SQLColumn> id_column(Class clss);

    //endregion



    abstract protected List<SQLColumn> get_columns(Class clss);


    //endregion

}
