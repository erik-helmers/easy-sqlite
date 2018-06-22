package easysqlite.annotations;

import lombok.Builder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLExecutor {

    Connection connection;
    String statement;
    PstmtSetter args_setter;
    @Builder
    public SQLExecutor(Connection connection, String sql, PstmtSetter pstmtSetter){
        this.connection = connection;
        this.statement = sql;
        this.args_setter = pstmtSetter;
    }

    /**
     * Execute an insert : shortcut for insert_and_get_ids with singletons list
     */
    public int execute_insert(Object object) {
        int affected;
        try (PreparedStatement pstmt = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)){
            args_setter.setArgs(pstmt, object);
            affected =  pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);

        } catch (SQLException e){
            throw new Error("failed insert", e);
        }

    }

    /**
     * Execute a large insert : @warning TOO SLOOOOW
     * @param objects objects to insert
     * @return generated row ids
     */

    @Deprecated
    public <T> List<Integer> insert_and_get_ids(List<T> objects){
        List<Integer> output = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            for (Object obj : objects) {
                args_setter.setArgs(pstmt, obj);

                //LOL leak of utils to add all int[]
                int changed = pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                while (rs.next()){
                    output.add(rs.getInt(1));
                }

            }

        } catch(SQLException e){
            throw new Error("failed to execute large insert", e);
        }
        return output;
    }

    public <T> void execute_batch_insert(List<T> objects){
        List<Integer> output = new ArrayList<>();
        int i=1;
        try (PreparedStatement pstmt = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)){
            for (Object obj: objects){
                args_setter.setArgs(pstmt, obj);
                pstmt.addBatch();
                if (i==1000 || i==objects.size()){
                    pstmt.executeBatch();
                }
                i++;
            }
        } catch (SQLException e){
            throw new Error("failed to execute large insert", e);
        }
    }

    public ResultSet execute_query(Object object){
        try (PreparedStatement pstmt = connection.prepareStatement(statement)) {
            args_setter.setArgs(pstmt, object);
            return pstmt.executeQuery();
        } catch (SQLException e){
            throw new Error("failed to execute statement", e);
        }
    }

    //TODO: warning : PstmtSetter and methods types are not fool proofs


    public static interface PstmtSetter<T>{
        public void setArgs(PreparedStatement pstmt, T object) throws SQLException;
    }








}
