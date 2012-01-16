package cakehat.newdatabase;

import cakehat.Allocator;
import cakehat.database.ConnectionProvider;
import cakehat.newdatabase.DbPropertyValue.DbPropertyKey;
import cakehat.services.ServicesException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DatabaseImpl implements Database
{
    
    private ConnectionProvider _connProvider;

    /**
     * sets DB path to regular location
     */
    public DatabaseImpl() {
        this(Allocator.getPathServices().getDatabaseFile());
    }
    
    public DatabaseImpl(final File dbFile) {
        _connProvider = new ConnectionProvider() {

            public Connection createConnection() throws SQLException {
                try {
                    Class.forName("org.sqlite.JDBC");
                    SQLiteConfig config = new SQLiteConfig();
                    config.enforceForeignKeys(true);
                    
                    return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath(), config.toProperties());
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Could not open a connection to the DB.", e);
                }
            }

            public void closeConnection(Connection c) throws SQLException {
                if (c != null) {
                    c.close();
                }
            }
        };
        
        this.createDatabaseIfNecessary(dbFile);
    }
    
    private void createDatabaseIfNecessary(File databaseFile)
    {
        if(!databaseFile.exists())
        {
            try
            {
                Allocator.getFileSystemServices().makeDirectory(databaseFile.getParentFile());
            }
            catch(ServicesException ex)
            {
                System.err.println("cakehat is unable to create a database. Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }

            try
            {
                databaseFile.createNewFile();
                Allocator.getFileSystemServices().sanitize(databaseFile);

                this.resetDatabase();
            }
            catch(SQLException ex)
            {
                System.err.println("cakehat is unable to create a database. Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }
            catch(ServicesException ex)
            {
                System.err.println("cakehat is unable to create a database. Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }
            catch(IOException ex)
            {
                System.err.println("cakehat is unable to create a database. Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }

            /* TODO: re-implement using new objects and methods
            // If this is test mode, create some test students
            if(CakehatMain.isDeveloperMode())
            {
                try
                {
                    for(char letter = 'a'; letter <= 'z'; letter++)
                    {
                        this.addStudent(letter + "student", new Character(letter).toString().toUpperCase(), "Student");
                    }
                }
                catch(SQLException ex)
                {
                    System.err.println("Unable to add test students to database.");
                    ex.printStackTrace();
                }
            }
             */
        }
    }
    
    /**
     * opens a new connection to the DB
     */
    private Connection openConnection() throws SQLException {
        return _connProvider.createConnection();
    }
    
    private void closeConnection(Connection c) throws SQLException {
        _connProvider.closeConnection(c);
    }
    
    @Override
    public void addActionProperty(DbActionProperty property) throws SQLException
    {
        Connection conn = this.openConnection();
        PreparedStatement ps;
        Integer id = property.getId();
        if (property.getId() == null) {
            ps = conn.prepareStatement("INSERT INTO actionproperty (paid, key, value)"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, property.getPartAction().getId());
            ps.setString(2, property.getKey());
            ps.setString(3, property.getValue());
            ps.executeUpdate();

            id = ps.getGeneratedKeys().getInt(1);
        }
        else {
            ps = conn.prepareStatement("UPDATE actionproperty SET key = ?, value = ?"
                    + " WHERE apid == ?");
            ps.setString(1, property.getKey());
            ps.setString(2, property.getValue());
            ps.setInt(3, property.getId());
            ps.executeUpdate();
        }
        property.setId(id);
    }
    
    public <T> DbPropertyValue<T> getPropertyValue(DbPropertyKey<T> key) throws SQLException
    {
        Connection conn = this.openConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT cp.cpid AS cpid, cp.value AS value"
                    + " FROM courseproperties AS cp"
                    + " WHERE cp.key == ?");
        ps.setString(1, key.getName());
        ResultSet rs = ps.executeQuery();
        
        DbPropertyValue<T> propValue = null;
        if(rs.next())
        {
            int id = rs.getInt("cpid");
            T value = key.getValue(rs, "value");
            propValue = new DbPropertyValue<T>(id, value);
        }
        
        return propValue;
    }
    
    public <T> void setPropertyValue(DbPropertyKey<T> key, DbPropertyValue<T> value) throws SQLException
    {
        Connection conn = this.openConnection();
        PreparedStatement ps;
        Integer id = value.getId();
        if(id == null)
        {
            ps = conn.prepareStatement("INSERT INTO courseproperties ('key', 'value') VALUES (?, ?)");
            ps.setString(1, key.getName());
            key.setValue(ps, 2, value.getValue());
            ps.executeUpdate();

            id = ps.getGeneratedKeys().getInt(1);
            value.setId(id);
        }
        else
        {
            ps = conn.prepareStatement("UPDATE courseproperties value = ? WHERE cpid == ?");
            key.setValue(ps, 1, value.getValue());
            ps.setInt(2, value.getId());
            ps.executeUpdate();
        }
    }
    
    @Override
    public void resetDatabase() throws SQLException
    {
        Connection conn = this.openConnection();
        try {

            //DROP all tables in DB
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS actionproperty");

            //CREATE all DB tables
            conn.createStatement().executeUpdate("CREATE TABLE actionproperty ('apid' INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "paid INTEGER NOT NULL, "
                    + "key VARCHAR, "
                    + "value VARCHAR)");

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();

            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    public static void main(String[] argv) throws SQLException {
        DatabaseImpl db = new DatabaseImpl();
        db.resetDatabase();
    }
}
