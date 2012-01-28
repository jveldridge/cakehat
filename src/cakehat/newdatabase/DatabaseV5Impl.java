package cakehat.newdatabase;

import cakehat.Allocator;
import cakehat.database.ConnectionProvider;
import cakehat.newdatabase.DbPropertyValue.DbPropertyKey;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public class DatabaseV5Impl implements DatabaseV5
{
    
    private ConnectionProvider _connProvider;

    /**
     * sets DB path to regular location
     */
    public DatabaseV5Impl() {
        this(Allocator.getPathServices().getDatabaseFile());
    }
    
    public DatabaseV5Impl(final File dbFile) {
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
    public <T> DbPropertyValue<T> getPropertyValue(DbPropertyKey<T> key) throws SQLException {
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT cp.cpid AS cpid, cp.value AS value"
                    + " FROM courseproperties AS cp"
                    + " WHERE cp.key == ?");
            ps.setString(1, key.getName());
            ResultSet rs = ps.executeQuery();

            DbPropertyValue<T> propValue = null;
            if (rs.next()) {
                int id = rs.getInt("cpid");
                T value = key.getValue(rs, "value");
                propValue = new DbPropertyValue<T>(id, value);
            }
            
            return propValue;
        }
        finally {
           this.closeConnection(conn);             
        }
    }
    
    @Override
    public <T> void putPropertyValue(DbPropertyKey<T> key, DbPropertyValue<T> value) throws SQLException {
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps;
            Integer id = value.getId();
            if (id == null) {
                ps = conn.prepareStatement("INSERT INTO courseproperties ('key', 'value') VALUES (?, ?)");
                ps.setString(1, key.getName());
                key.setValue(ps, 2, value.getValue());
                ps.executeUpdate();

                id = ps.getGeneratedKeys().getInt(1);
                value.setId(id);
            }
            else {
                ps = conn.prepareStatement("UPDATE courseproperties value = ? WHERE cpid == ?");
                key.setValue(ps, 1, value.getValue());
                ps.setInt(2, value.getId());
                ps.executeUpdate();
            }
        }
        finally {
            this.closeConnection(conn);
        }
    }

    private final DbDataItemGetOperation<DbNotifyAddress> NOTIFY_ADDRESS_GET_OP =
            new DbDataItemGetOperation<DbNotifyAddress>(
            "SELECT naid, address FROM notifyaddresses") {
        @Override
        DbNotifyAddress getDbDataItem(ResultSet rs) throws SQLException {
            return new DbNotifyAddress(rs.getInt("naid"), rs.getString("address"));
        }
    };
    
    @Override
    public Set<DbNotifyAddress> getNotifyAddresses() throws SQLException {
        return this.getDbDataItems(NOTIFY_ADDRESS_GET_OP);
    }
    
    private final DbDataItemPutOperation<DbNotifyAddress> NOTIFY_ADDRESS_PUT_OP =
            new DbDataItemPutOperation<DbNotifyAddress>(
            "INSERT INTO notifyaddresses (address) VALUES (?)",
            "UPDATE notifyaddresses SET address = ? WHERE naid == ?") {
                
        @Override
        int setFields(PreparedStatement ps, DbNotifyAddress item) throws SQLException {
            ps.setString(1, item.getAddress());
            return 2;
        }
    };
    
    @Override
    public void putNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException {
        this.putDbDataItems(notifyAddresses, NOTIFY_ADDRESS_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }
    
    @Override
    public void removeNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException {
        this.removeDbDataItems("notifyaddresses", "naid", notifyAddresses);
    }

    private final DbDataItemGetOperation<DbTA> TA_GET_OP = new DbDataItemGetOperation<DbTA>(
            "SELECT tid, login, firstname, lastname, defaultgrader, admin FROM ta") {
        @Override
        DbTA getDbDataItem(ResultSet rs) throws SQLException {
            return new DbTA(rs.getInt("tid"), rs.getString("login"), rs.getString("firstname"), rs.getString("lastname"),
                            rs.getBoolean("defaultgrader"), rs.getBoolean("admin"));
        }
    };
    
    @Override
    public Set<DbTA> getTAs() throws SQLException {
        return this.getDbDataItems(TA_GET_OP);
    }

    @Override
    public void putTAs(Set<DbTA> tas) throws SQLException {
        Connection conn = this.openConnection();
        conn.setAutoCommit(false);
        
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE ta SET login = ?, firstname = ?, lastname = ?, "
                    + "admin = ?, defaultgrader = ? WHERE tid == ?");
            
            //try updating all; if a particular update changed no row, need to insert instead
            List<DbTA> orderedTAs = new ArrayList<DbTA>(tas);
            for (DbTA ta : tas) {
                ps.setString(1, ta.getLogin());
                ps.setString(2, ta.getFirstName());
                ps.setString(3, ta.getLastName());
                ps.setBoolean(4, ta.isAdmin());
                ps.setBoolean(5, ta.isDefaultGrader());
                ps.setInt(6, ta.getId());
                ps.addBatch();
            }
            
            List<DbTA> needInsert = new ArrayList<DbTA>();
            int[] results = ps.executeBatch();
            for (int i = 0; i < orderedTAs.size(); i++) {
                if (results[i] < 1) {
                    needInsert.add(orderedTAs.get(i));
                }
            }
            
            //now insert those that need it
            if (!needInsert.isEmpty()) {
                ps = conn.prepareStatement("INSERT INTO ta (tid, login, firstname, lastname, admin, defaultgrader) VALUES "
                        + "(?, ?, ?, ?, ?, ?)");
                for (DbTA ta : needInsert) {
                    ps.setInt(1, ta.getId());
                    ps.setString(2, ta.getLogin());
                    ps.setString(3, ta.getFirstName());
                    ps.setString(4, ta.getLastName());
                    ps.setBoolean(5, ta.isAdmin());
                    ps.setBoolean(6, ta.isDefaultGrader());
                    ps.addBatch();
                }
                
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private final DbDataItemGetOperation<DbStudent> STUDENT_GET_OP = new DbDataItemGetOperation<DbStudent>(
            "SELECT sid, login, firstname, lastname, email, enabled, hascollab FROM student") {
        @Override
        DbStudent getDbDataItem(ResultSet rs) throws SQLException {
            return new DbStudent(rs.getInt("sid"), rs.getString("login"), rs.getString("firstname"),
                                 rs.getString("lastname"), rs.getString("email"), rs.getBoolean("enabled"),
                                 rs.getBoolean("hascollab"));
        }
    };

    @Override
    public Set<DbStudent> getStudents() throws SQLException {
        return this.getDbDataItems(STUDENT_GET_OP);
    }
    
    private final DbDataItemPutOperation<DbStudent> STUDENT_PUT_OP = new DbDataItemPutOperation<DbStudent>(
            "INSERT INTO student (login, firstname, lastname, email, enabled, hascollab) VALUES (?, ?, ?, ?, ?, ?)",
            "UPDATE student SET login = ?, firstname = ?, lastname = ?, email = ?, enabled = ?, hascollab = ? WHERE sid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbStudent item) throws SQLException {
            ps.setString(1, item.getLogin());
            ps.setString(2, item.getFirstName());
            ps.setString(3, item.getLastName());
            ps.setString(4, item.getEmailAddress());
            ps.setBoolean(5, item.isEnabled());
            ps.setBoolean(6, item.hasCollabPolicy());
            return 7;
        }
    };

    @Override
    public void putStudents(Set<DbStudent> students) throws SQLException {
        this.putDbDataItems(students, STUDENT_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    //TODO look at factoring out repetitive assignment get code
    @Override
    public Set<DbAssignment> getAssignments() throws SQLException {
        ImmutableSet.Builder<DbAssignment> asgns = ImmutableSet.builder();
        
        Connection conn = this.openConnection();
        try {
            SetMultimap<Integer, DbGradableEvent> gradableEvents = this.getGradableEvents(conn);
            
            PreparedStatement ps = conn.prepareStatement("SELECT aid, name, ordering, hasgroups FROM assignment");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int asgnId = rs.getInt("aid");
                asgns.add(new DbAssignment(asgnId, rs.getString("name"), rs.getInt("ordering"), rs.getBoolean("hasgroups"),
                                           gradableEvents.get(asgnId)));
            }
        
            return asgns.build();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private SetMultimap<Integer, DbGradableEvent> getGradableEvents(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbGradableEvent> gradableEvents = ImmutableSetMultimap.builder();
        
        SetMultimap<Integer, DbPart> parts = this.getParts(conn);
        
        PreparedStatement ps = conn.prepareStatement("SELECT geid, aid, name, ordering, directory, deadlinetype,"
                + "earlydate, earlypoints, ontimedate, latedate, latepoints, lateperiod FROM gradableevent");
        
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int asgnId = rs.getInt("aid");
            int gradableEventId = rs.getInt("geid");
            gradableEvents.put(asgnId, new DbGradableEvent(asgnId, gradableEventId, rs.getString("name"),
                                                           rs.getInt("ordering"), rs.getString("directory"),
                                                           rs.getString("deadlinetype"), rs.getString("earlydate"),
                                                           getDouble(rs, "earlypoints"), rs.getString("ontimedate"),
                                                           rs.getString("latedate"), getDouble(rs, "latepoints"),
                                                           rs.getString("lateperiod"), parts.get(asgnId)));
        }
        
        return gradableEvents.build();
    }
    
    private SetMultimap<Integer, DbPart> getParts(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbPart> parts = ImmutableSetMultimap.builder();
        
        SetMultimap<Integer, DbPartAction> partActions = this.getPartActions(conn);
        SetMultimap<Integer, DbInclusionFilter> inclusionFilters = this.getInclusionFilters(conn);
        
        PreparedStatement ps = conn.prepareStatement("SELECT pid, geid, name, ordering, gmltemplate, outof, quickname, "
                + "gradingguide FROM part");
     
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int gradableEventId = rs.getInt("geid");
            int partId = rs.getInt("pid");
            parts.put(gradableEventId, new DbPart(gradableEventId, partId, rs.getString("name"), rs.getInt("ordering"),
                                                  rs.getString("gmltemplate"), getDouble(rs, "outof"),
                                                  rs.getString("quickname"), rs.getString("gradingguide"),
                                                  partActions.get(partId), inclusionFilters.get(partId)));
        }

        return parts.build();
    }
    
    private SetMultimap<Integer, DbInclusionFilter> getInclusionFilters(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbInclusionFilter> filters = ImmutableSetMultimap.builder();

        PreparedStatement ps = conn.prepareStatement("SELECT ifid, pid, type, path FROM inclusionfilter");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int partId = rs.getInt("pid");
            filters.put(partId, new DbInclusionFilter(partId, rs.getInt("ifid"), rs.getString("type"),
                                                      rs.getString("path")));
        }

        return filters.build();
    }
    
    private SetMultimap<Integer, DbPartAction> getPartActions(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbPartAction> properties = ImmutableSetMultimap.builder();
        
        SetMultimap<Integer, DbActionProperty> partActionProperties = this.getPartActionProperties(conn);
        
        PreparedStatement ps = conn.prepareStatement("SELECT paid, pid, type, name FROM partaction");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int partId = rs.getInt("pid");
            int partActionId = rs.getInt("paid");
            properties.put(partId, new DbPartAction(partId, partActionId, rs.getString("type"), rs.getString("name"),
                                                    partActionProperties.get(partActionId)));
        }

        return properties.build();
    }
    
    private SetMultimap<Integer, DbActionProperty> getPartActionProperties(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbActionProperty> properties = ImmutableSetMultimap.builder();

        PreparedStatement ps = conn.prepareStatement("SELECT apid, paid, key, value FROM actionproperty");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int partActionId = rs.getInt("paid");
            properties.put(partActionId, new DbActionProperty(partActionId, rs.getInt("apid"), rs.getString("key"),
                                                              rs.getString("value")));
        }

        return properties.build();
    }
    
    private final DbDataItemPutOperation<DbAssignment> ASGN_PUT_OP = new DbDataItemPutOperation<DbAssignment>(
            "INSERT INTO assignment (name, ordering, hasgroups) VALUES (?, ?, ?)",
            "UPDATE assignment SET name = ?, ordering = ?, hasgroups = ? WHERE aid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbAssignment toInsert) throws SQLException {
            ps.setString(1, toInsert.getName());
            ps.setInt(2, toInsert.getOrder());
            ps.setBoolean(3, toInsert.hasGroups());
            return 4;
        }
    };

    @Override
    public void putAssignments(Set<DbAssignment> assignments) throws SQLException {
        this.putDbDataItems(assignments, ASGN_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeAssignments(Set<DbAssignment> assignments) throws SQLException {
        this.removeDbDataItems("assignment", "aid", assignments);
    }

    private final DbDataItemPutOperation<DbGradableEvent> GE_PUT_OP = new DbDataItemPutOperation<DbGradableEvent>(
            "INSERT INTO gradableevent (aid, name, ordering, directory, deadlinetype, earlydate, earlypoints, ontimedate,"
            + "latedate, latepoints, lateperiod) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            "UPDATE gradableevent SET aid = ?, name = ?, ordering = ?, directory = ?, deadlinetype = ?, earlydate = ?,"
            + "earlypoints = ?, ontimedate = ?, latedate = ?, latepoints = ?, lateperiod = ? WHERE geid = ?") {

        @Override
        int setFields(PreparedStatement ps, DbGradableEvent item) throws SQLException {
            ps.setInt(1, item.getAssignmentId());
            ps.setString(2, item.getName());
            ps.setInt(3, item.getOrder());
            setObjectAsStringNullSafe(ps, 4, item.getDirectory());
            setObjectAsStringNullSafe(ps, 5, item.getDeadlineType());
            setObjectAsStringNullSafe(ps, 6, item.getEarlyDate());
            setDouble(ps, 7, item.getEarlyPoints());
            setObjectAsStringNullSafe(ps, 8, item.getOnTimeDate());
            setObjectAsStringNullSafe(ps, 9, item.getLateDate());
            setDouble(ps, 10, item.getLatePoints());
            setObjectAsStringNullSafe(ps, 11, item.getLatePeriod());
            return 12;
        }
    };
    
    @Override
    public void putGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException {
        this.putDbDataItems(gradableEvents, GE_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException {
        this.removeDbDataItems("gradableevent", "geid", gradableEvents);
    }

    private final DbDataItemPutOperation<DbPart> PART_PUT_OP = new DbDataItemPutOperation<DbPart>(
            "INSERT INTO PART (geid, name, ordering, gmltemplate, outof, quickname, gradingguide) VALUES (?, ?, ?, ?, ?, ?, ?)",
            "UPDATE part SET geid = ?, name = ?, ordering = ?, gmltemplate = ?, outof = ?, quickname = ?, gradingguide = ? "
            + "WHERE pid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbPart item) throws SQLException {
            ps.setInt(1, item.getGradableEventId());
            ps.setString(2, item.getName());
            ps.setInt(3, item.getOrder());
            setObjectAsStringNullSafe(ps, 4, item.getGmlTemplate());
            setDouble(ps, 5, item.getOutOf());
            ps.setString(6, item.getQuickName());
            setObjectAsStringNullSafe(ps, 7, item.getGradingGuide());
            return 8;
        }
    };
    
    @Override
    public void putParts(Set<DbPart> parts) throws SQLException {
        this.putDbDataItems(parts, PART_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeParts(Set<DbPart> parts) throws SQLException {
        this.removeDbDataItems("part", "pid", parts);
    }

    private final DbDataItemPutOperation<DbPartAction> PART_ACTION_PUT_OP = new DbDataItemPutOperation<DbPartAction>(
            "INSERT INTO partaction (pid, type, name) VALUES (?, ?, ?)", 
            "UPDATE partaction SET pid = ?, type = ?, name = ? WHERE paid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbPartAction item) throws SQLException {
            ps.setInt(1, item.getPartId());
            setObjectAsStringNullSafe(ps, 2, item.getType());
            ps.setString(3, item.getName());
            return 4;
        }
    };
    
    @Override
    public void putPartActions(Set<DbPartAction> partActions) throws SQLException {
        this.putDbDataItems(partActions, PART_ACTION_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removePartActions(Set<DbPartAction> partActions) throws SQLException {
        this.removeDbDataItems("partaction", "paid", partActions);
    }
    
    private final DbDataItemPutOperation<DbActionProperty> ACTION_PROPERTY_PUT_OP = new DbDataItemPutOperation<DbActionProperty>(
            "INSERT INTO actionproperty (paid, key, value) VALUES (?, ?, ?)", 
            "UPDATE actionproperty SET paid = ?, key = ?, value = ? WHERE apid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbActionProperty item) throws SQLException {
            ps.setInt(1, item.getPartActionId());
            ps.setString(2, item.getKey());
            ps.setString(3, item.getValue());
            return 4;
        }
    };
    
    @Override
    public void putPartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException {
        this.putDbDataItems(actionProperties, ACTION_PROPERTY_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removePartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException {
        this.removeDbDataItems("actionproperty", "apid", actionProperties);
    }
    
    private final DbDataItemPutOperation<DbInclusionFilter> INCLUSION_FILTER_PUT_OP = new DbDataItemPutOperation<DbInclusionFilter>(
            "INSERT INTO inclusionfilter (pid, type, path) VALUES (?, ?, ?)", 
            "UPDATE inclusionfilter SET pid = ?, type = ?, path = ? WHERE ifid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbInclusionFilter item) throws SQLException {
            ps.setInt(1, item.getPartId());
            setObjectAsStringNullSafe(ps, 2, item.getType());
            ps.setString(3, item.getPath());
            return 4;
        }
    };

    @Override
    public void putInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException {
        this.putDbDataItems(inclusionFilters, INCLUSION_FILTER_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException {
        this.removeDbDataItems("inclusionfilter", "ifid", inclusionFilters);
    }
    
    private Double getDouble(ResultSet rs, String field) throws SQLException {
        Double d = rs.getDouble(field);
        if (rs.wasNull()) {
            d = null;
        }

        return d;
    }
    
    private void setDouble(PreparedStatement ps, int pos, Double d) throws SQLException {
        if (d == null) {
            ps.setNull(pos, java.sql.Types.DOUBLE);
        }
        else {
            ps.setDouble(pos, d);
        }
    }
    
    private void setObjectAsStringNullSafe(PreparedStatement ps, int pos, Object o) throws SQLException {
        String toSet = o == null ? null : o.toString();
        ps.setString(pos, toSet);
    }
    
    private abstract class DbDataItemGetOperation<T extends DbDataItem> {
        
        private String _selectCommand;
        
        private DbDataItemGetOperation(String selectCommand) {
            _selectCommand = selectCommand;
        }
        
        abstract T getDbDataItem(ResultSet rs) throws SQLException;
    }

    private <T extends DbDataItem> Set<T> getDbDataItems(DbDataItemGetOperation<T> operation) throws SQLException {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(operation._selectCommand);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                builder.add(operation.getDbDataItem(rs));
            }
            
            return builder.build();
        } finally {
            this.closeConnection(conn);
        }
    }

    private abstract class DbDataItemPutOperation<T extends DbDataItem> {
        
        private String _insertCommand, _updateCommand;

        private DbDataItemPutOperation(String insertCommand, String updateCommand) {
            _insertCommand = insertCommand;
            _updateCommand = updateCommand;
        }
        
        abstract int setFields(PreparedStatement ps, T item) throws SQLException;
    }

    private <T extends DbDataItem> void putDbDataItems(Set<T> items, DbDataItemPutOperation<T> operation,
                                                       DbDataItemIdUpdater idUpdater) throws SQLException {
        Connection conn = this.openConnection();
        conn.setAutoCommit(false);
        
        try {
            PreparedStatement psInsert = conn.prepareStatement(operation._insertCommand);
            PreparedStatement psUpdate = conn.prepareStatement(operation._updateCommand);
            
            List<T> orderedItems = new ArrayList<T>(items);
            List<DbDataItem> insertedItems = new ArrayList<DbDataItem>();
            List<T> updatedItems = new ArrayList<T>();
            for (T item : orderedItems) {
                Integer id = item.getId();
                
                if (id == null) {
                    operation.setFields(psInsert, item);
                    psInsert.addBatch();
                    insertedItems.add(item);
                }
                else {
                    int idPosition = operation.setFields(psUpdate, item);
                    psUpdate.setInt(idPosition, item.getId());
                    psUpdate.addBatch();
                    updatedItems.add(item);
                }
            }
            
            if (!insertedItems.isEmpty()) {
                psInsert.executeBatch();
                idUpdater.updateIds(psUpdate, insertedItems);
            }
            if (!updatedItems.isEmpty()) {
                int[] results = psUpdate.executeBatch();
                this.checkUpdateValidity(results, updatedItems);
            }
            
            psInsert.close();
            psUpdate.close();
            
            conn.commit();            
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private <T extends DbDataItem> void removeDbDataItems(String table, String idField, Set<T> toRemove) throws SQLException {
        Connection conn = this.openConnection();
        conn.setAutoCommit(false);
        
        try {
            String deleteCommand = String.format("DELETE FROM %s WHERE %s.%s == ?", table, table, idField);
            PreparedStatement ps = conn.prepareStatement(deleteCommand);
            
            List<DbDataItem> orderingedItems = new ArrayList<DbDataItem>(toRemove);
            for (DbDataItem item : orderingedItems) {
                ps.setInt(1, item.getId());
                ps.addBatch();
            }
            
            int[] results = ps.executeBatch();
            this.checkUpdateValidity(results, orderingedItems);
            REMOVAL_ID_UPDATER.updateIds(ps, orderingedItems);
            
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private interface DbDataItemIdUpdater {
        void updateIds(PreparedStatement ps, List<DbDataItem> items) throws SQLException;
    }
    
    private final DbDataItemIdUpdater DEFAULT_INSERTION_ID_UPDATER = new DbDataItemIdUpdater() {
        @Override
        public void updateIds(PreparedStatement ps, List<DbDataItem> items) throws SQLException {
            //only the last inserted key is returned
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int lastID = rs.getInt(1);
                for (int i = items.size() - 1; i >= 0; i--) {
                    DbDataItem item = items.get(i);
                    item.setId(lastID);
                    
                    for (DbDataItem child : item.getChildren()) {
                        child.setParentId(lastID);
                    }
                    
                    lastID--;
                }

                rs.close();
            }
            else {
                rs.close();
                throw new SQLException("Something is horribly wrong--internal object IDs could not be updated properly.");
            }
        }
    };
    
    //used by the DbTA object because the IDs are POSIX IDs, not autogenerated DB IDs.
    private final DbDataItemIdUpdater DO_NOTHING_ID_UPDATER = new DbDataItemIdUpdater() {
        @Override
        public void updateIds(PreparedStatement ps, List<DbDataItem> items) throws SQLException {}
    };
    
    private final DbDataItemIdUpdater REMOVAL_ID_UPDATER = new DbDataItemIdUpdater() {
        @Override
        public void updateIds(PreparedStatement ps, List<DbDataItem> items) throws SQLException {
            for (DbDataItem item : items) {
                item.setId(null);
                setChildIdsNull(item.getChildren());
            }
        }
    };
    
    private void setChildIdsNull(Iterable<DbDataItem> children) {
        for (DbDataItem child : children) {
            child.setParentId(null);
            child.setId(null);
            this.setChildIdsNull(child.getChildren());
        }
    }
    
    private <T extends DbDataItem> void checkUpdateValidity(int[] results, List<T> modifedItems) throws SQLException {
        //a non-positive element in the results array means an update was unsuccessful or did not update any rows
        for (int i = 0; i < results.length; i++) {
            if (results[i] < 1) {
                DbDataItem firstProblem = modifedItems.get(i);
                throw new SQLException("There was no row in the table with ID " + firstProblem.getId() + ","
                        + " or some unknown insidious database issue occurred. No rows have been inserted or updated.");
            }
        }
    }
    
    @Override
    public Set<StudentRecord> getAllStudents() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void blacklistStudents(Set<Integer> studentIDs, int taID) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unBlacklistStudents(Set<Integer> studentIDs, int taID) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getBlacklistedStudents() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getBlacklist(int taID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Set<GroupRecord> getAllGroups() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public GroupRecord addGroup(NewGroup group) throws SQLException, CakeHatDBIOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<GroupRecord> addGroups(Set<NewGroup> groups) 
                                    throws SQLException, CakeHatDBIOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getGroups(int asgnID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeGroups(int asgnID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean isDistEmpty(Set<Integer> partIDs) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Map<Integer, Set<Integer>> getDistribution(int partID) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void setDistribution(Map<Integer, Map<Integer, Set<Integer>>> distribution) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void assignGroup(int groupID, int partID, int taID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unassignGroup(int groupID, int partID, int taID) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getAssignedGroups(int partID, int taID) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getAssignedGroups(int partID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getPartsWithAssignedGroups(int taID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Integer getGrader(Integer partID, int groupID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setEarned(int groupID, int partID, int taID, double earned,
                            boolean matchesGml) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GradeRecord getEarned(int groupID, int partID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Integer, GradeRecord> getAllEarned(int partID, Set<Integer> groupIDs) 
                                                        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void resetDatabase() throws SQLException {
        Connection conn = this.openConnection();
        try {
            //DROP all tables in DB
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS notifyaddresses");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS ta");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS student");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS assignment");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS gradableevent");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS part");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS partaction");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS actionproperty");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS inclusionfilter");

            //CREATE all DB tables
            conn.createStatement().executeUpdate("CREATE TABLE courseproperties (cpid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "key VARCHAR NOT NULL,"
                    + "value BLOB NOT NULL," //note that the values may be booleans as well
                    + "CONSTRAINT keysunique UNIQUE (key) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE notifyaddresses (naid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " address VARCHAR NOT NULL)");
            conn.createStatement().executeUpdate("CREATE TABLE ta (tid INTEGER PRIMARY KEY NOT NULL,"
                    + " login VARCHAR NOT NULL,"
                    + " firstname VARCHAR NOT NULL,"
                    + " lastname VARCHAR NOT NULL,"
                    + " admin INTEGER NOT NULL DEFAULT 0,"
                    + " defaultgrader INTEGER NOT NULL DEFAULT 0,"
                    + " CONSTRAINT tidunique UNIQUE (tid) ON CONFLICT ROLLBACK,"
                    + " CONSTRAINT loginunique UNIQUE (login) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE student (sid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " login VARCHAR NOT NULL,"
                    + " firstname VARCHAR NOT NULL,"
                    + " lastname VARCHAR NOT NULL,"
                    + " email VARCHAR NOT NULL,"
                    + " enabled INTEGER NOT NULL DEFAULT 1,"
                    + " hascollab INTEGER NOT NULL DEFAULT 0,"
                    + " CONSTRAINT loginunique UNIQUE (login) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE assignment (aid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " name VARCHAR NOT NULL,"
                    + " ordering INTEGER NOT NULL,"
                    + " hasgroups INTEGER NOT NULL DEFAULT 0)");
            conn.createStatement().executeUpdate("CREATE TABLE gradableevent (geid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " aid INTEGER NOT NULL,"
                    + " name VARCHAR NOT NULL,"
                    + " ordering INTEGER NOT NULL,"
                    + " directory VARCHAR,"
                    + " deadlinetype VARCHAR,"
                    + " earlydate VARCHAR,"
                    + " earlypoints DOUBLE,"
                    + " ontimedate VARCHAR,"
                    + " latedate VARCHAR,"
                    + " latepoints DOUBLE,"
                    + " lateperiod VARCHAR,"
                    + " FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE)");
            conn.createStatement().executeUpdate("CREATE TABLE part (pid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " geid INTEGER NOT NULL,"
                    + " name VARCHAR NOT NULL,"
                    + " ordering INTEGER NOT NULL,"
                    + " gmltemplate VARCHAR,"
                    + " outof DOUBLE,"
                    + " quickname VARCHAR,"
                    + " gradingguide VARCHAR,"
                    + " FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE,"
                    + " CONSTRAINT orderingunique UNIQUE (ordering) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE partaction (paid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " pid INTEGER NOT NULL,"
                    + " type VARCHAR NOT NULL,"
                    + " name VARCHAR NOT NULL,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE,"
                    + " CONSTRAINT pidtypeunique UNIQUE (pid, name) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE actionproperty (apid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " paid INTEGER NOT NULL,"
                    + " key VARCHAR NOT NULL,"
                    + " value VARCHAR NOT NULL,"
                    + " FOREIGN KEY (apid) REFERENCES partaction(paid) ON DELETE CASCADE,"
                    + " CONSTRAINT paidkeyunique UNIQUE (paid, key) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE inclusionfilter (ifid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " pid INTEGER NOT NULL,"
                    + " type VARCHAR NOT NULL,"
                    + " path VARCHAR NOT NULL,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE,"
                    + " CONSTRAINT pidpathunique UNIQUE (pid, path) ON CONFLICT ROLLBACK)");

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    public static void main(String[] argv) throws SQLException {
        DatabaseV5Impl db = new DatabaseV5Impl();
        db.resetDatabase();
    }
}
