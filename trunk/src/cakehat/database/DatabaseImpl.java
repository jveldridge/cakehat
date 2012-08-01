package cakehat.database;

import cakehat.Allocator;
import cakehat.InitializationException;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import cakehat.services.ServicesException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.sqlite.SQLiteConfig;
import support.utils.Pair;

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
                databaseFile.createNewFile();
                Allocator.getFileSystemServices().sanitize(databaseFile);

                this.resetDatabase();
            }
            catch(SQLException ex)
            {
                throw new InitializationException("cakehat is unable to create a database", ex);
            }
            catch(ServicesException ex)
            {
                throw new InitializationException("cakehat is unable to create a database", ex);
            }
            catch(IOException ex)
            {
                throw new InitializationException("cakehat is unable to create a database", ex);
            }
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
                ps = conn.prepareStatement("UPDATE courseproperties set  value = ? WHERE cpid == ?");
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
        try {
            conn.setAutoCommit(false);
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
            ps.setBoolean(6, item.hasCollabContract());
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
    
    @Override
    public DbGradableEvent getDbGradableEvent(int geid) throws SQLException {
        Connection conn = this.openConnection();

        try {
            Collection<DbGradableEvent> gradableEvents = this.getGradableEvents(conn).values();
            for (DbGradableEvent ge : gradableEvents) {
                if (ge.getId() == geid) {
                    return ge;
                }
            }
            return null;
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
                                                           rs.getString("lateperiod"), parts.get(gradableEventId)));
        }
        
        return gradableEvents.build();
    }
    
    private SetMultimap<Integer, DbPart> getParts(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbPart> parts = ImmutableSetMultimap.builder();
        
        SetMultimap<Integer, DbAction> actions = this.getActions(conn);
        SetMultimap<Integer, DbInclusionFilter> inclusionFilters = this.getInclusionFilters(conn);
        
        PreparedStatement ps = conn.prepareStatement("SELECT pid, geid, name, ordering, gmltemplate, outof, quickname "
                + " FROM part");
     
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int gradableEventId = rs.getInt("geid");
            int partId = rs.getInt("pid");
            parts.put(gradableEventId, new DbPart(gradableEventId, partId, rs.getString("name"), rs.getInt("ordering"),
                                                  rs.getString("gmltemplate"), getDouble(rs, "outof"),
                                                  rs.getString("quickname"), actions.get(partId),
                                                  inclusionFilters.get(partId)));
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
    
    private SetMultimap<Integer, DbAction> getActions(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbAction> actions = ImmutableSetMultimap.builder();
        
        SetMultimap<Integer, DbActionProperty> actionProperties = this.getActionProperties(conn);
        
        PreparedStatement ps = conn.prepareStatement("SELECT acid, pid, name, icon, ordering, task FROM action");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int partId = rs.getInt("pid");
            int actionId = rs.getInt("acid");
            
            actions.put(partId, new DbAction(partId, actionId, rs.getString("name"), rs.getString("icon"),
                    rs.getInt("ordering"), rs.getString("task"), actionProperties.get(actionId)));
        }

        return actions.build();
    }
    
    private SetMultimap<Integer, DbActionProperty> getActionProperties(Connection conn) throws SQLException {
        ImmutableSetMultimap.Builder<Integer, DbActionProperty> properties = ImmutableSetMultimap.builder();

        PreparedStatement ps = conn.prepareStatement("SELECT apid, acid, key, value FROM actionproperty");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int partActionId = rs.getInt("acid");
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
            "INSERT INTO PART (geid, name, ordering, gmltemplate, outof, quickname) VALUES (?, ?, ?, ?, ?, ?)",
            "UPDATE part SET geid = ?, name = ?, ordering = ?, gmltemplate = ?, outof = ?, quickname = ? "
            + "WHERE pid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbPart item) throws SQLException {
            ps.setInt(1, item.getGradableEventId());
            ps.setString(2, item.getName());
            ps.setInt(3, item.getOrder());
            setObjectAsStringNullSafe(ps, 4, item.getGmlTemplate());
            setDouble(ps, 5, item.getOutOf());
            ps.setString(6, item.getQuickName());
            return 7;
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

    private final DbDataItemPutOperation<DbAction> ACTION_PUT_OP = new DbDataItemPutOperation<DbAction>(
            "INSERT INTO action (pid, name, icon, ordering, task) VALUES (?, ?, ?, ?, ?)", 
            "UPDATE action SET pid = ?, name = ?, icon = ?, ordering = ?, task = ? WHERE acid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbAction item) throws SQLException {
            ps.setInt(1, item.getPartId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getIcon().toString());
            ps.setInt(4, item.getOrder());
            setObjectAsStringNullSafe(ps, 5, item.getTask());
            return 6;
        }
    };
    
    @Override
    public void putActions(Set<DbAction> partActions) throws SQLException {
        this.putDbDataItems(partActions, ACTION_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeActions(Set<DbAction> partActions) throws SQLException {
        this.removeDbDataItems("action", "acid", partActions);
    }
    
    private final DbDataItemPutOperation<DbActionProperty> ACTION_PROPERTY_PUT_OP = new DbDataItemPutOperation<DbActionProperty>(
            "INSERT INTO actionproperty (acid, key, value) VALUES (?, ?, ?)", 
            "UPDATE actionproperty SET acid = ?, key = ?, value = ? WHERE apid == ?") {

        @Override
        int setFields(PreparedStatement ps, DbActionProperty item) throws SQLException {
            ps.setInt(1, item.getActionId());
            ps.setString(2, item.getKey());
            ps.setString(3, item.getValue());
            return 4;
        }
    };
    
    @Override
    public void putActionProperties(Set<DbActionProperty> actionProperties) throws SQLException {
        this.putDbDataItems(actionProperties, ACTION_PROPERTY_PUT_OP, DEFAULT_INSERTION_ID_UPDATER);
    }

    @Override
    public void removeActionProperties(Set<DbActionProperty> actionProperties) throws SQLException {
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
        try {
            conn.setAutoCommit(false);
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
        try {
            conn.setAutoCommit(false);
            String deleteCommand = String.format("DELETE FROM %s WHERE %s.%s == ?", table, table, idField);
            PreparedStatement ps = conn.prepareStatement(deleteCommand);
            
            List<DbDataItem> orderingedItems = new ArrayList<DbDataItem>(toRemove);
            for (DbDataItem item : orderingedItems) {
                if (item.getId() != null) {
                    ps.setInt(1, item.getId());
                    ps.addBatch();
                }
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
        void updateIds(PreparedStatement ps, List<? extends DbDataItem> items) throws SQLException;
    }
    
    private final DbDataItemIdUpdater DEFAULT_INSERTION_ID_UPDATER = new DbDataItemIdUpdater() {
        @Override
        public void updateIds(PreparedStatement ps, List<? extends DbDataItem> items) throws SQLException {
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
        public void updateIds(PreparedStatement ps, List<? extends DbDataItem> items) throws SQLException {}
    };
    
    private final DbDataItemIdUpdater REMOVAL_ID_UPDATER = new DbDataItemIdUpdater() {
        @Override
        public void updateIds(PreparedStatement ps, List<? extends DbDataItem> items) throws SQLException {
            for (DbDataItem item : items) {
                item.setId(null);
                setChildIdsNull(item.getChildren());
            }
        }
    };
    
    private void setChildIdsNull(Iterable<? extends DbDataItem> children) {
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
    public void setStudentsAreEnabled(Map<Integer, Boolean> studentsToUpdate) throws SQLException {
        if (studentsToUpdate.values().contains(null)) {
            throw new NullPointerException("Enabled value may not be set to null.");
        }
        
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("UPDATE student SET enabled = ? WHERE sid = ?");
            
            for (Integer studentID : studentsToUpdate.keySet()) {
                ps.setBoolean(1, studentsToUpdate.get(studentID));
                ps.setInt(2, studentID);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setStudentsHasCollaborationContract(Map<Integer, Boolean> studentsToUpdate) throws SQLException {   
        if(studentsToUpdate.keySet().contains(null) || studentsToUpdate.values().contains(null)) {
            throw new NullPointerException("Collaboration contract map may not contain null key or value");
        }
        
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("UPDATE student SET hascollab = ? WHERE sid == ?");
            
            for(Entry<Integer, Boolean> entry : studentsToUpdate.entrySet()) {
                ps.setBoolean(1, entry.getValue());
                ps.setInt(2, entry.getKey());
                ps.addBatch();
            }
            
            ps.executeBatch();
            ps.close();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void blacklistStudents(Set<Integer> studentIDs, int taID) 
                                                        throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO blacklist "
                    + "('sid', 'tid') VALUES (?, ?)");

            for (Integer studentID : studentIDs) {
                ps.setInt(1, studentID);
                ps.setInt(2, taID);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unBlacklistStudents(Set<Integer> studentIDs, int taID) 
                                                        throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM blacklist "
                    + "WHERE tid == ? AND sid == ?");

            for (Integer studentID : studentIDs) {
                ps.setInt(1, taID);
                ps.setInt(2, studentID);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Set<Integer> getBlacklistedStudents() throws SQLException {
        Connection conn = this.openConnection();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT b.sid AS sid "
                    + "FROM blacklist AS b");
            Set<Integer> result = new HashSet<Integer>();
            while (rs.next()) {
                result.add(rs.getInt("sid"));
            }
            return ImmutableSet.copyOf(result);
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Set<Integer> getBlacklist(int taID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            Set<Integer> blackList = new HashSet<Integer>();
            
            PreparedStatement ps = conn.prepareStatement("SELECT b.sid AS sid"
                                                     + " FROM blacklist AS b"
                                                     + " WHERE b.tid == ?");
            ps.setInt(1, taID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blackList.add(rs.getInt("sid"));
            }
            ps.close();

            return ImmutableSet.copyOf(blackList);
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Set<DbGroup> getGroups() throws SQLException {
        Connection conn = this.openConnection();
        try {
            return this.getGroups(conn);
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private Set<DbGroup> getGroups(Connection conn) throws SQLException {
        Set<DbGroup> result = new HashSet<DbGroup>();

        PreparedStatement ps = conn.prepareStatement("SELECT gp.agid as agid, gm.sid AS sid,"
                + " gp.name AS groupName, gp.aid AS aid"
                + " FROM asgngroup AS gp"
                + " LEFT JOIN groupmember AS gm" //so that the groups with no members get returned as well
                + " ON gm.agid == gp.agid"
                + " ORDER BY gp.agid");

        ResultSet rs = ps.executeQuery();

        int prevGroupId = 0;
        int groupAsgnId = 0;
        String groupName = null;
        Set<Integer> memberIDs = new HashSet<Integer>();
        while (rs.next()) { //while there are more records
            int currGroupId = rs.getInt("agid");

            if (currGroupId != prevGroupId) {   //current row represents the beginning of a new group
                if (prevGroupId != 0) {
                    //create record for previous group
                    result.add(new DbGroup(groupAsgnId, prevGroupId, groupName, memberIDs));
                }

                memberIDs.clear();
                prevGroupId = currGroupId;
                groupAsgnId = rs.getInt("aid");
                groupName = rs.getString("groupName");
            }
            
            int memberId = rs.getInt("sid");
            if (memberId != 0) { // add the id only when it's not null (NULL becomes 0 after getInt)
                memberIDs.add(memberId);
            }
        }
        //create record for last group
        if (prevGroupId != 0) {
            result.add(new DbGroup(groupAsgnId, prevGroupId, groupName, memberIDs));
        }

        return ImmutableSet.copyOf(result);
    }
    
    @Override
    public void putGroups(Set<DbGroup> groups) throws SQLException {        
        Set<DbGroup> groupsToAdd = new HashSet<DbGroup>();
        Set<DbGroup> groupsToUpdate = new HashSet<DbGroup>();
        for (DbGroup group : groups) {
            if (group.getId() == null) {
                groupsToAdd.add(group);
            }
            else {
                groupsToUpdate.add(group);
            }
        }
        
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
        
            //for each group that will be updated, figure out which students currently in the group in the database
            //need to be removed from the group, and which students are not yet in the group in the database
            //and must be added to it
            Multimap<DbGroup, Integer> studentsToRemove = HashMultimap.create();
            Multimap<DbGroup, Integer> studentsToAdd = HashMultimap.create();
            
            //map each group ID to a DbGroup object representing the corresponding group in the database
            Map<Integer, DbGroup> groupsInDb = new HashMap<Integer, DbGroup>();
            for (DbGroup group : this.getGroups(conn)) {
                groupsInDb.put(group.getId(), group);
            }

            for (DbGroup group : groupsToUpdate) {
                Set<Integer> membersInDb = groupsInDb.get(group.getId()).getMemberIds();
                
                Set<Integer> studentsToRemoveForGroup = new HashSet<Integer>(membersInDb);
                studentsToRemoveForGroup.removeAll(group.getMemberIds());
                studentsToRemove.putAll(group, studentsToRemoveForGroup);
                
                Set<Integer> studentsToAddForGroup = new HashSet<Integer>(group.getMemberIds());
                studentsToAddForGroup.removeAll(membersInDb);
                studentsToAdd.putAll(group, studentsToAddForGroup);
            }
            
            //remove students that need to be removed
            PreparedStatement psDelete = conn.prepareStatement("DELETE FROM groupmember WHERE agid == ? AND sid == ?");
            for (Entry<DbGroup, Integer> entryToDelete : studentsToRemove.entries()) {
                psDelete.setInt(1, entryToDelete.getKey().getId());
                psDelete.setInt(2, entryToDelete.getValue());
                psDelete.addBatch();
            }
            psDelete.executeBatch();
            psDelete.close();

            this.updateGroups(groupsToUpdate, studentsToAdd, conn);
            this.addGroups(groupsToAdd, conn);

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            
            //revert any updates to DbGroup IDs
            for (DbGroup group : groupsToAdd) {
                group.setId(null);
            }
            
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private void updateGroups(Set<DbGroup> allGroups, Multimap<DbGroup, Integer> studentsToAdd,
                              Connection conn) throws SQLException {
        //update group names        
        PreparedStatement psGroup = conn.prepareStatement("UPDATE asgngroup SET name == ? WHERE agid == ?");
        for (DbGroup group : allGroups) {
            psGroup.setString(1, group.getName());
            psGroup.setInt(2, group.getId());
            psGroup.addBatch();
        }
        psGroup.executeBatch();
        psGroup.close();
        
        //add students to add
        PreparedStatement psMember = conn.prepareStatement("INSERT INTO groupmember(agid, sid) VALUES (?, ?)");
        for (Entry<DbGroup, Integer> entry : studentsToAdd.entries()) {
            psMember.setInt(1, entry.getKey().getId());
            psMember.setInt(2, entry.getValue());
            psMember.addBatch();
        }
        psMember.executeBatch();
        psMember.close();
    }
    
    private void addGroups(Set<DbGroup> groups, Connection conn) throws SQLException {
        // create List of DbGroup objects to guarantee consistent iteration order
        List<DbGroup> groupList = new ArrayList<DbGroup>(groups);
        
        // insert all the groups
        PreparedStatement psGroup = conn.prepareStatement("INSERT INTO asgngroup"
                + " ('name', 'aid') VALUES (?, ?)");
        for (DbGroup group : groupList) {
            psGroup.setString(1, group.getName());
            psGroup.setInt(2, group.getAssignmentId());
            psGroup.addBatch();
        }
        psGroup.executeBatch();

        // update IDs of DbGroup objects
        DEFAULT_INSERTION_ID_UPDATER.updateIds(psGroup, groupList);
        psGroup.close();

        // add all the members to those groups
        PreparedStatement psMember = conn.prepareStatement("INSERT INTO groupmember (agid, sid) VALUES (?, ?)");
        for (DbGroup group : groupList) {
            for (Integer memberId : group.getMemberIds()) {
                psMember.setInt(1, group.getId());
                psMember.setInt(2, memberId);
                psMember.addBatch();
            }
        }
        psMember.executeBatch();
        psMember.close();
    }
    
    @Override
    public void removeGroups(Set<DbGroup> groups) throws SQLException {
        this.removeDbDataItems("asgngroup", "agid", groups);
    }

    @Override
    public Set<DbGroup> getGroups(int asgnID) throws SQLException {
        Set<DbGroup> groupsForAsgn = new HashSet<DbGroup>();
        
        Set<DbGroup> allGroups = this.getGroups();
        for (DbGroup group : allGroups) {
            if (group.getAssignmentId() == asgnID) {
                groupsForAsgn.add(group);
            }
        }
        
        return groupsForAsgn;
    }
    
    private String partIDsSetToString(Set<Integer> partIDs) {
        StringBuilder builder = new StringBuilder();
        Iterator<Integer> iterator = partIDs.iterator();
        while (iterator.hasNext()) {
            builder.append('\'').append(iterator.next()).append('\'');
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        
        return builder.toString();
    }
    
    @Override
    public boolean isDistEmpty(Set<Integer> partIDs) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.agid) AS rowcount"
                    + " FROM distribution AS d"
                    + " WHERE d.pid IN (" + this.partIDsSetToString(partIDs) + ")");
            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            ps.close();

            return rows == 0;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public SetMultimap<Integer, Integer> getDistribution(int partID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            SetMultimap<Integer, Integer> dist = HashMultimap.create();

            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS taID, d.agid AS agid"
                    + " FROM distribution AS d"
                    + " WHERE d.pid == ?");
            ps.setInt(1, partID);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                dist.put(rs.getInt("taID"), rs.getInt("agid"));
            }
            
            return dist; 
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setDistribution(Map<Integer, Map<Integer, Set<Integer>>> distribution) 
                                                        throws SQLException {
        Connection conn = this.openConnection();
        try {  
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE distribution.pid == ?");
            for (int partID : distribution.keySet()) {
                ps.setInt(1, partID);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            
            ps = conn.prepareStatement(
                    "INSERT INTO distribution ('pid', 'agid', 'tid') "
                    + "VALUES (?, ?, ?)");
            for (int partID : distribution.keySet()) {
                for (int taID : distribution.get(partID).keySet()) {
                    for (Integer groupID : distribution.get(partID).get(taID)) {
                        ps.setInt(1, partID);
                        ps.setInt(2, groupID);
                        ps.setInt(3, taID);
                        ps.addBatch();   
                    }
                }
            }

            ps.executeBatch();
            ps.close();

            // commit all the inserts to the DB file
            conn.commit();
        } catch (SQLException e) {
            // the exception is caught so that any old distribution is preserved
            conn.rollback();

            //then the exception is re-thrown to inform the client of the error
            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void assignGroup(int groupID, int partID, int taID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO distribution ('agid', 'tid', 'pid')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setInt(2, taID);
            ps.setInt(3, partID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unassignGroup(int groupID, int partID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE pid == ?"
                    + " AND agid == ?");
            ps.setInt(1, partID);
            ps.setInt(2, groupID);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Set<Integer> getAssignedGroups(int partID, int taID) 
                                                        throws SQLException {
        Collection<Integer> fromDist = this.getDistribution(partID).get(taID);
        if (fromDist != null) {
            return ImmutableSet.copyOf(fromDist);
        }

        return Collections.emptySet();
    }

    @Override
    public Set<Integer> getAssignedGroups(int partID) throws SQLException {
        return ImmutableSet.copyOf(this.getDistribution(partID).values());
    }

    @Override
    public Set<Integer> getPartsWithAssignedGroups(int taID) throws SQLException {
        Set<Integer> partIDs = new HashSet<Integer>();

        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.pid AS partID"
                    + " FROM distribution AS d"
                    + " WHERE d.tid == ?");
            ps.setInt(1, taID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int partID = rs.getInt("partID");
                partIDs.add(partID);
            }

            return ImmutableSet.copyOf(partIDs);
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Integer getGrader(Integer partID, int groupID) throws SQLException {
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS taID"
                    + " FROM distribution AS d"
                    + " WHERE d.pid==? AND d.agid==?");
            ps.setInt(1, partID);
            ps.setInt(2, groupID);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("taID");
            }
            return null;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setExtensions(int geId, String ontime, boolean shiftDates, String note, String dateRecorded,
            int taId, Set<Integer> groupIds) throws SQLException
    {
        Connection conn = this.openConnection();
        try
        {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO extension "
                + "('agid', 'geid', 'tid', 'daterecorded', 'ontime', 'shiftdates', 'note')"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)");
            
            for(Integer agid : groupIds)
            {
                ps.setInt(1, agid);
                ps.setInt(2, geId);
                ps.setInt(3, taId);
                ps.setString(4, dateRecorded);
                ps.setString(5, ontime);
                ps.setBoolean(6, shiftDates);
                ps.setString(7, note);
                ps.addBatch();
            }
            
            ps.executeBatch();
            ps.close();
            conn.commit();
        }
        catch(SQLException ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void deleteExtensions(int geId, Set<Integer> groupIds) throws SQLException
    {
        Connection conn = this.openConnection();
        try
        {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension "
                    + "WHERE geid == ? AND agid == ?");

            for(Integer groupId : groupIds)
            {
                ps.setInt(1, geId);
                ps.setInt(2, groupId);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            conn.commit();
        }
        catch(SQLException ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Map<Integer, ExtensionRecord> getExtensions(int geId, Set<Integer> groupIds) throws SQLException
    {
        Map<Integer, ExtensionRecord> records = new HashMap<Integer, ExtensionRecord>();

        Connection conn = this.openConnection();
        try
        {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT agid, ontime, shiftdates, note, daterecorded, tid"
                    + " FROM extension AS e"
                    + " WHERE e.agid IN ("+ this.groupIDsSetToString(groupIds) +")"
                    + " AND e.geid == ? ");
            ps.setInt(1, geId);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                ExtensionRecord record = new ExtensionRecord(rs.getString("ontime"), rs.getBoolean("shiftdates"),
                        rs.getString("note"), rs.getString("daterecorded"), rs.getInt("tid"));
                records.put(rs.getInt("agid"), record);
            }

            return records;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }

    @Override
    public void setEarned(int groupID, int partID, int taID, Double earned,
                boolean submitted, String dateRecorded) throws SQLException {
        Connection conn = this.openConnection();
        try {
            //database uniqueness constraint ensures that any existing grade
            //for this group will be replaced
            PreparedStatement ps = conn.prepareStatement("INSERT INTO grade "
                + "('agid', 'pid', 'tid', 'earned', 'submitted', 'daterecorded')"
                + " VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setInt(2, partID);
            ps.setInt(3, taID);
            this.setDouble(ps, 4, earned);
            ps.setBoolean(5, submitted);
            ps.setString(6, dateRecorded);
            
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setEarned(int partId, int taId, String dateRecorded, Map<Integer, Pair<Double, Boolean>> earned)
            throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO grade "
                + "('agid', 'pid', 'tid', 'earned', 'submitted', 'daterecorded')"
                + " VALUES (?, ?, ?, ?, ?, ?)");
            
            for (Entry<Integer, Pair<Double, Boolean>> entry : earned.entrySet()) {
                ps.setInt(1, entry.getKey());
                ps.setInt(2, partId);
                ps.setInt(3, taId);
                this.setDouble(ps, 4, entry.getValue().getFirst());
                ps.setBoolean(5, entry.getValue().getSecond());
                ps.setString(6, dateRecorded);
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setEarnedSubmitted(int partId, int taId, String dateRecorded, Map<Integer, Boolean> submitted)
            throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO grade "
                + "('agid', 'pid', 'tid', 'submitted', 'daterecorded')"
                + " VALUES (?, ?, ?, ?, ?)");
            
            for (Entry<Integer, Boolean> entry : submitted.entrySet()) {
                ps.setInt(1, entry.getKey());
                ps.setInt(2, partId);
                ps.setInt(3, taId);
                ps.setBoolean(4, entry.getValue());
                ps.setString(5, dateRecorded);
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public GradeRecord getEarned(int groupID, int partID) throws SQLException {
        GradeRecord record = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT daterecorded, tid, earned, submitted" 
                    + " FROM grade AS g"
                    + " WHERE g.agid == ? AND g.pid == ?");
            ps.setInt(1, groupID);
            ps.setInt(2, partID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                record = new GradeRecord(rs.getString("daterecorded"), rs.getInt("tid"), 
                            this.getDouble(rs, "earned"), rs.getBoolean("submitted"));
            }

            return record;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    private String groupIDsSetToString(Set<Integer> groupIDs) {
        String groupIDString = "";
        Iterator<Integer> groupIter = groupIDs.iterator();
        while (groupIter.hasNext()) {
            groupIDString += "'" + groupIter.next() + "'";
            if (groupIter.hasNext()) {
                groupIDString += ",";
            }
        }
        
        return groupIDString;
    }

    @Override
    public Map<Integer, GradeRecord> getEarned(int partID, Set<Integer> groupIDs) 
                                                        throws SQLException {
        Map<Integer, GradeRecord> records = new HashMap<Integer, GradeRecord>();

        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT agid, daterecorded, tid, earned, submitted"
                    + " FROM grade AS g"
                    + " WHERE g.agid IN ("+ this.groupIDsSetToString(groupIDs) +")"
                    + " AND g.pid == ? ");
            ps.setInt(1, partID);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.put(rs.getInt("agid"), 
                            new GradeRecord(rs.getString("daterecorded"), rs.getInt("tid"),
                                    this.getDouble(rs, "earned"), rs.getBoolean("submitted")));
            }

            return records;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Map<Integer, GradableEventOccurrenceRecord> getGradableEventOccurrences(int geId, Set<Integer> groupIds)
            throws SQLException
    {
        Map<Integer, GradableEventOccurrenceRecord> records = new HashMap<Integer, GradableEventOccurrenceRecord>();

        Connection conn = this.openConnection();
        try
        {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT agid, time, daterecorded, tid"
                    + " FROM geoccurrence AS geo"
                    + " WHERE geo.agid IN (" + this.groupIDsSetToString(groupIds) + ")"
                    + " AND geo.geid == ? ");
            ps.setInt(1, geId);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                GradableEventOccurrenceRecord record = new GradableEventOccurrenceRecord(rs.getInt("tid"),
                        rs.getString("daterecorded"), rs.getString("time"));
                records.put(rs.getInt("agid"), record);
            }

            return records;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setGradableEventOccurrences(int geid, Map<Integer, String> groupsToTime, int tid, String dateRecorded)
            throws SQLException
    {
        Connection conn = this.openConnection();

        try
        {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO geoccurrence (geid, agid, time, dateRecorded, tid) VALUES (?, ?, ?, ?, ?)");

            for(Map.Entry<Integer, String> entry : groupsToTime.entrySet())
            {
                ps.setInt(1, geid);
                ps.setInt(2, entry.getKey());
                ps.setString(3, entry.getValue());
                ps.setString(4, dateRecorded);
                ps.setInt(5, tid);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            conn.commit();
        }
        catch(SQLException e)
        {
            conn.rollback();
            throw e;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }
        
    @Override
    public void deleteGradableEventOccurrences(int geId, Set<Integer> groupIds) throws SQLException
    {
        Connection conn = this.openConnection();
        try
        {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM geoccurrence WHERE geid == ? AND agid == ?");

            for(Integer groupId : groupIds)
            {
                ps.setInt(1, geId);
                ps.setInt(2, groupId);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            conn.commit();
        }
        catch(SQLException ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            this.closeConnection(conn);
        }
    }

    @Override
    public void resetDatabase() throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            //DROP all tables in DB
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS adjustment");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS flag");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS grade");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS distribution");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS exemption");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS extension");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS geoccurrence");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS groupmember");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS asgngroup");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS inclusionfilter");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS actionproperty");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS action");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS part");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS gradableevent");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS assignment");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS blacklist");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS student");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS ta");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS notifyaddresses");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS courseproperties");
            
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
            conn.createStatement().executeUpdate("CREATE TABLE blacklist (sid INTEGER NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT sidtidunique UNIQUE (sid, tid) ON CONFLICT IGNORE)");
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
                    + " FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE)");
            conn.createStatement().executeUpdate("CREATE TABLE action (acid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " pid INTEGER NOT NULL,"
                    + " name VARCHAR NOT NULL,"
                    + " icon VARCHAR NOT NULL,"
                    + " ordering INTEGER NOT NULL,"
                    + " task VARCHAR,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE)");
            conn.createStatement().executeUpdate("CREATE TABLE actionproperty (apid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " acid INTEGER NOT NULL,"
                    + " key VARCHAR NOT NULL,"
                    + " value VARCHAR NOT NULL,"
                    + " FOREIGN KEY (acid) REFERENCES action(acid) ON DELETE CASCADE,"
                    + " CONSTRAINT acidkeyunique UNIQUE (acid, key) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE inclusionfilter (ifid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " pid INTEGER NOT NULL,"
                    + " type VARCHAR NOT NULL,"
                    + " path VARCHAR NOT NULL,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE,"
                    + " CONSTRAINT pidpathunique UNIQUE (pid, path) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE asgngroup (agid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " aid INTEGER NOT NULL,"
                    + " name VARCHAR NOT NULL,"
                    + " FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE,"
                    + " CONSTRAINT aidnameunique UNIQUE (aid, name) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE groupmember (gmid INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " agid INTEGER NOT NULL,"
                    + " sid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE,"
                    + " CONSTRAINT singlemembership UNIQUE (agid, sid) ON CONFLICT ROLLBACK)");
            conn.createStatement().executeUpdate("CREATE TABLE geoccurrence (geid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " time VARCHAR NOT NULL,"
                    + " daterecorded VARCHAR NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT singlehandin UNIQUE (agid, geid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE extension (geid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " ontime VARCHAR NOT NULL,"
                    + " shiftdates INTEGER NOT NULL DEFAULT 0,"
                    + " note TEXT,"
                    + " daterecorded VARCHAR NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT singleextension UNIQUE (agid, geid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE exemption (geid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " note TEXT,"
                    + " daterecorded VARCHAR NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (geid) REFERENCES gradableevent(geid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT singleextension UNIQUE (agid, geid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE distribution (pid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT onegrader UNIQUE (agid, pid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE grade (pid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " earned DOUBLE,"
                    + " daterecorded VARCHAR NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " submitted INTEGER NOT NULL DEFAULT 1,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT onegrade UNIQUE (agid, pid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE flag (pid INTEGER NOT NULL,"
                    + " agid INTEGER NOT NULL,"
                    + " note TEXT,"
                    + " daterecorded VARCHAR NOT NULL,"
                    + " tid INTEGER NOT NULL,"
                    + " FOREIGN KEY (agid) REFERENCES asgngroup(agid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (pid) REFERENCES part(pid) ON DELETE CASCADE,"
                    + " FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + " CONSTRAINT oneflag UNIQUE (agid, pid) ON CONFLICT REPLACE)");
            conn.createStatement().executeUpdate("CREATE TABLE adjustment (aid INTEGER NOT NULL,"
                    + "sid INTEGER NOT NULL,"
                    + "note TEXT,"
                    + "points DOUBLE,"
                    + "tid INTEGER NOT NULL,"
                    + "daterecorded VARCHAR NOT NULL,"
                    + "FOREIGN KEY (aid) REFERENCES assignment(aid) ON DELETE CASCADE,"
                    + "FOREIGN KEY (sid) REFERENCES student(sid) ON DELETE CASCADE,"
                    + "FOREIGN KEY (tid) REFERENCES ta(tid) ON DELETE CASCADE,"
                    + "CONSTRAINT singleadjustment UNIQUE (aid, sid) ON CONFLICT REPLACE)");
            
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    public static void main(String[] argv) throws SQLException {
        Database db = new DatabaseImpl();
        db.resetDatabase();
    }
}