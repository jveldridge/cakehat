package gradesystem.database;

import gradesystem.Allocator;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import gradesystem.rubric.TimeStatus;
import gradesystem.views.shared.ErrorView;
import com.google.common.collect.ArrayListMultimap;
import gradesystem.config.Assignment;
import gradesystem.config.Part;
import gradesystem.config.TA;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sqlite.SQLiteConfig;

/**
 * all DB accessor and mutator methods for cakehat
 * @author alexku
 */
public class DBWrapper implements DatabaseIO {

    private ConnectionProvider _connProvider;

    /**
     * sets DB path to regular location
     */
    public DBWrapper() {
        _connProvider = new ConnectionProvider() {

            public Connection createConnection() throws SQLException {
                Connection c = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    SQLiteConfig config = new SQLiteConfig();
                    config.enforceForeignKeys(true);
                    c = DriverManager.getConnection("jdbc:sqlite:" +
                            Allocator.getPathServices().getDatabaseFile().getAbsolutePath(),
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
    }

    /**
     * takes in a connection provider so that a different DB connection (in memory)
     * can be used.
     * this constructor should only be used for testing the DBWrapper
     * @param cp - a connection provider to connect to a DB
     */
    public DBWrapper(ConnectionProvider cp) {
        _connProvider = cp;
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
    public Collection<String> getTABlacklist(TA ta) throws SQLException {
        ArrayList<String> blackList = new ArrayList<String>();
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT b.sid AS login"
                    + " FROM blacklist AS b"
                    + " WHERE b.tid == ?");
            ps.setString(1, ta.getLogin());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blackList.add(rs.getString("login"));
            }

            return blackList;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO student"
                    + " ('login', 'firstname', 'lastname')"
                    + " VALUES (?, ?, ?)");
            ps.setString(1, studentLogin);
            ps.setString(2, studentFirstName);
            ps.setString(3, studentLastName);
            ps.executeUpdate();

            ps.close();
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Set enabled to 0 for student passed in
     * @param studentLogin - String Student Login
     */
    @Override
    public void disableStudent(String studentLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 0 WHERE login == ?");
            ps.setString(1, studentLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Set enabled to 1 for student passed in
     * @param studentLogin - String Student Login
     */
    @Override
    public void enableStudent(String studentLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 1 WHERE login == ?");
            ps.setString(1, studentLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getAllStudents() throws SQLException {
        HashMap<String, String> result = new HashMap<String, String>();
        Connection conn = this.openConnection();

        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT s.login AS studlogin, "
                    + "s.firstname AS fname, "
                    + "s.lastname AS lname "
                    + "FROM student AS s ");

            while (rs.next()) {
                result.put(rs.getString("studlogin"), rs.getString("fname") + " " + rs.getString("lname"));
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getEnabledStudents() throws SQLException {
        HashMap<String, String> result = new HashMap<String, String>();
        Connection conn = this.openConnection();

        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT s.login AS studlogin, "
                    + "s.firstname AS fname, "
                    + "s.lastname AS lname "
                    + "FROM student AS s "
                    + "WHERE enabled == 1");

            while (rs.next()) {
                result.put(rs.getString("studlogin"), rs.getString("fname") + " " + rs.getString("lname"));
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void blacklistStudents(Collection<String> studentLogins, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO blacklist "
                    + "('sid', 'tid') VALUES (?, ?)");

            for (String login : studentLogins) {
                ps.setString(1, login);
                ps.setString(2, ta.getLogin());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<String> getBlacklistedStudents() throws SQLException {
        Connection conn = this.openConnection();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT b.sid AS studlogin "
                    + "FROM blacklist AS b");
            Collection<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unBlacklistStudents(Collection<String> studentLogins, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM blacklist "
                    + "WHERE tid == ? "
                    + "AND sid == ?");

            for (String studentLogin : studentLogins) {
                ps.setString(1, ta.getLogin());
                ps.setString(2, studentLogin);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean isStudentEnabled(String studentLogin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.enabled FROM student"
                    + " AS s"
                    + " WHERE"
                    + " s.login == ?");
            ps.setString(1, studentLogin);

            ResultSet rs = ps.executeQuery();
            int enabled = rs.getInt("enabled");

            return (enabled == 1);
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<DistributablePart, TA> getGradersForStudent(String studentLogin) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        Map<DistributablePart, TA> graders = new HashMap<DistributablePart, TA>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS login, d.pid AS partID"
                    + " FROM distribution AS d"
                    + " INNER JOIN groupmember AS gm"
                    + " ON d.gpid == gm.gpid"
                    + " WHERE gm.sid == ?");
            ps.setString(1, studentLogin);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String partID = rs.getString("partID");
                if (Allocator.getConfigurationInfo().getDistributablePart(partID) != null) {
                    String taLogin = rs.getString("login");
                    TA ta = Allocator.getConfigurationInfo().getTA(taLogin);
                    if (ta == null) {
                        throw new CakeHatDBIOException("TA with login " + taLogin + " is not in the config file, "
                                + "but is assigned to grade student " + studentLogin + " for "
                                + "assignment " + Allocator.getConfigurationInfo().getDistributablePart(partID).getName() + ".");
                    }
                    graders.put(Allocator.getConfigurationInfo().getDistributablePart(partID), ta);
                } else {
                    throw new CakeHatDBIOException("The DistributablePart: "
                            + Allocator.getConfigurationInfo().getDistributablePart(partID).getName()
                            + " with ID: " + Allocator.getConfigurationInfo().getDistributablePart(partID).getDBID()
                            + " exists in the Database but not in the config file.");
                }
            }

            return graders;
        } finally {
            this.closeConnection(conn);
        }
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
                    + "CONSTRAINT 'nameaidunique' UNIQUE ('aid','name') ON CONFLICT FAIL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'blacklist' ('tid' VARCHAR NOT NULL, "
                    + "'sid' VARCHAR NOT NULL, "
                    + "CONSTRAINT 'tidsidunique' UNIQUE ('tid','sid') ON CONFLICT IGNORE, "
                    + "FOREIGN KEY(sid) REFERENCES student(login));");
            conn.createStatement().executeUpdate("CREATE TABLE 'distribution' ('gpid' INTEGER NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'tid' VARCHAR NOT NULL, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'exemption' ('gpid' INTEGER NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'note' TEXT, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'extension' ('gpid' INTEGER NOT NULL, "
                    + "'aid' VARCHAR NOT NULL, "
                    + "'ontime' INTEGER NOT NULL, "
                    + "'note' TEXT, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'grade' ('pid' VARCHAR NOT NULL, "
                    + "'gpid' INTEGER NOT NULL, "
                    + "'score' DOUBLE NOT NULL, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groupmember' ('gpid' INTEGER NOT NULL, "
                    + "'sid' VARCHAR NOT NULL, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid), "
                    + "FOREIGN KEY(sid) REFERENCES student(login));");
            conn.createStatement().executeUpdate("CREATE TABLE 'handin' ('gpid' INTEGER NOT NULL, "
                    + "'status' VARCHAR NOT NULL, "
                    + "'late' INTEGER NOT NULL, "
                    + "FOREIGN KEY(gpid) REFERENCES asgngroup(gpid) ON DELETE CASCADE);");
            conn.createStatement().executeUpdate("CREATE TABLE 'student' ('login' VARCHAR NOT NULL, "
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
            conn.createStatement().executeUpdate("CREATE INDEX student_login ON student (login);");
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();

            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void setGroups(Assignment asgn, Collection<Group> groups) throws SQLException {
        Connection conn = this.openConnection();

        try {
            conn.setAutoCommit(false);

            // insert all the groups
            PreparedStatement psGroup = conn.prepareStatement("INSERT INTO asgngroup"
                    + " ('name', 'aid') VALUES (?, ?)");
            for (Group group : groups) {
                psGroup.setString(1, group.getName());
                psGroup.setString(2, asgn.getDBID());
                psGroup.addBatch();
            }
            psGroup.executeBatch();
            psGroup.close();

            // add all the members to those groups
            PreparedStatement psMember = conn.prepareStatement("INSERT INTO groupmember "
                    + "('gpid', 'sid') "
                    + "VALUES ((SELECT gpid FROM asgngroup WHERE name==? AND aid==?), ?)");

            for (Group group : groups) {

                for (String student : group.getMembers()) {
                    psMember.setString(1, group.getName());
                    psMember.setString(2, asgn.getDBID());
                    psMember.setString(3, student);
                    psMember.addBatch();
                }
            }
            psMember.executeBatch();
            psMember.close();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();

            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void setGroup(Assignment asgn, Group group) throws SQLException {
        Collection<Group> groups = new ArrayList<Group>(1);
        groups.add(group);
        this.setGroups(asgn, groups);
    }

    @Override
    public Group getStudentsGroup(Assignment asgn, String student) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gm.sid AS studLogin, gp.name as groupName"
                    + " FROM groupmember AS gm"
                    + " INNER JOIN asgngroup AS gp"
                    + " ON gp.gpid == gm.gpid"
                    + " WHERE gp.gpid IN (SELECT gp.gpid"
                    + " FROM asgngroup AS gp"
                    + " INNER JOIN groupmember as gm"
                    + " ON gp.gpid == gm.gpid"
                    + " WHERE gp.aid == ?"
                    + " AND gm.sid == ?)");
            ps.setString(1, asgn.getDBID());
            ps.setString(2, student);
            ResultSet rs = ps.executeQuery();

            Collection<String> loginsForGroup = new ArrayList(5);
            String nameForGroup = null;
            while (rs.next()) {
                loginsForGroup.add(rs.getString("studLogin"));
                nameForGroup = rs.getString("groupName");
            }
            Group group = null;
            if (nameForGroup != null) {
                group = new Group(nameForGroup, loginsForGroup);
            }

            return group;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<Group> getGroupsForAssignment(Assignment asgn) throws SQLException {
        Collection<Group> groups = new ArrayList<Group>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gm.sid AS studLogin, gp.name AS groupName"
                    + " FROM groupmember AS gm"
                    + " INNER JOIN asgngroup AS gp"
                    + " ON gp.gpid == gm.gpid"
                    + " WHERE gp.aid == ?"
                    + " ORDER BY gp.gpid DESC");
            /* ORDER BY is required since the returned records are processed in
             * the order returned. Students are split into groups when the groupName
             * switches. It will switch at the correct point only if the records
             * are sorted.
             */
            ps.setString(1, asgn.getDBID());
            ResultSet rs = ps.executeQuery();

            Collection<String> loginsForGroup = new ArrayList<String>(5);
            String nameForGroup = "";
            while (rs.next()) {
                String groupName = rs.getString("groupName");
                String studentLogin = rs.getString("studLogin");
                if (nameForGroup.equals(groupName)) {
                    loginsForGroup.add(studentLogin);
                }
                else {
                    if (!nameForGroup.equals("")) {
                        groups.add(new Group(nameForGroup, loginsForGroup));
                    }
                    loginsForGroup = new ArrayList<String>(5);
                    nameForGroup = groupName;
                    loginsForGroup.add(studentLogin);
                }
            }
            if (!nameForGroup.equals("")) {
                groups.add(new Group(nameForGroup, loginsForGroup));
            }

            return groups;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeGroup(Assignment asgn, Group group) throws SQLException {
        Connection conn = this.openConnection();

        try {
            conn.setAutoCommit(false);

            int groupID = this.group2groupID(conn, asgn, group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM groupmember"
                    + " WHERE gpid == ?");
            ps.setInt(1, groupID);
            ps.executeUpdate();

            ps = conn.prepareStatement("DELETE FROM group"
                    + " WHERE gpid == ?");
            ps.setInt(1, groupID);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();

            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeGroupsForAssignment(Assignment asgn) throws SQLException {
        Connection conn = this.openConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM groupmember"
                    + " WHERE gpid IN (SELECT gpid"
                    + " FROM asgngroup"
                    + " WHERE aid == ?)");
            ps.setString(1, asgn.getDBID());
            ps.executeUpdate();

            ps = conn.prepareStatement("DELETE FROM asgngroup"
                    + " WHERE aid == ?");
            ps.setString(1, asgn.getDBID());
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();

            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean isDistEmpty(Assignment asgn) throws SQLException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.gpid) AS rowcount"
                    + " FROM distribution AS d"
                    + " WHERE d.pid IN (" + this.asgn2PartIDs(asgn) + ")");

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            ps.close();

            return rows == 0;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<TA, Collection<Group>> getDistribution(DistributablePart part) throws SQLException, CakeHatDBIOException {
        ArrayListMultimap<TA, Group> groups = ArrayListMultimap.create();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS taLogin, gm.sid AS studLogin, gp.name AS groupName"
                    + " FROM groupmember AS gm"
                    + " INNER JOIN distribution AS d"
                    + " ON d.gpid == gm.gpid"
                    + " INNER JOIN asgngroup AS gp"
                    + " ON gp.gpid == d.gpid"
                    + " WHERE d.pid == ?"
                    + " ORDER BY gm.gpid DESC");
            ps.setString(1, part.getDBID());
            ResultSet rs = ps.executeQuery();

            Collection<String> loginsForGroup = new ArrayList<String>(5);
            String currentNameForGroup = "";
            String taLogin = "";
            TA ta = null;
            while (rs.next()) { //while there are more records
                String groupName = rs.getString("groupName");
                String studentLogin = rs.getString("studLogin");
                if (currentNameForGroup.equals(groupName)) { //if we are still populating the same group
                    loginsForGroup.add(studentLogin);
                }
                else { //if it is a new group
                    if (!currentNameForGroup.equals("")) { //if the old group was not the blank group with no name
                        if (ta == null) {
                            throw new CakeHatDBIOException("The TA: " + taLogin + ", exists in the DB but not in the config file.");
                        }
                        Group group = new Group(currentNameForGroup, loginsForGroup); //make a group from the logins and name we have
                        groups.put(ta, group); //add the group to the dist
                    }
                    taLogin = rs.getString("taLogin");
                    ta = Allocator.getConfigurationInfo().getTA(taLogin);
                    loginsForGroup = new ArrayList<String>(5); //make a new list of logins
                    currentNameForGroup = groupName; //update the current name
                    loginsForGroup.add(studentLogin); //add the student to the list
                }
            }
            if (!currentNameForGroup.equals("")) { //we are done with all the records and there were some records
                if (ta == null) {
                    throw new CakeHatDBIOException("The TA: " + taLogin + ", exists in the DB but not in the config file.");
                }
                groups.put(Allocator.getConfigurationInfo().getTA(taLogin), new Group(currentNameForGroup, loginsForGroup)); //make and add a group
            }

            return groups.asMap();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void assignGroupToGrader(Group group, DistributablePart part, TA ta) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        try {
            int groupID = this.group2groupID(conn, part.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.gpid) AS timesAssigned"
                    + " FROM distribution AS d"
                    + " WHERE d.gpid == ?"
                    + " AND d.pid == ?");
            ps.setInt(1, groupID);
            ps.setString(2, part.getDBID());

            ResultSet rs = ps.executeQuery();
            boolean isntAssigned = (rs.getInt("timesAssigned") == 0);

            if (isntAssigned) {
                ps = conn.prepareStatement("INSERT INTO distribution ('gpid', 'tid', 'pid')"
                        + " VALUES (?, ?, ?)");
                ps.setInt(1, groupID);
                ps.setString(2, ta.getLogin());
                ps.setString(3, part.getDBID());
                ps.executeUpdate();
            } else {
                throw new CakeHatDBIOException("The group: " + group
                        + " is already assigned to a TA. You can't assign them to"
                        + " another TA without removing them from the other TA's"
                        + " dist.");
            }
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unassignGroupFromGrader(Group group, DistributablePart part, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {

            int groupID = this.group2groupID(conn, part.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE pid == ?"
                    + " AND gpid == ?"
                    + " AND tid ==  ?");
            ps.setString(1, part.getDBID());
            ps.setInt(2, groupID);
            ps.setString(3, ta.getLogin());

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<Group> getGroupsAssigned(DistributablePart part, TA ta) throws SQLException, CakeHatDBIOException {
        Collection<Group> fromDist = this.getDistribution(part).get(ta);
        if (fromDist != null) {
            return fromDist;
        }

        return Collections.emptyList();
    }

    @Override
    public Collection<Group> getAllAssignedGroups(DistributablePart part) throws SQLException, CakeHatDBIOException {
        ArrayList<Group> groups = new ArrayList<Group>();

        for (Collection<Group> groups4TA : this.getDistribution(part).values()) {
            groups.addAll(groups4TA);
        }

        return groups;
    }

    @Override
    public void grantExtension(Group group, Handin handin, Calendar newDate, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);

            int groupID = this.group2groupID(conn, handin.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE aid == ?"
                    + " AND gpid == ?");
            ps.setString(1, handin.getAssignment().getDBID());
            ps.setInt(2, groupID);
            ps.executeUpdate();

            int ontime = (int) (newDate.getTimeInMillis() / 1000);

            ps = conn.prepareStatement("INSERT INTO extension ('gpid', 'aid', 'ontime', 'note')"
                    + " VALUES (?, ?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, handin.getAssignment().getDBID());
            ps.setInt(3, ontime);
            ps.setString(4, note);
            ps.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            // the exception is caught so any old extension is preserved
            conn.rollback();

            // then the exception is re-thrown to inform the client of the error
            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeExtension(Group group, Handin handin) throws SQLException {
        Connection conn = this.openConnection();
        try {
            int groupID = this.group2groupID(conn, handin.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE aid == ?"
                    + " AND gpid == ?");
            ps.setString(1, handin.getAssignment().getDBID());
            ps.setInt(2, groupID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void grantExemption(Group group, Part part, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);

            int groupID = this.group2groupID(conn, part.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid == ?"
                    + " AND gpid == ?");
            ps.setString(1, part.getDBID());
            ps.setInt(2, groupID);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO exemption ('gpid', 'pid', 'note')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, part.getDBID());
            ps.setString(3, note);
            ps.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            // the exception is caught so any old exemption is preserved
            conn.rollback();

            // then the exception is re-thrown to inform the client of the error
            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeExemption(Group group, Part part) throws SQLException {
        Connection conn = this.openConnection();
        try {
            int groupID = this.group2groupID(conn, part.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid == ?"
                    + " AND gpid == ?");
            ps.setString(1, part.getDBID());
            ps.setInt(2, groupID);
            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Calendar getExtension(Group group, Handin handin) throws SQLException {
        Calendar result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.ontime AS date"
                    + " FROM extension AS x"
                    + " INNER JOIN asgngroup AS g"
                    + " ON g.gpid == x.gpid"
                    + " WHERE x.aid == ?"
                    + " AND g.name == ?"
                    + " AND g.aid == ?");
            ps.setString(1, handin.getAssignment().getDBID());
            ps.setString(2, group.getName());
            ps.setString(3, handin.getAssignment().getDBID());

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
    public Map<Group, Calendar> getAllExtensions(Handin handin) throws SQLException {
        HashMap<Group, Calendar> result = new HashMap<Group, Calendar>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.gpid AS groupID, e.ontime AS date"
                    + " FROM extension AS e"
                    + " WHERE e.aid == ?");
            ps.setString(1, handin.getAssignment().getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(rs.getInt("date") * 1000L);

                PreparedStatement groupPS = conn.prepareStatement("SELECT gm.sid AS login, g.name AS name"
                        + " FROM groupmember AS gm"
                        + " INNER JOIN asgngroup AS g"
                        + " ON gm.gpid == g.gpid"
                        + " WHERE gm.gpid == ?");
                groupPS.setInt(1, rs.getInt("groupID"));

                ResultSet groupRS = groupPS.executeQuery();
                Collection<String> members = new ArrayList<String>(10);
                String name = "";
                while (groupRS.next()) {
                    members.add(groupRS.getString("login"));
                    name = groupRS.getString("name");
                }

                result.put(new Group(name, members), cal);
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public String getExtensionNote(Group group, Handin handin) throws SQLException {
        String result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS extnote"
                    + " FROM extension AS x"
                    + " INNER JOIN asgngroup AS g"
                    + " ON g.gpid == x.gpid"
                    + " WHERE x.aid == ?"
                    + " AND g.name == ?"
                    + " AND g.aid == ?");
            ps.setString(1, handin.getAssignment().getDBID());
            ps.setString(2, group.getName());
            ps.setString(3, handin.getAssignment().getDBID());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("extnote");
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * this is a bad implementation.
     */
    public Map<Part, Collection<Group>> getAllExemptions(Assignment a) throws SQLException{
        HashMap<Part, Collection<Group>> result = new HashMap<Part, Collection<Group>>();

        Collection<Group> groups = null;
        try{
            groups = Allocator.getDatabaseIO().getGroupsForAssignment(a);
        } catch (SQLException ex) {
            new ErrorView(ex);
        }

        for (Part p : a.getParts()){
            for (Group g : groups){
                String note = this.getExemptionNote(g, p);
                if (note != null){
                    if (result.containsKey(p)){
                        result.get(p).add(g);
                    }
                    else{
                        ArrayList<Group> groupList = new ArrayList<Group>();
                        groupList.add(g);
                        result.put(p, groupList);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getExemptionNote(Group group, Part part) throws SQLException {
        String result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS exenote"
                    + " FROM exemption AS x"
                    + " INNER JOIN asgngroup AS g"
                    + " ON g.gpid == x.gpid"
                    + " WHERE x.pid == ?"
                    + " AND g.name == ?"
                    + " AND g.aid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, group.getName());
            ps.setString(3, part.getAssignment().getDBID());

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
    public void enterGrade(Group group, Part part, double score) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);

            int groupID = this.group2groupID(conn, part.getAssignment(), group);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM grade"
                    + " WHERE pid == ?"
                    + " AND gpid == ?");
            ps.setString(1, part.getDBID());
            ps.setInt(2, groupID);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO grade ('gpid', 'pid', 'score')"
                    + " VALUES (?, ?, ?)");
            ps.setInt(1, groupID);
            ps.setString(2, part.getDBID());
            ps.setDouble(3, score);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            //if there was an error, rollback to preserve the old grade
            conn.rollback();

            //then rethrow exception to inform user of the error
            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Double getGroupScore(Group group, Part part) throws SQLException {
        Double grade = null;
        Connection conn = this.openConnection();

        try {
            int groupID = this.group2groupID(conn, part.getAssignment(), group);

            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore"
                    + " FROM grade AS g"
                    + " WHERE g.gpid == ?"
                    + " AND g.pid == ?");
            ps.setInt(1, groupID);
            ps.setString(2, part.getDBID());

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
    public void setDistributablePartDist(Map<DistributablePart, Map<TA, Collection<Group>>> distribution) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        try {

            Map<String, Integer> names2IDs = new HashMap<String, Integer>();

            if (!distribution.isEmpty()) {
                // lookup all group names and their gpids
                PreparedStatement ps = conn.prepareStatement("SELECT gp.gpid AS groupID, gp.name AS groupName"
                        + " FROM asgngroup AS gp"
                        + " WHERE gp.aid == ?");
                ps.setString(1, distribution.keySet().iterator().next().getAssignment().getDBID());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    names2IDs.put(rs.getString("groupName"), rs.getInt("groupID"));
                }
                ps.close();
            } else {
                // if the dist is empty then no reason to do anything
                return;
            }

            // stop committing so that all inserts happen in one FileIO
            conn.setAutoCommit(false);

            PreparedStatement psD = conn.prepareStatement("DELETE FROM distribution WHERE pid == ?");
            for (DistributablePart part : distribution.keySet()) {
                psD.setString(1, part.getDBID());
                psD.addBatch();
            }
            psD.executeBatch();
            psD.close();

            PreparedStatement psI = conn.prepareStatement("INSERT INTO distribution ('pid', 'gpid', 'tid') VALUES (?, ?, ?)");
            for (DistributablePart part : distribution.keySet()) {
                //add the distribution to the DB
                for (TA ta : distribution.get(part).keySet()) {
                    Collection<Group> distributedGroups = distribution.get(part).get(ta);

                    for (Group group : distributedGroups) {
                        psI.setString(1, part.getDBID());
                        //make sure that if a group's gpid can't be looked up that an exception is thrown
                        if (!names2IDs.containsKey(group.getName())) {
                            psD.close();
                            psI.close();
                            conn.rollback();
                            throw new CakeHatDBIOException("could not find the groupID in the database for the group: " + group);
                        }
                        psI.setInt(2, names2IDs.get(group.getName()));
                        psI.setString(3, ta.getLogin());
                        psI.addBatch();
                    }
                }
            }
            psI.executeBatch();
            psI.close();

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

    private int group2groupID(Connection conn, Assignment asgn, Group group) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT gp.gpid AS groupID"
                + " FROM asgngroup AS gp"
                + " WHERE gp.name == ?"
                + " AND gp.aid == ?");
        ps.setString(1, group.getName());
        ps.setString(2, asgn.getDBID());
        ResultSet rs = ps.executeQuery();
        int groupID = rs.getInt("groupID");
        ps.close();
        return groupID;
    }

    @Override
    public Double getGroupAsgnScore(Group group, Assignment asgn) throws SQLException {
        Double grade = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore"
                    + " FROM grade AS g"
                    + " WHERE g.gpid == ?"
                    + " AND g.pid IN (" + this.asgn2PartIDs(asgn) + ")");
            ps.setInt(1, this.group2groupID(conn, asgn, group));

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
    public Map<Group, Double> getPartScoresForGroups(Part part, Iterable<Group> groups) throws SQLException {
        Map<Group, Double> scores = new HashMap<Group, Double>();

        Connection conn = this.openConnection();

        Map<Integer, Group> gpid2Group = new HashMap<Integer, Group>();
        String groupIDs = "";
        for (Group group : groups) {
            Integer gpid = this.group2groupID(conn, part.getAssignment(), group);
            groupIDs += ",'" + gpid + "'";
            gpid2Group.put(gpid, group);
        }
        if (groupIDs.length() > 1) {
            groupIDs = groupIDs.substring(1);
        }


        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, g.gpid AS groupID"
                    + " FROM grade AS g"
                    + " WHERE g.gpid IN (" + groupIDs + ")"
                    + " AND g.pid == '" + part.getDBID() + "'");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.put(gpid2Group.get(rs.getInt("groupID")), rs.getDouble("partscore"));
            }

            return scores;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Group, Double> getAssignmentScoresForGroups(Assignment asgn, Iterable<Group> groups) throws SQLException {
        Map<Group, Double> scores = new HashMap<Group, Double>();

        Connection conn = this.openConnection();

        Map<Integer, Group> gpid2Group = new HashMap<Integer, Group>();
        String groupIDs = "";
        for (Group group : groups) {
            Integer gpid = this.group2groupID(conn, asgn, group);
            groupIDs += ",'" + gpid + "'";
            gpid2Group.put(gpid, group);
        }
        if (groupIDs.length() > 1) {
            groupIDs = groupIDs.substring(1);
        }

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore, g.gpid AS groupID"
                    + " FROM grade AS g"
                    + " WHERE g.gpid IN (" + groupIDs + ")"
                    + " AND g.pid IN (" + this.asgn2PartIDs(asgn) + ")"
                    + " GROUP BY g.gpid");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int groupID = rs.getInt("groupID");
                Double score = rs.getDouble("asgnscore");
                scores.put(gpid2Group.get(groupID), score);
            }

            return scores;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Set<DistributablePart> getDPsWithAssignedStudents(TA ta) throws SQLException {
        Set<DistributablePart> parts = new HashSet<DistributablePart>();

        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.pid AS partID"
                    + " FROM distribution AS d"
                    + " WHERE d.tid == ?");
            ps.setString(1, ta.getLogin());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String partID = rs.getString("partID");
                parts.add(Allocator.getConfigurationInfo().getDistributablePart(partID));
            }

            return parts;
        } finally {
            this.closeConnection(conn);
        }
    }

    public TA getGraderForGroup(DistributablePart part, Group group) throws SQLException, CakeHatDBIOException {
        return this.getGradersForStudent(group.getMembers().iterator().next()).get(part);
    }

    @Override
    public HandinStatus getHandinStatus(Handin handin, Group group) throws SQLException {
        Connection conn = this.openConnection();

        try {
            int groupID = this.group2groupID(conn, handin.getAssignment(), group);
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

    private String asgn2PartIDs(Assignment asgn) {
        //make a list of all the parts for the assignment so that the sum can be made.
        //  grade only containts part IDs not asgn IDs
        String parts = "";
        for (Part p : asgn.getParts()) {
            parts += ",'" + p.getDBID() + "'";
        }
        if (parts.length() > 1) {
            parts = parts.substring(1);
        }
        return parts;
    }

    public void setHandinStatus(Handin handin, Group group, HandinStatus status) throws SQLException {
        Map<Group, HandinStatus> statuses = new HashMap<Group, HandinStatus>();
        statuses.put(group, status);
        this.setHandinStatuses(handin, statuses);
    }

    public void setHandinStatuses(Handin handin, Map<Group, HandinStatus> statuses) throws SQLException {
        Connection conn = this.openConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement psDelete = conn.prepareStatement("DELETE FROM handin"
                    + " WHERE gpid == ?");
            PreparedStatement psInsert = conn.prepareStatement("INSERT INTO handin ('gpid', 'status', 'late')"
                    + " VALUES (?, ?, ?)");
            for (Group group : statuses.keySet()) {
                int groupID = this.group2groupID(conn, handin.getAssignment(), group);
                
                psDelete.setInt(1, groupID);
                psDelete.addBatch();

                psInsert.setInt(1, groupID);
                psInsert.setString(2, statuses.get(group).getTimeStatus().name());
                psInsert.setInt(3, statuses.get(group).getDaysLate());
                psInsert.addBatch();
            }
            psDelete.executeBatch();
            psInsert.executeBatch();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();

            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    public boolean areHandinStatusesSet(Handin handin) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS numStatuses" +
                    " FROM handin AS h INNER JOIN asgngroup AS ag" +
                    " ON h.gpid == ag.gpid WHERE ag.aid == ?;");
            ps.setString(1, handin.getAssignment().getDBID());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new CakeHatDBIOException("Could not read number of statuses for assignment " +
                                               handin.getAssignment() + " from the database.");
            }

            return rs.getInt("numStatuses") != 0;
        } finally {
            this.closeConnection(conn);
        }
    }
}
