package cakehat.newdatabase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionProviders encapsulate the creation of DB connections.
 * Used by the DBWrapper to allow it to work with both sqlite file DB and the
 * sqlite in memory test DB.
 *
 * @author aunger
 */
public interface ConnectionProvider {

    /**
     * Opens a new connection to the same DB as the last connection.
     *
     * @return a connection to the DB
     * @throws SQLException
     */
    public Connection createConnection() throws SQLException;

    /**
     * Closes a connection that was opened by the createConnection method.
     *
     * @param connection - the connection to close
     * @throws SQLException
     */
    public void closeConnection(Connection connection) throws SQLException;
}
