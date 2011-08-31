package cakehat.database;

import cakehat.Allocator;
import cakehat.CakehatMain;
import cakehat.rubric.TimeStatus;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ArrayListMultimap;
import cakehat.config.Assignment;
import cakehat.services.ServicesException;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sqlite.SQLiteConfig;

/**
 * Implementation of {@link Database}.
 * 
 * @author aunger
 * @author jeldridg
 */
public class DatabaseImpl implements Database {

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
                Connection c = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    SQLiteConfig config = new SQLiteConfig();
                    config.enforceForeignKeys(true);
                    c = DriverManager.getConnection("jdbc:sqlite:"
                            + dbFile.getAbsolutePath(),
                            config.toProperties());
                } catch (ClassNotFoundException e) {
                    new ErrorView(e, "Could not open a connection to the DB.");
                }
                return c;
            }

            public void closeConnection(Connection c) throws SQLException {
                if (c != null) {
                    if (!c.getAutoCommit()) {
                        c.commit();
                    }
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
                System.err.println("cakehat is unable to create a database. " +
                        "Underlying reason: ");
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
                System.err.println("cakehat is unable to create a database. " +
                        "Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }
            catch(ServicesException ex)
            {
                System.err.println("cakehat is unable to create a database. " +
                        "Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }
            catch(IOException ex)
            {
                System.err.println("cakehat is unable to create a database. " +
                        "Underlying reason: ");
                ex.printStackTrace();
                System.exit(-1);
            }

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
        }
    }

    /**
     * opens a new connection to the DB
     */
    private Connection openConnection() throws SQLException {
        return _connProvider.createConnection();
    }

    /**
     * closes current connection to DB
     */
    private void closeConnection(Connection c) throws SQLException {
        _connProvider.closeConnection(c);
    }
    
    @Override
    public int addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO student"
                    + " ('login', 'firstname', 'lastname')"
                    + " VALUES (?, ?, ?)");
            ps.setString(1, studentLogin);
            ps.setString(2, studentFirstName);
            ps.setString(3, studentLastName);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            int id = rs.getInt(1);
            
            ps.close();
            return id;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void disableStudent(int studentID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 0 WHERE sid == ?");
            ps.setInt(1, studentID);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void enableStudent(int studentID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 1 WHERE sid == ?");
            ps.setInt(1, studentID);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<StudentRecord> getAllStudents() throws SQLException {
        Connection conn = this.openConnection();
        try {
            Collection<StudentRecord> result = new ArrayList<StudentRecord>();
            
            ResultSet rs = conn.createStatement().executeQuery("SELECT s.sid AS sid, "
                    + "s.login AS login, "
                    + "s.firstname AS fname, "
                    + "s.lastname AS lname, "
                    + "s.enabled AS enabled "
                    + "FROM student AS s ");

            while (rs.next()) {
                result.add(new StudentRecord(rs.getInt("sid"),
                                             rs.getString("login"),
                                             rs.getString("fname"),
                                             rs.getString("lname"),
                                             rs.getBoolean("enabled")));
            }

            return Collections.unmodifiableCollection(result);
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void blacklistStudents(Collection<Integer> studentIDs, String taLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO blacklist "
                    + "('sid', 'tid') VALUES (?, ?)");

            for (Integer studentID : studentIDs) {
                ps.setInt(1, studentID);
                ps.setString(2, taLogin);
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
    public void unBlacklistStudents(Collection<Integer> studentIDs, String taLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM blacklist "
                    + "WHERE tid == ? "
                    + "AND sid == ?");

            for (Integer studentID : studentIDs) {
                ps.setString(1, taLogin);
                ps.setInt(2, studentID);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<Integer> getBlacklistedStudents() throws SQLException {
        Connection conn = this.openConnection();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT b.sid AS sid "
                    + "FROM blacklist AS b");
            Collection<Integer> result = new ArrayList<Integer>();
            while (rs.next()) {
                result.add(rs.getInt("sid"));
            }
            return result;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Collection<Integer> getBlacklist(String taLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            ArrayList<Integer> blackList = new ArrayList<Integer>();
            
            PreparedStatement ps = conn.prepareStatement("SELECT b.sid AS sid"
                    + " FROM blacklist AS b"
                    + " WHERE b.tid == ?");
            ps.setString(1, taLogin);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blackList.add(rs.getInt("sid"));
            }
            ps.close();

            return blackList;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Collection<GroupRecord> getAllGroups() throws SQLException {
        Connection conn = this.openConnection();
        try {
            Collection<GroupRecord> result = new ArrayList<GroupRecord>();
            
            PreparedStatement ps = conn.prepareStatement("SELECT gp.gpid as gpid, gm.sid AS sid,"
                    + " gp.name AS groupName, gp.aid AS aid"
                    + " FROM groupmember AS gm"
                    + " INNER JOIN asgngroup AS gp"
                    + " ON gm.gpid == gp.gpid"
                    + " ORDER BY gp.gpid");
            
            ResultSet rs = ps.executeQuery();

            int prevGroupId = 0;
            String groupAsgnId = null;
            String groupName = null;
            Collection<Integer> memberIDs = new ArrayList<Integer>();
            while (rs.next()) { //while there are more records
                int currGroupId = rs.getInt("gpid");
                
                if (currGroupId != prevGroupId) {   //current row represents the beginning of a new group
                    if (prevGroupId != 0) {
                        //create record for previous group
                        result.add(new GroupRecord(prevGroupId, groupAsgnId, groupName, memberIDs));
                    }
                    
                    memberIDs.clear();
                    prevGroupId = currGroupId;
                    groupAsgnId = rs.getString("aid");
                    groupName = rs.getString("groupName");
                    memberIDs.add(rs.getInt("sid"));
                }
                else {                              //current row represents an additional member of the same group
                    memberIDs.add(rs.getInt("sid"));
                }
            }
            //create record for last group
            if (prevGroupId != 0) {
                result.add(new GroupRecord(prevGroupId, groupAsgnId, groupName, memberIDs));
            }

            return Collections.unmodifiableCollection(result);
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public GroupRecord addGroup(NewGroup group) throws SQLException, CakeHatDBIOException {
        return Iterables.get(this.addGroups(Arrays.asList(group)), 0);
    }

    @Override
    public Collection<GroupRecord> addGroups(Collection<NewGroup> groups) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        try {
            Collection<GroupRecord> result = new ArrayList<GroupRecord>(groups.size());
            conn.setAutoCommit(false);
            
            // create new List of NewGroup objects to guarantee consistent iteration order
            List<NewGroup> groupList = new ArrayList<NewGroup>(groups);
            
            // check that no students in the groups to be added already have groups
            // for the assignment for which the group is being created
            ArrayListMultimap<Assignment, Student> studentsToCheck = ArrayListMultimap.create();
            for (NewGroup group : groupList) {
                studentsToCheck.putAll(group.getAssignment(), group.getMembers());
            }
            
            int numConflicts = 0;
            for (Assignment asgn : studentsToCheck.keySet()) {
                StringBuilder sids = new StringBuilder();
                for (Student student : studentsToCheck.get(asgn)) {
                    sids.append(", ").append(student.getDbId());
                }
                String sidString = sids.toString();
                if (!sidString.isEmpty()) {
                    sidString = sidString.substring(1);
                }
                
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(gp.gpid) AS numConflicts"
                        + " FROM asgngroup AS gp"
                        + " INNER JOIN groupmember AS gm"
                        + " ON gp.gpid == gm.gpid"
                        + " WHERE gm.sid IN (" + sidString + ")"
                        + " AND gp.aid == ?");
                ps.setString(1, asgn.getDBID());
                
                ResultSet rs = ps.executeQuery();
                rs.next();
                numConflicts += rs.getInt("numConflicts");
            }

            if (numConflicts > 0) {
                throw new CakeHatDBIOException("A student may not be in more than one group "
                        + "for a given assignment.  No groups have been added.");
            }
            
            // insert all the groups
            PreparedStatement psGroup = conn.prepareStatement("INSERT INTO asgngroup"
                    + " ('name', 'aid') VALUES (?, ?)");
            for (NewGroup group : groupList) {
                psGroup.setString(1, group.getName());
                psGroup.setString(2, group.getAssignment().getDBID());
                psGroup.addBatch();
            }
            psGroup.executeBatch();
            
            // get IDs of newly inserted groups
            List<Integer> groupIDs = new ArrayList<Integer>(groupList.size());
            
            // only the last inserted key is returned
            ResultSet rs = psGroup.getGeneratedKeys();
            if (rs.next()) {
                int lastID = rs.getInt(1);
                for (int id = lastID - groupList.size() + 1; id <= lastID; id++) {
                    groupIDs.add(id);
                }
            }
            psGroup.close();

            // add all the members to those groups
            PreparedStatement psMember = conn.prepareStatement("INSERT INTO groupmember "
                    + "('gpid', 'sid') "
                    + "VALUES (?, ?)");

            for (int i = 0; i < groupList.size(); i++) {
                for (Student student : groupList.get(i).getMembers()) {
                    psMember.setInt(1, groupIDs.get(i));
                    psMember.setInt(2, student.getDbId());
                    psMember.addBatch();
                }
            }
            psMember.executeBatch();
            psMember.close();

            conn.commit();
            
            // create GroupRecord objects to return
            for (int i = 0; i < groupList.size(); i++) {
                NewGroup group = groupList.get(i);
                Collection<Integer> memberIDs = new ArrayList<Integer>(group.size());
                for (Student member : group.getMembers()) {
                    memberIDs.add(member.getDbId());
                }
                
                result.add(new GroupRecord(groupIDs.get(i),
                                           group.getAssignment().getDBID(),
                                           group.getName(),
                                           memberIDs));
            }
            
            return result;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public int getGroup(String asgnID, int studentID) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gp.gpid AS gpid"
                    + " FROM asgngroup AS gp"
                    + " INNER JOIN groupmember AS gm"
                    + " ON gp.gpid == gm.gpid "
                    + " WHERE gm.sid == ?"
                    + " AND gp.aid == ?");
            ps.setInt(1, studentID);
            ps.setString(2, asgnID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("gpid");
            }
            return 0;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<Integer> getGroups(String asgnID) throws SQLException {
        Collection<Integer> groupIDs = new ArrayList<Integer>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gp.gpid AS gpid"
                    + " FROM asgngroup as gp"
                    + " WHERE gp.aid == ?");
            ps.setString(1, asgnID);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groupIDs.add(rs.getInt("gpid"));
            }

            return groupIDs;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeGroup(int groupID) throws SQLException {
        Connection conn = this.openConnection();

        try {
            //only need to delete from asgngroup table;
            //foreign key reference in groupmember table will cause cascading
            //to delete groupmember entries as needed
            PreparedStatement ps = conn.prepareStatement("DELETE FROM asgngroup"
                    + " WHERE gpid == ?");
            ps.setInt(1, groupID);
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeGroups(String asgnID) throws SQLException {
        Connection conn = this.openConnection();

        try {
            //only need to delete from asgngroup table--see comment above
            PreparedStatement ps = conn.prepareStatement("DELETE FROM asgngroup"
                    + " WHERE aid == ?");
            ps.setString(1, asgnID);
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public boolean isDistEmpty(Iterable<String> partIDs) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.gpid) AS rowcount"
                    + " FROM distribution AS d"
                    + " WHERE d.pid IN (" + this.partIDsIterableToString(partIDs) + ")");

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            ps.close();

            return rows == 0;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Collection<Integer>> getDistribution(String dpID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            ArrayListMultimap<String, Integer> groups = ArrayListMultimap.create();

            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS taLogin, d.gpid AS gpid"
                    + " FROM distribution AS d"
                    + " WHERE d.pid == ?");
            ps.setString(1, dpID);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                groups.put(rs.getString("taLogin"), rs.getInt("gpid"));
            }
            
            return groups.asMap();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public void setDistribution(Map<String, Map<String, Collection<Integer>>> distribution) throws SQLException {
        Connection conn = this.openConnection();
        
        try {
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement("INSERT INTO distribution ('pid', 'gpid', 'tid') VALUES (?, ?, ?)");
            for (String partID : distribution.keySet()) {
                for (String taLogin : distribution.get(partID).keySet()) {
                    for (Integer groupID : distribution.get(partID).get(taLogin)) {
                        ps.setString(1, partID);
                        ps.setInt(2, groupID);
                        ps.setString(3, taLogin);
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
    public void assignGroup(int groupID, String partID, String taLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO distribution ('gpid', 'tid', 'pid')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, taLogin);
            ps.setString(3, partID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unassignGroup(int groupID, String partID, String taLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE pid == ?"
                    + " AND gpid == ?"
                    + " AND tid ==  ?");
            ps.setString(1, partID);
            ps.setInt(2, groupID);
            ps.setString(3, taLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<Integer> getAssignedGroups(String partID, String taLogin) throws SQLException {
        Collection<Integer> fromDist = this.getDistribution(partID).get(taLogin);
        if (fromDist != null) {
            return fromDist;
        }

        return Collections.emptyList();
    }

    @Override
    public Collection<Integer> getAssignedGroups(String partID) throws SQLException {
        ArrayList<Integer> groups = new ArrayList<Integer>();

        for (Collection<Integer> groups4TA : this.getDistribution(partID).values()) {
            groups.addAll(groups4TA);
        }

        return groups;
    }
    
    @Override
    public Set<String> getDPsWithAssignedGroups(String taLogin) throws SQLException {
        Set<String> partIDs = new HashSet<String>();

        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.pid AS partID"
                    + " FROM distribution AS d"
                    + " WHERE d.tid == ?");
            ps.setString(1, taLogin);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String partID = rs.getString("partID");
                partIDs.add(partID);
            }

            return partIDs;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public String getGrader(String partID, int groupID) throws SQLException {
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS taLogin"
                    + " FROM distribution AS d"
                    + " WHERE d.pid==? AND d.gpid==?");
            ps.setString(1, partID);
            ps.setInt(2, groupID);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("taLogin");
            }
            return null;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void grantExtension(int groupID, String asgnID, Calendar newDate, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            int ontime = (int) (newDate.getTimeInMillis() / 1000);

            //database uniqueness constraint ensures that any existing exemption
            //for this group will be replaced
            PreparedStatement ps = conn.prepareStatement("INSERT INTO extension ('gpid', 'aid', 'ontime', 'note')"
                    + " VALUES (?, ?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, asgnID);
            ps.setInt(3, ontime);
            ps.setString(4, note);
            
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeExtension(int groupID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE extension.gpid == ?");
            ps.setInt(1, groupID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Calendar getExtension(int groupID) throws SQLException {
        Calendar result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.ontime AS date"
                    + " FROM extension AS x"
                    + " WHERE x.gpid == ?");
            ps.setInt(1, groupID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new GregorianCalendar();
                result.setTimeInMillis(rs.getInt("date") * 1000L);
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Integer, Calendar> getExtensions(String asgnID) throws SQLException {
        HashMap<Integer, Calendar> result = new HashMap<Integer, Calendar>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.gpid AS groupID, e.ontime AS date"
                    + " FROM extension AS e"
                    + " WHERE e.aid == ?");
            ps.setString(1, asgnID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int groupID = rs.getInt("groupID");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(rs.getInt("date") * 1000L);

                result.put(groupID, cal);
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public String getExtensionNote(int groupID) throws SQLException {
        String result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS extnote"
                    + " FROM extension AS x"
                    + " WHERE x.gpid == ?");
            ps.setInt(1, groupID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("extnote");
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void grantExemption(int groupID, String partID, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            //database uniqueness constraint ensures that any existing exemption
            //for this group will be replaced
            PreparedStatement ps = conn.prepareStatement("INSERT INTO exemption ('gpid', 'pid', 'note')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, partID);
            ps.setString(3, note);
            
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeExemption(int groupID, String partID) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE gpid == ? AND pid == ?");
            ps.setInt(1, groupID);
            ps.setString(2, partID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public Set<Integer> getExemptions(String partID) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.gpid AS gpid"
                    + " FROM exemption AS e"
                    + " WHERE e.pid == ?");
            ps.setString(1, partID);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("gpid"));
            }
        } finally {
            this.closeConnection(conn);
        }
        
        return result;
    }

    @Override
    public String getExemptionNote(int groupID, String partID) throws SQLException {
        String result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS exenote"
                    + " FROM exemption AS x"
                    + " WHERE x.gpid == ?"
                    + " AND x.pid == ?");
            ps.setInt(1, groupID);
            ps.setString(2, partID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("exenote");
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void enterGrade(int groupID, String partID, double score) throws SQLException {
        Connection conn = this.openConnection();
        try {
            //database uniqueness constraint ensures that any existing grade
            //for this group will be replaced
            PreparedStatement ps = conn.prepareStatement("INSERT INTO grade ('gpid', 'pid', 'score')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, partID);
            ps.setDouble(3, score);
            
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Double getPartScore(int groupID, String partID) throws SQLException {
        Double grade = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore"
                    + " FROM grade AS g"
                    + " WHERE g.gpid == ?"
                    + " AND g.pid == ?");
            ps.setInt(1, groupID);
            ps.setString(2, partID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                grade = rs.getDouble("partscore");
            }

            return grade;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Double getScore(int groupID, Iterable<String> partIDs) throws SQLException {
        Double grade = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore"
                    + " FROM grade AS g"
                    + " WHERE g.gpid == ?"
                    + " AND g.pid IN (" + this.partIDsIterableToString(partIDs) + ")");
            ps.setInt(1, groupID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                grade = rs.getDouble("asgnscore");
            }

            return grade;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Integer, Double> getPartScores(String partID, Iterable<Integer> groupIDs) throws SQLException {
        Map<Integer, Double> scores = new HashMap<Integer, Double>();

        Connection conn = this.openConnection();

        String groupIDString = "";
        Iterator<Integer> groupIter = groupIDs.iterator();
        while (groupIter.hasNext()) {
            groupIDString += "'" + groupIter.next() + "'";
            if (groupIter.hasNext()) {
                groupIDString += ",";
            }
        }

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, g.gpid AS groupID"
                    + " FROM grade AS g"
                    + " WHERE g.gpid IN (" + groupIDString + ")"
                    + " AND g.pid == '" + partID + "'");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.put(rs.getInt("groupID"), rs.getDouble("partscore"));
            }

            return scores;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Integer, Double> getScores(Iterable<String> partIDs, Iterable<Integer> groupIDs) throws SQLException {
        Map<Integer, Double> scores = new HashMap<Integer, Double>();

        Connection conn = this.openConnection();

        String groupIDString = "";
        for (int groupID : groupIDs) {
            groupIDString += ",'" + groupID + "'";
        }
        if (groupIDString.length() > 1) {
            groupIDString = groupIDString.substring(1);
        }

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore, g.gpid AS groupID"
                    + " FROM grade AS g"
                    + " WHERE g.gpid IN (" + groupIDString + ")"
                    + " AND g.pid IN (" + this.partIDsIterableToString(partIDs)
                    + ")"
                    + " GROUP BY g.gpid");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.put(rs.getInt("groupID"), rs.getDouble("asgnscore"));
            }

            return scores;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void setHandinStatus(int groupID, HandinStatus status) throws SQLException {
        Map<Integer, HandinStatus> statuses = new HashMap<Integer, HandinStatus>();
        statuses.put(groupID, status);
        this.setHandinStatuses(statuses);
    }

    @Override
    public void setHandinStatuses(Map<Integer, HandinStatus> statuses) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO handin ('gpid', 'status', 'late')"
                    + " VALUES (?, ?, ?)");
            for (int groupID : statuses.keySet()) {              
                ps.setInt(1, groupID);
                ps.setString(2, statuses.get(groupID).getTimeStatus().name());
                ps.setInt(3, statuses.get(groupID).getDaysLate());
                ps.addBatch();
            }
            
            ps.executeBatch();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean areHandinStatusesSet(String asgnID) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS numStatuses" +
                    " FROM handin AS h INNER JOIN asgngroup AS ag" +
                    " ON h.gpid == ag.gpid WHERE ag.aid == ?;");
            ps.setString(1, asgnID);
            ResultSet rs = ps.executeQuery();

            return rs.getInt("numStatuses") != 0;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    @Override
    public HandinStatus getHandinStatus(int groupID) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT h.status AS status, h.late AS late"
                    + " FROM handin AS h"
                    + " WHERE h.gpid == ?");
            ps.setInt(1, groupID);
            ResultSet rs = ps.executeQuery();

            TimeStatus status = null;
            Integer daysLate = null;
            if (rs.next()) {
                status = TimeStatus.valueOf(rs.getString("status"));
                daysLate = rs.getInt("late");
            }

            if (status == null && daysLate == null) {
                return null;
            }
            return new HandinStatus(status, daysLate);
        } finally {
            this.closeConnection(conn);
        }
    }

    private String partIDsIterableToString(Iterable<String> partIDs) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = partIDs.iterator();
        while (iterator.hasNext()) {
            builder.append('\'').append(iterator.next()).append('\'');
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        
        return builder.toString();
    }
    
    @Override
    public void resetDatabase() throws SQLException {
        Connection conn = this.openConnection();
        try {

            //DROP all tables in DB
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'blacklist';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'distribution';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'exemption';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'extension';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'grade';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'groupmember';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'handin';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'student';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'asgngroup';");

            //CREATE all DB tables
            conn.createStatement().executeUpdate("CREATE TABLE 'asgngroup' ('gpid' INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "'name' VARCHAR NOT NULL, "
                    + "'aid' VARCHAR NOT NULL, "
                    + "CONSTRAINT 'nameaidunique' UNIQUE ('aid','name') ON CONFLICT ROLLBACK);");
            conn.createStatement().executeUpdate("CREATE TABLE 'blacklist' ('tid' VARCHAR NOT NULL, "
                    + "'sid' INTEGER NOT NULL, "
                    + "CONSTRAINT 'tidsidunique' UNIQUE ('tid','sid') ON CONFLICT IGNORE, "
                    + "FOREIGN KEY(sid) REFERENCES student(sid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'distribution' ('gpid' INTEGER NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'tid' VARCHAR NOT NULL, "
                    + "CONSTRAINT 'onegrader' UNIQUE ('gpid', 'pid') ON CONFLICT REPLACE, "
                    + "CONSTRAINT 'rowsunique' UNIQUE ('gpid', 'pid', 'tid') ON CONFLICT IGNORE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'exemption' ('gpid' INTEGER NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'note' TEXT, "
                    + "CONSTRAINT 'gpidpidunique' UNIQUE ('gpid', 'pid') ON CONFLICT REPLACE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'extension' ('gpid' INTEGER NOT NULL, "
                    + "'aid' VARCHAR NOT NULL, "
                    + "'ontime' INTEGER NOT NULL, "
                    + "'note' TEXT, "
                    + "CONSTRAINT 'gpidaidunique' UNIQUE ('gpid', 'aid') ON CONFLICT REPLACE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'grade' ('pid' VARCHAR NOT NULL, "
                    + "'gpid' INTEGER NOT NULL, "
                    + "'score' DOUBLE NOT NULL, "
                    + "CONSTRAINT 'gpidpidunique' UNIQUE ('gpid', 'pid') ON CONFLICT REPLACE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groupmember' ('gpid' INTEGER NOT NULL, "
                    + "'sid' INTEGER NOT NULL, "
                    + "CONSTRAINT 'gpidsidunique' UNIQUE ('gpid', 'sid') ON CONFLICT IGNORE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE, "
                    + "FOREIGN KEY(sid) REFERENCES student(sid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'handin' ('gpid' INTEGER NOT NULL, "
                    + "'status' VARCHAR NOT NULL, "
                    + "'late' INTEGER NOT NULL, "
                    + "CONSTRAINT 'gpidunique' UNIQUE ('gpid') ON CONFLICT REPLACE, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'student' ('sid' INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "'login' VARCHAR NOT NULL, "
                    + "'firstname' VARCHAR NOT NULL, "
                    + "'lastname' VARCHAR NOT NULL, "
                    + "'enabled' INTEGER NOT NULL DEFAULT 1, "
                    + "CONSTRAINT 'studentunique' UNIQUE ('login') ON CONFLICT IGNORE);");

            //CREATE all tables indices
            conn.createStatement().executeUpdate("CREATE INDEX blacklist_student ON blacklist (sid);");
            conn.createStatement().executeUpdate("CREATE INDEX blacklist_ta ON blacklist (tid);");
            conn.createStatement().executeUpdate("CREATE INDEX dist_pid ON distribution (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX dist_tid ON distribution (tid);");
            conn.createStatement().executeUpdate("CREATE INDEX exemp_pid ON exemption (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX exemp_gpid ON exemption (gpid);");
            conn.createStatement().executeUpdate("CREATE INDEX exten_aid ON extension (aid);");
            conn.createStatement().executeUpdate("CREATE INDEX exten_gpid ON extension (gpid);");
            conn.createStatement().executeUpdate("CREATE INDEX grade_pid ON grade (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX grade_gpid ON grade (gpid);");
            conn.createStatement().executeUpdate("CREATE INDEX handin_gpid ON handin (gpid);");
            conn.createStatement().executeUpdate("CREATE INDEX student_login ON student (sid);");
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();

            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }
    
    public static void main(String[] argv) throws SQLException {
        Database db = new DatabaseImpl();
        db.resetDatabase();
    }

}