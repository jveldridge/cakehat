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
     * @param messageFrame - frame in which errors will show up
     */
    private Connection openConnection() {
        try {
            return _connProvider.createConnection();
        } catch (SQLException e) {
            new ErrorView(e, "Could not open a connection to the DB.");
        }
        return null;
    }

    /**
     * closes current connection to DB
     * @param messageFrame - frame in which errors will show up
     */
    private void closeConnection(Connection c) {
        try {
            _connProvider.closeConnection(c);
        } catch (SQLException e) {
            new ErrorView(e, "Could not close connection to the DB.");
        }
    }

    /**
     * delete's assignment's current distribution and adds a new distribution
     * @param messageFrame - frame in which errors will show up
     * @param asgn - string of assignment name
     * @param distribution - hashmap of ta login to arraylist of student logins
     */
    @Override
    public boolean setAsgnDist(HandinPart part, Map<TA, Collection<String>> distribution) {
        //add the distribution to the DB
        Connection conn = this.openConnection();
        try {
            HashMap<String, Integer> studentIDs = new HashMap<String, Integer>();
            HashMap<String, Integer> taIDs = new HashMap<String, Integer>();
            
            //get student logins and IDs
            ResultSet rs = conn.createStatement().executeQuery("SELECT s.login, s.sid FROM student AS s");
            while (rs.next()) {
                studentIDs.put(rs.getString("login"), rs.getInt("sid"));
            }

            //get TA logins and IDs
            rs = conn.createStatement().executeQuery("SELECT t.login, t.tid FROM ta AS t");
            while (rs.next()) {
                taIDs.put(rs.getString("login"), rs.getInt("tid"));
            }

            //get assignment and part IDs
            PreparedStatement ps = conn.prepareStatement("SELECT p.pid AS pid FROM part AS p" +
                    " INNER JOIN asgn AS a ON a.aid == p.aid" +
                    " WHERE a.name == ? AND p.name ==?");
            ps.setString(1, part.getAssignment().getName());
            ps.setString(2, part.getName());

            rs = ps.executeQuery();
            int partID = rs.getInt("pid");

            // stop committing so that all inserts happen in one FileIO
            conn.setAutoCommit(false);

            ps = conn.prepareStatement("DELETE FROM distribution WHERE pid == ?");
            ps.setInt(1, partID);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO distribution ('pid', 'sid', 'tid')" +
                    " VALUES (?, ?, ?)");
            for (TA ta : distribution.keySet()) {
                int taID = taIDs.get(ta.getLogin());
                Collection<String> distributedStudents = distribution.get(ta);

                for (String student : distributedStudents) {
                    ps.setInt(1, partID);
                    ps.setInt(2, studentIDs.get(student));
                    ps.setInt(3, taID);
                    ps.addBatch();
                }
            }
            ps.executeBatch();

            // commit all the inserts to the DB file
            conn.commit();

            return true;
        } catch (SQLException e) {
            try {
                // if there was an issue then remove the distribution
                conn.rollback();
            } catch (SQLException ex) { }

            new ErrorView(e, "There was an error distributing the assignments to the grading TAs.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * gets a single TA's blacklist from the DB as arraylist of student logins
     * @param messageFrame - frame in which errors will show up
     * @param ta
     * @return list of student logins that have been blacklisted by the ta
     */
    @Override
    public Collection<String> getTABlacklist(TA ta) {
        ArrayList<String> blackList = new ArrayList<String>();
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login "
                    + "FROM student AS s "
                    + "INNER JOIN blacklist AS b "
                    + "ON b.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON b.tid == t.tid "
                    + "WHERE t.login == ?");
            ps.setString(1, ta.getLogin());
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blackList.add(rs.getString("login"));
            }

            return blackList;
        } catch (Exception e) {
            new ErrorView(e, "Could not get black list for TA + " + ta + ".");
            return null;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Inserts a new assignment into the DB
     * @param assignmentName - Part
     * @return status
     */
    @Override
    public boolean addAssignmentPart(Part part) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(a.aid) AS asgnCount, a.aid AS asgnID "
                    + "FROM asgn AS a "
                    + "WHERE a.name == ?");
            ps.setString(1, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            int asgnCount = rs.getInt("asgnCount");

            if (asgnCount == 0) { //if asgn does not exist
                throw new CakeHatDBIOException("The part being added does not have a corresponding assignment in the DB.");
            }
            int asgnID = rs.getInt("asgnID");

            ps = conn.prepareStatement("SELECT COUNT(p.pid) AS partCount "
                    + "FROM part AS p "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == ?"
                    + "AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());

            rs = ps.executeQuery();
            int partCount = rs.getInt("partCount");

            if (partCount != 0) { //if assignment part already exists
                throw new CakeHatDBIOException("An assignment part with that name already exists for that assignment.");
            }

            ps = conn.prepareStatement("INSERT INTO part "
                    + "('name', 'aid') VALUES (?, ?)");
            ps.setString(1, part.getName());
            ps.setInt(2, asgnID);
            ps.executeUpdate();

            return true;
        } catch (Exception e) {
            new ErrorView(e, "The assignment part: " + part.getName() + " could not be added to the Database.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Inserts a new student into the DB
     * If the student already exists it will silently not add the student
     *
     * @param studentLogin - String login
     * @param studentFirstName - String First Name
     * @param studentLastName - String Last Name
     * @return was student added
     */
    @Override
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(s.sid) AS rowcount "
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
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for student.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Set enabled to 0 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    @Override
    public boolean disableStudent(String studentLogin) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 0 WHERE login == ?");
            ps.setString(1, studentLogin);

            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be disabled.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Set enabled to 1 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    @Override
    public boolean enableStudent(String studentLogin) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE student "
                    + "SET enabled = 1 WHERE login == ?");
            ps.setString(1, studentLogin);

            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be enabled.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean addTA(TA ta) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(t.tid) AS rowcount "
                    + "FROM ta AS t "
                    + "WHERE t.login == ?");
            ps.setString(1, ta.getLogin());

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");

            if (rows == 0) {
                ps = conn.prepareStatement("INSERT INTO ta "
                        + "('login', 'name') "
                        + "VALUES (?, ?)");
                ps.setString(1, ta.getLogin());
                ps.setString(2, ta.getName());
                ps.executeUpdate();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for ta: " + ta);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getAllStudents() {
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

        } catch (Exception e) {
            new ErrorView(e, "Could not get All Students from DB");
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public Map<String, String> getEnabledStudents() {
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

        } catch (Exception e) {
            new ErrorView(e, "Could not get All Enabled Students from DB");
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    /**
     * Check to see if TA has already BlackListed Student. If has not then adds Student to TA's blacklist
     * @param studentLogin - String Student
     * @param ta
     * @return status
     */
    @Override
    public boolean blacklistStudent(String studentLogin, TA ta) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(b.sid) AS value"
                    + " FROM blacklist AS b"
                    + " INNER JOIN student AS s"
                    + " ON s.sid == b.sid"
                    + " INNER JOIN ta AS t"
                    + " ON t.tid == b.tid"
                    + " WHERE s.login == ?"
                    + " AND  t.login == ?"
                    + " UNION ALL"
                    + " SELECT t.tid AS taid"
                    + " FROM ta AS t"
                    + " WHERE t.login == ?"
                    + " UNION ALL"
                    + " SELECT s.sid AS studentid"
                    + " FROM student AS s"
                    + " WHERE s.login == ?");
            ps.setString(1, studentLogin);
            ps.setString(2, ta.getLogin());
            ps.setString(3, ta.getLogin());
            ps.setString(4, studentLogin);

            ResultSet rs = ps.executeQuery();
            rs.next();
            int rows = rs.getInt("value");
            rs.next();
            int taID = rs.getInt("value");
            rs.next();
            int studentID = rs.getInt("value");
            if (rows == 0) {
                ps = conn.prepareStatement("INSERT INTO blacklist "
                        + "('sid', 'tid') VALUES (?, ?)");
                ps.setInt(1, studentID);
                ps.setInt(2, taID);
                ps.executeUpdate();
            }
            
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not blacklist the student: " + studentLogin + " for ta: " + ta);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Checks to see if the dist for an assignment is empty. If yes return true.
     * @param asgn - Part
     * @return Boolean - Empty = true, Not = false
     */
    @Override
    public boolean isDistEmpty(HandinPart part) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(d.sid) AS rowcount"
                    + " FROM distribution AS d"
                    + " INNER JOIN part AS p"
                    + " ON p.pid == d.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON a.aid == p.aid"
                    + " WHERE a.name == ?"
                    + " AND p.name == ?");
            ps.setString(1, part.getAssignment().getName());
            ps.setString(2, part.getName());

            ResultSet rs = ps.executeQuery();
            int rows = rs.getInt("rowcount");
            return rows == 0;
        } catch (Exception e) {
            new ErrorView(e, "Could not determine the size of the dist for: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Return all students on any TA's blacklist
     * @return Collection - strings of all the student logins
     */
    @Override
    public Collection<String> getBlacklistedStudents() {
        Connection conn = this.openConnection();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT s.login AS studlogin "
                    + "FROM student AS s "
                    + "INNER JOIN blacklist AS b "
                    + "ON b.sid == s.sid");
            HashSet<String> result = new HashSet<String>();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get BlackListed Students from DB");
            return new ArrayList<String>();
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * Get all the logins of the students assigned to a particular TA for an assignment.
     * Dist could be empty and will then return empty ArrayList
     * @param assignmentName - Part
     * @param taLogin - String TA Login
     * @return Collection - list of student logins
     */
    @Override
    public Collection<String> getStudentsAssigned(HandinPart part, TA ta) {
        ArrayList<String> result = new ArrayList<String>();
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS studlogin"
                    + " FROM student AS s"
                    + " INNER JOIN distribution AS d"
                    + " ON d.sid == s.sid"
                    + " INNER JOIN ta AS t"
                    + " ON d.tid == t.tid"
                    + " INNER JOIN part AS p"
                    + " ON d.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE t.login == ?"
                    + " AND p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, ta.getLogin());
            ps.setString(2, part.getName());
            ps.setString(3, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            
        } catch (Exception e) {
            new ErrorView(e, "Could not get students assigned to ta: " + ta + " for assignment: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public Collection<String> getAllAssignedStudents(HandinPart part) {
        Connection conn = this.openConnection();
        Collection<String> assignedStudents = new LinkedList<String>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS login FROM student AS s" +
                " INNER JOIN distribution AS d ON d.sid == s.sid" +
                " INNER JOIN part AS p ON p.pid == d.pid" +
                " INNER JOIN asgn AS a ON a.aid == p.aid" +
                " WHERE p.name == ? AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            
            ResultSet rs =  ps.executeQuery();
            while (rs.next()) {
                assignedStudents.add(rs.getString("login"));
            }

        } catch (SQLException e) {
            new ErrorView(e, String.format("Could not get all assigned students " +
                    "for assignment %s", part.getName()));
        } finally {
            this.closeConnection(conn);
            return assignedStudents;
        }
    }

    @Override
    public boolean assignStudentToGrader(String studentLogin, HandinPart part, TA ta) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(s.login) AS assigned"
                    + " FROM student AS s"
                    + " INNER JOIN distribution AS d"
                    + " ON d.sid == s.sid"
                    + " INNER JOIN ta AS t"
                    + " ON d.tid == t.tid"
                    + " INNER JOIN part AS p"
                    + " ON d.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE t.login == ?"
                    + " AND s.login == ?"
                    + " AND p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, ta.getLogin());
            ps.setString(2, studentLogin);
            ps.setString(3, part.getName());
            ps.setString(4, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            int isAssigned = rs.getInt("assigned");

            if (isAssigned == 0) {
                ps = conn.prepareStatement("SELECT s.sid FROM student AS s" +
                        " WHERE s.login == ?");
                ps.setString(1, studentLogin);
                rs = ps.executeQuery();
                int sID = rs.getInt("sid");

                ps = conn.prepareStatement("SELECT t.tid FROM ta AS t WHERE t.login == ?");
                ps.setString(1, ta.getLogin());
                rs = ps.executeQuery();
                int tID = rs.getInt("tid");

                ps = conn.prepareStatement("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid" +
                        " WHERE a.name == ? AND p.name == ?");
                ps.setString(1, part.getAssignment().getName());
                ps.setString(2, part.getName());
                rs = ps.executeQuery();
                int pID = rs.getInt("pid");

                ps = conn.prepareStatement("INSERT INTO distribution ('sid', 'tid', 'pid')" +
                        " VALUES (?, ?, ?)");
                ps.setInt(1, sID);
                ps.setInt(2, tID);
                ps.setInt(3, pID);
                ps.executeUpdate();
            }
            
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not add student: " + studentLogin + " to the dist for assignment: " + part.getAssignment().getName() + " for: " + ta);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean unassignStudentFromGrader(String studentLogin, HandinPart part, TA ta) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM distribution"
                    + " WHERE pid IN "
                    + " (SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE p.name == ? AND a.name == ?)"
                    + " AND sid IN"
                    + " (SELECT sid FROM student WHERE login == ?)"
                    + " AND tid IN"
                    + " (SELECT tid FROM ta WHERE login ==  ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, studentLogin);
            ps.setString(4, ta.getLogin());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not remove student: " + studentLogin + " from the dist for assignment: " + part.getAssignment().getName() + " for: " + ta);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean grantExtension(String studentLogin, Part part, Calendar newDate, String note) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE pid IN"
                    + " (SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE p.name == ? AND a.name == ?)"
                    + " AND sid IN"
                    + " (SELECT s.sid FROM student AS s WHERE s.login == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, studentLogin);
            ps.executeUpdate();

            ps = conn.prepareStatement("SELECT s.sid FROM student AS s WHERE s.login == ?");
            ps.setString(1, studentLogin);
            ResultSet rs = ps.executeQuery();
            int sID = rs.getInt("sid");

            ps = conn.prepareStatement("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid" +
                    " WHERE a.name == ? AND p.name == ?");
            ps.setString(1, part.getAssignment().getName());
            ps.setString(2, part.getName());

            rs = ps.executeQuery();
            int pID = rs.getInt("pid");
            int ontime = (int) (newDate.getTimeInMillis() / 1000);

            ps = conn.prepareStatement("INSERT INTO extension ('sid', 'pid', 'ontime', 'note')" +
                    " VALUES (?, ?, ?, ?)");
            ps.setInt(1, sID);
            ps.setInt(2, pID);
            ps.setInt(3, ontime);
            ps.setString(4, note);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not grant extension for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean grantExemption(String studentLogin, Part part, String note) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid IN"
                    + " (SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE p.name == ? AND a.name == ?)"
                    + " AND sid IN"
                    + " (SELECT s.sid FROM student AS s WHERE login == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, studentLogin);
            ps.executeUpdate();

            ps = conn.prepareStatement("SELECT s.sid FROM student AS s" +
                    " WHERE s.login == ?");
            ps.setString(1, studentLogin);
            ResultSet rs = ps.executeQuery();
            int sID = rs.getInt("sid");

            ps = conn.prepareStatement("SELECT p.pid AS pid FROM part AS p" +
                    " INNER JOIN asgn AS a ON a.aid == p.aid" +
                    " WHERE a.name == ? AND p.name == ?");
            ps.setString(1, part.getAssignment().getName());
            ps.setString(2, part.getName());
            rs = ps.executeQuery();
            int pID = rs.getInt("pid");

            ps = conn.prepareStatement("INSERT INTO exemption ('sid', 'pid', 'note')" +
                    " VALUES (?, ?, ?)");
            ps.setInt(1, sID);
            ps.setInt(2, pID);
            ps.setString(3, note);
            ps.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not grant exemption for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Calendar> getExtensions(Part part) {
        HashMap<String, Calendar> result = new HashMap<String, Calendar>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS studlogin, e.ontime AS date"
                    + " FROM student AS s"
                    + " INNER JOIN extension AS e"
                    + " ON e.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON e.pid == p.pid"
                    + " INNER JOIN asgn AS a "
                    + " ON p.aid == a.aid "
                    + " WHERE p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(rs.getInt("date") * 1000L);
                result.put(rs.getString("studlogin"), cal);
            }
            
        } catch (SQLException e) {
            new ErrorView(e, "Could not get students with extensions for assignment part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public Calendar getExtension(String studentLogin, Part part) {
        Calendar result = null;
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.ontime AS date"
                    + " FROM extension AS e"
                    + " INNER JOIN student AS s"
                    + " ON e.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON e.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND s.login == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, studentLogin);
            ps.setString(3, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new GregorianCalendar();
                result.setTimeInMillis(rs.getInt("date") * 1000L);
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get extension for: " + studentLogin + " for assignment part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public String getExtensionNote(String studentLogin, Part part) {
        String result = "";
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT e.note AS extnote "
                    + " FROM extension AS e "
                    + " INNER JOIN student AS s "
                    + " ON e.sid == s.sid "
                    + " INNER JOIN part AS p "
                    + " ON e.pid == p.pid "
                    + " INNER JOIN asgn AS a "
                    + " ON p.aid == a.aid "
                    + " WHERE p.name == ?"
                    + " AND s.login == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, studentLogin);
            ps.setString(3, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("extnote");
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get extension note for: " + studentLogin + " for assignment part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public String getExemptionNote(String studentLogin, Part part) {
        String result = null;
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT x.note AS exenote"
                    + " FROM exemption AS x"
                    + " INNER JOIN student AS s"
                    + " ON x.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON x.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND s.login == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, studentLogin);
            ps.setString(3, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("exenote");
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get exemption note for: " + studentLogin + " for assignment part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public boolean enterGrade(String studentLogin, Part part, double score) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.sid FROM student AS s" +
                    " WHERE s.login == ?");
            ps.setString(1, studentLogin);
            ResultSet rs = ps.executeQuery();
            int sID = rs.getInt("sid");

            ps = conn.prepareStatement("SELECT p.pid AS pid FROM part AS p" +
                    " INNER JOIN asgn AS a ON a.aid == p.aid" +
                    " WHERE a.name == ? AND p.name == ?");
            ps.setString(1, part.getAssignment().getName());
            ps.setString(2, part.getName());
            rs = ps.executeQuery();
            int pID = rs.getInt("pid");

            ps = conn.prepareStatement("DELETE FROM grade"
                    + " WHERE pid == ?"
                    + " AND sid == ?");
            ps.setInt(1, pID);
            ps.setInt(2, sID);
            ps.executeUpdate();

            ps = conn.prepareStatement("INSERT INTO grade ('sid', 'pid', 'score')" +
                    " VALUES (?, ?, ?)");
            ps.setInt(1, sID);
            ps.setInt(2, pID);
            ps.setDouble(3, score);
            ps.executeUpdate();

            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not add a score of: " + score + " to the grade for assignment: " + part.getAssignment().getName() + " part: " + part.getName() + " for: " + studentLogin);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public double getStudentScore(String studentLogin, Part part) {
        double grade = 0;
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore"
                    + " FROM grade AS g"
                    + " INNER JOIN student AS s ON g.sid == s.sid"
                    + " INNER JOIN part AS p ON g.pid == p.pid"
                    + " INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE s.login == ?"
                    + " AND p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, studentLogin);
            ps.setString(2, part.getName());
            ps.setString(3, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                grade = rs.getDouble("partscore");
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get a score for: " + studentLogin + " for for assignment: " + part.getAssignment().getName() + " part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return grade;
        }
    }

    @Override
    public double getStudentAsgnScore(String studentLogin, Assignment asgn) {
        double grade = 0.0;
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT SUM(g.score) AS asgnscore"
                    + " FROM grade AS g"
                    + " INNER JOIN student AS s ON g.sid == s.sid"
                    + " INNER JOIN part AS p ON g.pid == p.pid"
                    + " INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE s.login == ?"
                    + " AND a.name == ?");
            ps.setString(1, studentLogin);
            ps.setString(2, asgn.getName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                grade = rs.getDouble("asgnscore");
            }

        } catch (SQLException e) {
            new ErrorView(e, String.format("Could not get a total score for: %s for for assignment: %s.",
                    studentLogin, asgn.getName()));
        }
        finally {
            this.closeConnection(conn);
            return grade;
        }
    }

    @Override
    public boolean addAssignment(Assignment asgn) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(a.aid) AS count"
                    + " FROM asgn AS a"
                    + " WHERE a.name == ?");
            ps.setString(1, asgn.getName());

            ResultSet rs = ps.executeQuery();
            int count = rs.getInt("count");

            if (count != 0) { // if assignment already exists
                throw new CakeHatDBIOException("An assignment with that name already exists.");
            }

            ps = conn.prepareStatement("INSERT INTO asgn "
                    + " ('name') VALUES (?)");
            ps.setString(1, asgn.getName());
            ps.executeUpdate();

            return true;
        } catch (Exception e) {
            new ErrorView(e, "The assignment: " + asgn.getName() + " could not be added to the Database.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean unBlacklistStudent(String studentLogin, TA ta) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM blacklist "
                    + "WHERE tid IN "
                    + "(SELECT t.tid FROM ta AS t WHERE t.login == ?) "
                    + "AND sid IN "
                    + "(SELECT s.sid FROM student AS s WHERE s.login == ?)");
            ps.setString(1, ta.getLogin());
            ps.setString(2, studentLogin);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not remove student: " + studentLogin + " from the blacklist of: " + ta);
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<String, Double> getPartScores(Part part, Iterable<String> students) {
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
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, s.login AS studLogin"
                    + " FROM grade AS g"
                    + " INNER JOIN student AS s ON g.sid == s.sid"
                    + " INNER JOIN part AS p ON g.pid == p.pid"
                    + " INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE s.login IN (" + studLogins + ")"
                    + " AND p.name == '" + part.getName() + "'"
                    + " AND a.name == '" + part.getAssignment().getName() + "'");
            //TODO: restore prepared statement functionality
            //ps.setString(1, studLogins);
            //ps.setString(1, part.getName());
            //ps.setString(2, part.getAssignment().getName());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.put(rs.getString("studLogin"), rs.getDouble("partscore"));
            }
            
        } catch (Exception e) {
            new ErrorView(e, "Could not get a score for: " + studLogins + " for for assignment: " + part.getAssignment().getName() + " part: " + part.getName());
        } finally {
            this.closeConnection(conn);
            return scores;
        }
    }

    @Override
    public Map<String, Double> getAssignmentScores(Assignment asgn, Iterable<String> students) {
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
            PreparedStatement ps = conn.prepareStatement("SELECT g.score AS partscore, s.login AS studLogin"
                    + " FROM grade AS g"
                    + " INNER JOIN student AS s ON g.sid == s.sid"
                    + " INNER JOIN part AS p ON g.pid == p.pid"
                    + " INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE s.login IN (" + studLogins + ")"
                    + " AND a.name == '" + asgn.getName() + "'");
            //TODO: restore prepared statement functionality
            //ps.setString(1, studLogins);
            //ps.setString(2, asgn.getName());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String studLogin = rs.getString("studLogin");
                Double score = rs.getDouble("partscore");
                if (scores.containsKey(studLogin)) {
                    score += scores.get(studLogin);
                }
                scores.put(studLogin, score);
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get a score for: " + studLogins + " for for assignment: " + asgn.getName());
        } finally {
            this.closeConnection(conn);
            return scores;
        }
    }

    @Override
    public boolean isStudentEnabled(String studentLogin) {
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
        } catch (SQLException e) {
            new ErrorView(e, "Could not determine if student: " + studentLogin + " is enabled.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean removeExemption(String studentLogin, Part part) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exemption"
                    + " WHERE pid IN"
                    + " (SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE p.name == ? AND a.name == ?')"
                    + " AND sid IN"
                    + " (SELECT sid FROM student WHERE login == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, studentLogin);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not remove exemption for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean removeExtension(String studentLogin, Part part) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM extension"
                    + " WHERE pid IN"
                    + " (SELECT p.pid FROM part AS p INNER JOIN asgn AS a" 
                    + " ON p.aid == a.aid WHERE p.name == ?"
                    + " AND a.name == ?)"
                    + " AND sid IN"
                    + " (SELECT s.sid FROM student AS s WHERE s.login == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, studentLogin);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not remove extension for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<TA, Collection<String>> getDistribution(HandinPart part) {
        Map<TA, Collection<String>> result = new HashMap<TA, Collection<String>>();
        Connection conn = this.openConnection();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS studlogin, t.login AS talogin"
                    + " FROM student AS s"
                    + " INNER JOIN distribution AS d"
                    + " ON d.sid == s.sid"
                    + " INNER JOIN ta AS t"
                    + " ON d.tid == t.tid"
                    + " INNER JOIN part AS p"
                    + " ON d.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?"
                    + " ORDER BY t.tid");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());

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

        } catch (SQLException e) {
            new ErrorView(e, "Could not get students assigned for handin part: " + part.getName() + " for assignment: " + part.getAssignment().getName());
        } finally {
            this.closeConnection(conn);
            return result;
        }
    }

    @Override
    public boolean setGroups(HandinPart part, Map<String, Collection<String>> groupings) {
        Connection conn = this.openConnection();

        //make sure all the groupnames are valid and get the partID
        int partID;
        Set<String> groupNames = groupings.keySet();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT g.name AS groupname" +
                    " FROM groups AS g ");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dbName = rs.getString("groupname");
                if (groupNames.contains(dbName)) {
                    JOptionPane.showMessageDialog(null, "A group with this name, " + dbName + ", already exists. Please pick another name and try again. No groups were added due to this conflict.");
                    return false;
                }
            }
            
            ps = conn.prepareStatement("SELECT p.pid AS partid"
                    + " FROM part AS p"
                    + " INNER JOIN asgn AS a ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());

            rs = ps.executeQuery();
            partID = rs.getInt("partid");
        } catch (SQLException e) {
            new ErrorView(e, "Getting all the group names for this assignment failed.");
            return false;
        } finally {
            this.closeConnection(conn);
        }

        //put all the groups into the DB
        for (String groupName : groupings.keySet()) {
            if (!this.setGroup(part, groupName, groupings.get(groupName), partID)) {
                break;
            }
        }
        return true;
    }

    @Override
    public boolean setGroup(HandinPart part, String groupName, Collection<String> group, Integer partID) {
        Connection conn = this.openConnection();
        try {
            if (partID == null) {
                PreparedStatement ps = conn.prepareStatement("SELECT p.pid AS partid"
                        + " FROM part AS p"
                        + " INNER JOIN asgn AS a ON p.aid == a.aid"
                        + " WHERE p.name == ?"
                        + " AND a.name == ?");
                ps.setString(1, part.getName());
                ps.setString(1, part.getAssignment().getName());

                ResultSet rs = ps.executeQuery();
                partID = rs.getInt("partid");
            }

            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(g.gpid) AS numgroups"
                    + " FROM groups AS g"
                    + " WHERE g.name == ?");
            ps.setString(1, groupName);

            ResultSet testSet = ps.executeQuery();
            if (testSet.getInt("numgroups") != 0) {
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
                ps = conn.prepareStatement("SELECT s.sid AS studentid"
                        + " FROM student AS s"
                        + " WHERE s.login == ?");
                ps.setString(1, student);

                rs = ps.executeQuery();
                int studentID = rs.getInt("studentid");

                ps = conn.prepareStatement("INSERT INTO groupmembers "
                        + "('gpid', 'sid', 'pid') "
                        + "VALUES (?, ?, ?)");
                ps.setInt(1, groupID);
                ps.setInt(2, studentID);
                ps.setInt(3, partID);
                ps.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            new ErrorView(e, "Could not get students in the group with: for handin part: " + part.getName() + " for assignment: " + part.getAssignment().getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Collection<String> getGroup(HandinPart part, String student) {
        Collection<String> group = new ArrayList<String>();
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS studlogin"
                    + " FROM student AS s"
                    + " INNER JOIN groupmembers AS gm"
                    + " ON gm.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON gm.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?"
                    + " AND gm.gpid IN"
                    + " (SELECT gm.gpid"
                    + " FROM groupmembers AS gm"
                    + " INNER JOIN student AS s"
                    + " ON gm.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON gm.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?"
                    + " AND s.login == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.setString(3, part.getName());
            ps.setString(4, part.getAssignment().getName());
            ps.setString(5, student);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                group.add(rs.getString("studlogin"));
            }

            if (group.size() == 0) {
                group.add(student);
            }

        } catch (SQLException e) {
            new ErrorView(e, "Could not get students in the group with: " + student + " for handin part: " + part.getName() + " for assignment: " + part.getAssignment().getName());
        } finally {
            this.closeConnection(conn);
            return group;
        }
    }

    /*
     * build a mapping of all students to all the students in their group
     * @param HandinPart
     * @return map of students to collections of group members
     */
    @Override
    public Map<String, Collection<String>> getGroups(HandinPart part) {
        Map<String, Collection<String>> groups = new HashMap<String, Collection<String>>();
        Connection conn = this.openConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT s.login AS studlogin, gm.gpid AS groupID"
                    + " FROM student AS s"
                    + " INNER JOIN groupmembers AS gm"
                    + " ON gm.sid == s.sid"
                    + " INNER JOIN part AS p"
                    + " ON gm.pid == p.pid"
                    + " INNER JOIN asgn AS a"
                    + " ON p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());

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

        } catch (SQLException e) {
            new ErrorView(e, "Could not get all the groups: for handin part: " + part.getName() + " for assignment: " + part.getAssignment().getName());
        } finally {
            this.closeConnection(conn);
            return groups;
        }
    }

    @Override
    public boolean removeGroup(HandinPart handin, Collection<String> group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeGroups(HandinPart part) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM groups"
                    + " WHERE gpid IN"
                    + " (SELECT gm.gpid AS groupid"
                    + " FROM groupmembers AS gm"
                    + " INNER JOIN part AS p on gm.pid == p.pid"
                    + " INNER JOIN asgn AS a on p.aid == a.aid"
                    + " WHERE p.name == ?"
                    + " AND a.name == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.executeUpdate();

            ps = conn.prepareStatement("DELETE FROM groupmembers "
                    + "WHERE pid IN "
                    + "(SELECT p.pid AS partid "
                    + "FROM part AS p "
                    + "INNER JOIN asgn AS a on p.aid == a.aid "
                    + "WHERE p.name == ?"
                    + "AND a.name == ?)");
            ps.setString(1, part.getName());
            ps.setString(2, part.getAssignment().getName());
            ps.executeUpdate();

            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not remove all groups from assignment: " + part.getAssignment().getName() + " for the part: " + part.getName());
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean assignmentExists(Assignment asgn) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(a.aid) AS count"
                    + " FROM asgn AS a "
                    + " WHERE a.name == ?");
            ps.setString(1, asgn.getName());

            ResultSet rs = ps.executeQuery();
            int count = rs.getInt("count");
            
            return (count != 0);
        } catch (SQLException e) {
            new ErrorView(e, "There was an error while trying to test if the assignment: "
                    + asgn.getName() + " exists in the Database.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public boolean studentExists(String login) {
        Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(s.sid) AS count"
                    + " FROM student AS s"
                    + " WHERE s.login == ?");
            ps.setString(1, login);

            ResultSet rs = ps.executeQuery();
            int count = rs.getInt("count");

            return (count != 0);
        } catch (SQLException e) {
            new ErrorView(e, "There was an error while trying to test if the student: "
                    + login + " exists in the Database.");
            return false;
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public void resetDatabase() {
        Connection conn = this.openConnection();
        try {

            //DROP all tables in DB
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'asgn';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'blacklist';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'distribution';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'exemption';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'extension';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'grade';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'groupmembers';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'groups';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'part';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'student';");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS 'ta';");
            conn.commit();

            //CREATE all DB tables
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("CREATE TABLE 'asgn' ('aid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                                                     + "'name' VARCHAR NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'blacklist' ('bid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                                                          + "'tid' INTEGER NOT NULL, "
                                                                          + "'sid' INTEGER NOT NULL );");
            conn.createStatement().executeUpdate("CREATE TABLE 'distribution' ('did' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                                                             + "'sid' INTEGER NOT NULL, "
                                                                             + "'pid' INTEGER NOT NULL, "
                                                                             + "'tid' INTEGER NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'exemption' ('xid' INTEGER PRIMARY KEY NOT NULL, "
                                                                          + "'sid' INTEGER NOT NULL, "
                                                                          + "'pid' INTEGER NOT NULL, "
                                                                          + "'note' TEXT);");
            conn.createStatement().executeUpdate("CREATE TABLE 'extension' ('eid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                                                          + "'sid' INTEGER NOT NULL, "
                                                                          + "'pid' INTEGER NOT NULL, "
                                                                          + "'ontime' DATETIME NOT NULL, "
                                                                          + "'note' TEXT);");
            conn.createStatement().executeUpdate("CREATE TABLE 'grade' ('gid' INTEGER PRIMARY KEY NOT NULL, "
                                                                      + "'pid' INTEGER NOT NULL, "
                                                                      + "'sid' INTEGER NOT NULL, "
                                                                      + "'score' DOUBLE NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groupmembers' ('gmid' INTEGER PRIMARY KEY NOT NULL, "
                                                                             + "'gpid' INTEGER NOT NULL, "
                                                                             + "'sid' INTEGER NOT NULL,"
                                                                             + "'pid' INTEGER NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'groups' ('gpid' INTEGER PRIMARY KEY NOT NULL, "
                                                                       + "'name' VARCHAR NOT NULL );");
            conn.createStatement().executeUpdate("CREATE TABLE 'part' ('pid' INTEGER PRIMARY KEY  NOT NULL, "
                                                                     + "'name' VARCHAR NOT NULL, "
                                                                     + "'aid' INTEGER NOT NULL);");
            conn.createStatement().executeUpdate("CREATE TABLE 'student' ('sid' INTEGER PRIMARY KEY NOT NULL, "
                                                                        + "'login' VARCHAR NOT NULL, "
                                                                        + "'firstname' VARCHAR NOT NULL, "
                                                                        + "'lastname' VARCHAR NOT NULL, "
                                                                        + "'enabled' INTEGER NOT NULL DEFAULT 1 );");
            conn.createStatement().executeUpdate("CREATE TABLE 'ta' ('tid' INTEGER PRIMARY KEY NOT NULL, "
                                                                   + "'login' VARCHAR NOT NULL,"
                                                                   + "'name' VARCHAR NOT NULL DEFAULT not_listed);");
            conn.commit();

            //CREATE all tables indices
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("CREATE INDEX asgn_name ON asgn (name);");
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
            conn.createStatement().executeUpdate("CREATE INDEX part_aid ON part (aid);");
            conn.createStatement().executeUpdate("CREATE INDEX part_name ON part (name);");
            conn.createStatement().executeUpdate("CREATE INDEX student_login ON student (login);");
            conn.createStatement().executeUpdate("CREATE INDEX ta_login ON ta (login);");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {}
            new ErrorView(e, "The database could not be cleared and reset.");
        } finally {
            this.closeConnection(conn);
        }
    }

    @Override
    public Map<Assignment, TA> getAllGradersForStudent(String studentLogin) {
        Connection conn = this.openConnection();
        Map<Assignment, TA> graders = new HashMap<Assignment, TA>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT t.login AS login, a.name AS asgn FROM ta AS t"
                    + " INNER JOIN distribution AS d ON d.tid == t.tid"
                    + " INNER JOIN student AS s ON d.sid == s.sid"
                    + " INNER JOIN part AS p ON p.pid == d.pid"
                    + " INNER JOIN asgn AS a ON a.aid == p.aid"
                    + " WHERE s.login == ?");
            ps.setString(1, studentLogin);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                for (Assignment asgn : Allocator.getCourseInfo().getHandinAssignments()) {
                    if (asgn.getName().equals(rs.getString("asgn"))) {
                        String taLogin = rs.getString("login");
                        TA ta = Allocator.getCourseInfo().getTA(taLogin);
                        if (ta == null) {
                            new ErrorView("TA with login " + taLogin + " is not in the config file, " +
                                          "but is assigned to grade student " + studentLogin + " for " +
                                          "assignment " + asgn.getName() + ".");
                            //returning the map so far, even though it's invalid, to avoid
                            //an immediate NullPointerException
                            return graders;
                        }
                        graders.put(asgn, Allocator.getCourseInfo().getTA(taLogin));
                    }
                }
            }

        } catch (SQLException e) {
            new ErrorView(e, String.format("Could not get all the graders for the students: %s", studentLogin));
        } finally {
            this.closeConnection(conn);
            return graders;
        }
    }
}
