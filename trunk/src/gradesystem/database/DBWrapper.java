package gradesystem.database;

import gradesystem.Allocator;
import gradesystem.views.shared.ErrorView;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

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
                    c = DriverManager.getConnection("jdbc:sqlite:" + Allocator.getCourseInfo().getDatabaseFilePath());
                } catch (ClassNotFoundException e) {
                    new ErrorView(e, "Could not open a connection to the DB.");
                }
                return c;
            }

            public void closeConnection(Connection c) throws SQLException {
                if (c != null) {
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
    public void setAsgnDist(HandinPart part, Map<TA, Collection<String>> distribution) throws SQLException {
        //add the distribution to the DB
        Connection conn = this.openConnection();
        try {
            // stop committing so that all inserts happen in one FileIO
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution WHERE pid == ?");
            ps.setString(1, part.getDBID());
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO distribution ('pid', 'sid', 'tid')"
                    + " VALUES (?, ?, ?)");
            for (TA ta : distribution.keySet()) {
                Collection<String> distributedStudents = distribution.get(ta);

                for (String student : distributedStudents) {
                    ps.setString(1, part.getDBID());
                    ps.setString(2, student);
                    ps.setString(3, ta.getLogin());
                    ps.addBatch();
                }
            }
            ps.executeBatch();

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
    public Collection<String> getTABlacklist(TA ta) throws SQLException {
        ArrayList<String> blackList = new ArrayList<String>();
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT b.sid AS login"
                    + "FROM blacklist AS b "
                    + "WHERE b.tid == ?");
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
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(s.login) AS rowcount "
                    + "FROM student AS s "
                    + "WHERE s.login == ?");
            ps.setString(1, studentLogin);

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            if (rows != 0) {
                return false;
            }

            ps = conn.prepareStatement("INSERT INTO student "
                    + "('login', 'firstname', 'lastname') "
                    + "VALUES (?, ?, ?)");
            ps.setString(1, studentLogin);
            ps.setString(2, studentFirstName);
            ps.setString(3, studentLastName);
            ps.executeUpdate();

            return true;
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
    public boolean blacklistStudent(String studentLogin, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(b.sid) AS timesOnBlacklist"
                    + " FROM blacklist AS b"
                    + " WHERE b.sid == ?"
                    + " AND  b.tid == ?");
            ps.setString(1, studentLogin);
            ps.setString(2, ta.getLogin());

            ResultSet rs = ps.executeQuery();
            boolean isBlacklisted = (rs.getInt("timesOnBlacklist") != 0);

            if (!isBlacklisted) {
                ps = conn.prepareStatement("INSERT INTO blacklist "
                        + "('sid', 'tid') VALUES (?, ?)");
                ps.setString(1, studentLogin);
                ps.setString(2, ta.getLogin());
                ps.executeUpdate();
                
                return true;
            }

            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean isDistEmpty(HandinPart part) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.sid) AS rowcount"
                    + " FROM distribution AS d"
                    + " WHERE d.pid == ?");
            ps.setString(1, part.getDBID());

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            return rows == 0;
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
    public Collection<String> getStudentsAssigned(HandinPart part, TA ta) throws SQLException {
        ArrayList<String> result = new ArrayList<String>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.sid AS studlogin"
                    + " FROM distribution AS d"
                    + " WHERE d.tid == ?"
                    + " AND d.pid == ?");
            ps.setString(1, ta.getLogin());
            ps.setString(2, part.getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<String> getAllAssignedStudents(HandinPart part) throws SQLException {
        Connection conn = this.openConnection();
        Collection<String> assignedStudents = new LinkedList<String>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.sid AS login"
                    + " FROM distribution AS d"
                    + " WHERE d.pid == ?");
            ps.setString(1, part.getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                assignedStudents.add(rs.getString("login"));
            }

            return assignedStudents;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void assignStudentToGrader(String studentLogin, HandinPart part, TA ta) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.sid) AS timesAssigned"
                    + " FROM distribution AS d"
                    + " WHERE d.sid == ?"
                    + " AND d.pid == ?");
            ps.setString(1, studentLogin);
            ps.setString(2, part.getDBID());

            ResultSet rs = ps.executeQuery();
            boolean isAssigned = (rs.getInt("timesAssigned") != 0);

            if (!isAssigned) {
                ps = conn.prepareStatement("INSERT INTO distribution ('sid', 'tid', 'pid')"
                        + " VALUES (?, ?, ?)");
                ps.setString(1, studentLogin);
                ps.setString(2, ta.getLogin());
                ps.setString(3, part.getDBID());
                ps.executeUpdate();
            } else {
                throw new CakeHatDBIOException("The student: " + studentLogin
                        + " is already assigned to a TA. You can't assign them to"
                        + " another TA without removing them from the other TA's"
                        + " dist.");
            }
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void unassignStudentFromGrader(String studentLogin, HandinPart part, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE pid == ?"
                    + " AND sid == ?"
                    + " AND tid ==  ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);
            ps.setString(3, ta.getLogin());

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void grantExtension(String studentLogin, Part part, Calendar newDate, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);
            ps.executeUpdate();

            int ontime = (int) (newDate.getTimeInMillis() / 1000);

            ps = conn.prepareStatement("INSERT INTO extension ('sid', 'pid', 'ontime', 'note')"
                    + " VALUES (?, ?, ?, ?)");
            ps.setString(1, studentLogin);
            ps.setString(2, part.getDBID());
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
    public void grantExemption(String studentLogin, Part part, String note) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO exemption ('sid', 'pid', 'note')"
                    + " VALUES (?, ?, ?)");
            ps.setString(1, studentLogin);
            ps.setString(2, part.getDBID());
            ps.setString(3, note);
            ps.executeUpdate();

        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Calendar> getExtensions(Part part) throws SQLException {
        HashMap<String, Calendar> result = new HashMap<String, Calendar>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.sid AS studlogin, e.ontime AS date"
                    + " FROM extension AS e"
                    + " WHERE e.pid == ?");
            ps.setString(1, part.getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(rs.getInt("date") * 1000L);
                result.put(rs.getString("studlogin"), cal);
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Calendar getExtension(String studentLogin, Part part) throws SQLException {
        Calendar result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.ontime AS date"
                    + " FROM extension AS e"
                    + " WHERE e.pid == ?"
                    + " AND e.sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);

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
    public String getExtensionNote(String studentLogin, Part part) throws SQLException {
        String result = "";
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.note AS extnote "
                    + " FROM extension AS e"
                    + " WHERE e.pid == ?"
                    + " AND e.sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);

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
    public String getExemptionNote(String studentLogin, Part part) throws SQLException {
        String result = null;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS exenote"
                    + " FROM exemption AS x"
                    + " WHERE x.pid == ?"
                    + " AND x.sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);

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
    public void enterGrade(String studentLogin, Part part, double score) throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM grade"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO grade ('sid', 'pid', 'score')"
                    + " VALUES (?, ?, ?)");
            ps.setString(1, studentLogin);
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
    public double getStudentScore(String studentLogin, Part part) throws SQLException {
        double grade = 0;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore"
                    + " FROM grade AS g"
                    + " WHERE g.sid == ?"
                    + " AND g.pid == ?");
            ps.setString(1, studentLogin);
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
    public double getStudentAsgnScore(String studentLogin, Assignment asgn) throws SQLException {
        double grade = 0.0;
        Connection conn = this.openConnection();

        try {
            //make a list of all the parts for the assignment so that the sum can be made.
            //  grade only containts part IDs not asgn IDs
            String parts = "";
            for (Part p : asgn.getParts()) {
                parts += ",'" + p.getDBID() + "'";
            }
            if (parts.length() > 1) {
                parts = parts.substring(1);
            }

            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore"
                    + " FROM grade AS g"
                    + " WHERE g.sid == ?"
                    + " AND g.pid IN (" + parts + ")");
            ps.setString(1, studentLogin);

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
    public void unBlacklistStudent(String studentLogin, TA ta) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM blacklist "
                    + "WHERE tid == ? "
                    + "AND sid == ?");
            ps.setString(1, ta.getLogin());
            ps.setString(2, studentLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Double> getPartScores(Part part, Iterable<String> students) throws SQLException {
        Map<String, Double> scores = new HashMap<String, Double>();

        String studLogins = "";
        for (String student : students) {
            studLogins += ",'" + student + "'";
        }
        if (studLogins.length() > 1) {
            studLogins = studLogins.substring(1);
        }

        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, g.sid AS studLogin"
                    + " FROM grade AS g"
                    + " WHERE g.sid IN (" + studLogins + ")"
                    + " AND g.pid == '" + part.getDBID() + "'");
            //TODO: restore prepared statement functionality
            //ps.setString(1, studLogins);
            //ps.setString(1, part.getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.put(rs.getString("studLogin"), rs.getDouble("partscore"));
            }

            return scores;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Double> getAssignmentScores(Assignment asgn, Iterable<String> students) throws SQLException {
        Map<String, Double> scores = new HashMap<String, Double>();

        String studLogins = "";
        for (String student : students) {
            studLogins += ",'" + student + "'";
        }
        if (studLogins.length() > 1) {
            studLogins = studLogins.substring(1);
        }

        String parts = "";
        for (Part p : asgn.getParts()) {
            parts += ",'" + p.getDBID() + "'";
        }
        if (parts.length() > 1) {
            parts = parts.substring(1);
        }

        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, g.sid AS studLogin"
                    + " FROM grade AS g"
                    + " WHERE g.sid IN (" + studLogins + ")"
                    + " AND g.pid IN (" + parts + ")");
            //TODO: restore prepared statement functionality
            //ps.setString(1, studLogins);
            //ps.setString(2, parts);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String studLogin = rs.getString("studLogin");
                Double score = rs.getDouble("partscore");
                if (scores.containsKey(studLogin)) {
                    score += scores.get(studLogin);
                }
                scores.put(studLogin, score);
            }

            return scores;
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
    public void removeExemption(String studentLogin, Part part) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeExtension(String studentLogin, Part part) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setString(1, part.getDBID());
            ps.setString(2, studentLogin);

            ps.executeUpdate();
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<TA, Collection<String>> getDistribution(HandinPart part) throws SQLException {
        Map<TA, Collection<String>> result = new HashMap<TA, Collection<String>>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.sid AS studlogin, d.tid AS talogin"
                    + " FROM distribution AS d"
                    + " WHERE d.pid == ?");
            ps.setString(1, part.getDBID());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String taLogin = rs.getString("talogin");
                TA ta = Allocator.getCourseInfo().getTA(taLogin);
                String studLogin = rs.getString("studLogin");
                Collection taDist = result.get(ta);
                if (taDist == null) {
                    taDist = new ArrayList<String>();
                    result.put(ta, taDist);
                }
                taDist.add(studLogin);
            }

            return result;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean setGroups(HandinPart part, Map<String, Collection<String>> groupings) throws SQLException {
        Connection conn = this.openConnection();

        //make sure all the groupnames are valid
        Set<String> groupNames = groupings.keySet();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.name AS groupname"
                    + " FROM groups AS g ");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dbName = rs.getString("groupname");
                if (groupNames.contains(dbName)) {
                    //not bothering to fix this b/c groups methods will be rewritten soon
                    JOptionPane.showMessageDialog(null, "A group with this name, " + dbName + ", already exists. Please pick another name and try again. No groups were added due to this conflict.");
                    return false;
                }
            }

        } finally {
            this.closeConnection(conn);
        }

        //put all the groups into the DB
        for (String groupName : groupings.keySet()) {
            if (!this.setGroup(part, groupName, groupings.get(groupName))) {
                break;
            }
        }
        return true;
    }

    @Override
    public boolean setGroup(HandinPart part, String groupName, Collection<String> group) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(g.gpid) AS numgroups"
                    + " FROM groups AS g"
                    + " WHERE g.name == ?");
            ps.setString(1, groupName);

            ResultSet testSet = ps.executeQuery();
            if (testSet.getInt("numgroups") != 0) {
                //not bothering to fix this b/c groups methods will be rewritten soon
                JOptionPane.showMessageDialog(null, "A group with this name, " + groupName + ", already exists. Please pick another name. This group was not added.");
                return false;
            }

            ps = conn.prepareStatement("INSERT INTO groups"
                    + " ('name') VALUES (?)");
            ps.setString(1, groupName);
            ps.executeUpdate();

            ps = conn.prepareStatement("SELECT g.gpid AS groupid"
                    + " FROM groups AS g"
                    + " WHERE g.name == ?");
            ps.setString(1, groupName);

            ResultSet rs = ps.executeQuery();
            int groupID = rs.getInt("groupid");

            for (String student : group) {
                ps = conn.prepareStatement("INSERT INTO groupmembers "
                        + "('gpid', 'sid', 'pid') "
                        + "VALUES (?, ?, ?)");
                ps.setInt(1, groupID);
                ps.setString(2, student);
                ps.setString(3, part.getDBID());
                ps.executeUpdate();
            }

            return true;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<String> getGroup(HandinPart part, String student) throws SQLException {
        Collection<String> group = new ArrayList<String>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gm.sid AS studlogin"
                    + " FROM groupmembers AS gm"
                    + " WHERE gm.pid == ?"
                    + " AND gm.gpid IN"
                    + " (SELECT gm.gpid"
                    + " FROM groupmembers AS gm"
                    + " WHERE gm.pid == ?"
                    + " AND gm.sid == ?)");
            ps.setString(1, part.getDBID());
            ps.setString(2, part.getDBID());
            ps.setString(3, student);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                group.add(rs.getString("studlogin"));
            }

            if (group.size() == 0) {
                group.add(student);
            }

            return group;
        } finally {
            this.closeConnection(conn);
        }
    }

    /*
     * build a mapping of all students to all the students in their group
     * @param HandinPart
     * @return map of students to collections of group members
     */
    @Override
    public Map<String, Collection<String>> getGroups(HandinPart part) throws SQLException {
        Map<String, Collection<String>> groups = new HashMap<String, Collection<String>>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT gm.sid AS studlogin, gm.gpid AS groupID"
                    + " FROM groupmembers AS gm"
                    + " WHERE gm.pid == ?");
            ps.setString(1, part.getDBID());

            Multimap<Integer, String> group2studs = ArrayListMultimap.create();
            Map<String, Integer> stud2groupID = new HashMap<String, Integer>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                group2studs.put(rs.getInt("groupID"), rs.getString("studlogin"));
                stud2groupID.put(rs.getString("studlogin"), rs.getInt("groupID"));
            }

            for (String student : stud2groupID.keySet()) {
                groups.put(student, group2studs.get(stud2groupID.get(student)));
            }

            for (String student : Allocator.getDatabaseIO().getAllStudents().keySet()) {
                if (!groups.containsKey(student)) {
                    Collection<String> singleMember = new ArrayList<String>();
                    singleMember.add(student);
                    groups.put(student, singleMember);
                }
            }

            return groups;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void removeGroup(HandinPart handin, Collection<String> group) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeGroups(HandinPart part) throws SQLException {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM groups"
                    + " WHERE gpid IN"
                    + " (SELECT gm.gpid AS groupid"
                    + " FROM groupmembers AS gm"
                    + " WHERE gm.pid == ?");
            ps.setString(1, part.getDBID());
            ps.executeUpdate();

            ps = conn.prepareStatement("DELETE FROM groupmembers "
                    + "WHERE pid == ?");
            ps.setString(1, part.getDBID());
            ps.executeUpdate();

        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Assignment, TA> getAllGradersForStudent(String studentLogin) throws SQLException, CakeHatDBIOException {
        Connection conn = this.openConnection();
        Map<Assignment, TA> graders = new HashMap<Assignment, TA>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT d.tid AS login, d.pid AS partID"
                    + " FROM distribution AS d"
                    + " WHERE d.sid == ?");
            ps.setString(1, studentLogin);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                for (Assignment asgn : Allocator.getCourseInfo().getHandinAssignments()) {
                    if (asgn.getHandinPart().getDBID().equals(rs.getString("partID"))) {
                        String taLogin = rs.getString("login");
                        TA ta = Allocator.getCourseInfo().getTA(taLogin);
                        if (ta == null) {
                            throw new CakeHatDBIOException("TA with login " + taLogin + " is not in the config file, "
                                    + "but is assigned to grade student " + studentLogin + " for "
                                    + "assignment " + asgn.getName() + ".");
                        }
                        graders.put(asgn, ta);
                    }
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
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'groupmembers';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'groups';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'student';");
            conn.commit();

            //CREATE all DB tables
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("CREATE TABLE 'blacklist' ('tid' VARCHAR NOT NULL, "
                    + "'sid' VARCHAR NOT NULL );");
            conn.createStatement().executeUpdate("CREATE TABLE 'distribution' ('sid' VARCHAR NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'tid' VARCHAR NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'exemption' ('sid' VARCHAR NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'note' TEXT);");
            conn.createStatement().executeUpdate("CREATE TABLE 'extension' ('sid' VARCHAR NOT NULL, "
                    + "'pid' VARCHAR NOT NULL, "
                    + "'ontime' INTEGER NOT NULL, "
                    + "'note' TEXT);");
            conn.createStatement().executeUpdate("CREATE TABLE 'grade' ('pid' VARCHAR NOT NULL, "
                    + "'sid' VARCHAR NOT NULL, "
                    + "'score' DOUBLE NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groupmembers' ('gpid' INTEGER NOT NULL, "
                    + "'sid' VARCHAR NOT NULL,"
                    + "'pid' VARCHAR NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groups' ('gpid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "'name' VARCHAR NOT NULL );");
            conn.createStatement().executeUpdate("CREATE TABLE 'student' ('login' VARCHAR PRIMARY KEY NOT NULL, "
                    + "'firstname' VARCHAR NOT NULL, "
                    + "'lastname' VARCHAR NOT NULL, "
                    + "'enabled' INTEGER NOT NULL DEFAULT 1 );");
            conn.commit();

            //CREATE all tables indices
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("CREATE INDEX blacklist_student ON blacklist (sid);");
            conn.createStatement().executeUpdate("CREATE INDEX blacklist_ta ON blacklist (tid);");
            conn.createStatement().executeUpdate("CREATE INDEX dist_pid ON distribution (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX dist_tid ON distribution (tid);");
            conn.createStatement().executeUpdate("CREATE INDEX exemp_pid ON exemption (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX exemp_sid ON exemption (sid);");
            conn.createStatement().executeUpdate("CREATE INDEX exten_pid ON extension (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX exten_sid ON extension (sid);");
            conn.createStatement().executeUpdate("CREATE INDEX grade_pid ON grade (pid);");
            conn.createStatement().executeUpdate("CREATE INDEX grade_sid ON grade (sid);");
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();

            throw e;
        } finally {
            this.closeConnection(conn);
        }
    }
}
