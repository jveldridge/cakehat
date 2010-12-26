package gradesystem.database;

import gradesystem.views.shared.ErrorView;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates connections to a in memory SQLite test DB. Each connection will connect
 * to the same DB so long as it is in the same VM. Once the VM closes the DB is
 * removed. Multiple instances of this class will connect to the same DB.
 *
 * @author aunger
 */
public class InMemoryConnectionProvider implements ConnectionProvider {

    private Connection _conn = null;

    public Connection createConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");

            // if no connection has been made then create a connection.
            if (_conn == null) {
                _conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            }
        } catch (ClassNotFoundException e) {
            new ErrorView(e, "Could not open a connection to an in memory test DB.");
        }
        return _conn;
    }

    public void closeConnection(Connection c) throws SQLException {
        // just commit the changes. if the connection is closed then the test DB will be lost.
        _conn.commit();
    }
}
