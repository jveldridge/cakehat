package database;

import utils.*;
import config.Assignment;
import config.HandinPart;
import config.Part;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * all DB accessor and mutator methods for cakehat
 * @author alexku
 */
public class DBWrapper implements DatabaseIO {

    private Connection _connection;
    private Statement _statement;
    private String _dbPath;

    /**
     * sets DB path to regular location
     */
    public DBWrapper() {
        this("jdbc:sqlite:" + Allocator.getCourseInfo().getDatabaseFilePath());
    }

    /**
     * for testing allows for custom path
     * this constructor should only be used for testing the DBWrapper
     * @param path - String path to DB file
     */
    public DBWrapper(String path) {
        _dbPath = path;
    }

    /**
     * opens a new connection to the DB
     * @param messageFrame - frame in which errors will show up
     */
    private void openConnection() {
        _connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            _connection = DriverManager.getConnection(_dbPath);

            _statement = _connection.createStatement();
            _statement.setQueryTimeout(30);  // set timeout to 30 sec.

        } catch (Exception e) {
            new ErrorView(e, "Could not open a connection to the DB.");
        }

    }

    /**
     * closes current connection to DB
     * @param messageFrame - frame in which errors will show up
     */
    private void closeConnection() {
        try {
            if (_connection != null) {
                _connection.close();
            }
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
    public boolean setAsgnDist(HandinPart part, Map<String, Collection<String>> distribution) {
        //add the distribution to the DB
        this.openConnection();
        try {
            HashMap<String, Integer> studentIDs = new HashMap<String, Integer>();
            HashMap<String, Integer> taIDs = new HashMap<String, Integer>();
            int partID;

            ResultSet rs = _statement.executeQuery("SELECT s.login, s.sid FROM student AS s");
            while (rs.next()) {
                studentIDs.put(rs.getString("login"), rs.getInt("sid"));
            }
            rs = _statement.executeQuery("SELECT t.login, t.tid FROM ta AS t");
            while (rs.next()) {
                taIDs.put(rs.getString("login"), rs.getInt("tid"));
            }
            rs = _statement.executeQuery("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid WHERE a.name == '" + part.getAssignment().getName() + "' AND p.name == '" + part.getName() + "'");
            partID = rs.getInt("pid");

            _statement.executeUpdate("DELETE FROM distribution WHERE pid == " + partID);

            String insertCommand = "INSERT INTO distribution ('pid', 'sid', 'tid') VALUES"; //start of insert command
            for (String ta : distribution.keySet()) {
                Collection<String> distributedStudents = distribution.get(ta);
                for (String student : distributedStudents) {
                    _statement.addBatch(insertCommand + " (" + partID + ", '" + studentIDs.get(student) + "', '" + taIDs.get(ta) + "')");
                }
            }
            _statement.executeBatch();
            JOptionPane.showMessageDialog(null, "Assignments have been successfully distributed to the grading TAs.", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "There was an error distributing the assignments to the grading TAs.");
            this.closeConnection();
            return false;
        }
    }

    /**
     * gets a single TA's blacklist from the DB as arraylist of student logins
     * @param messageFrame - frame in which errors will show up
     * @param taLogin - string of single TA's login
     * @return list of student logins that have been blacklisted by the ta
     */
    public Collection<String> getTABlacklist(String taLogin) {
        this.openConnection();
        try {
            ArrayList<String> blackList = new ArrayList<String>();

            ResultSet rs = _statement.executeQuery("SELECT s.login "
                    + "FROM student AS s "
                    + "INNER JOIN blacklist AS b "
                    + "ON b.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON b.tid == t.tid "
                    + "WHERE t.login == '" + taLogin + "'");
            while (rs.next()) {
                blackList.add(rs.getString("login"));
            }
            this.closeConnection();
            return blackList;
        } catch (Exception e) {
            new ErrorView(e, "Could not get black list for TA.");
            this.closeConnection();
            return null;
        }
    }

    /**
     * Inserts a new assignment into the DB (not checking if that assignment already exists)
     * @param assignmentName - Part
     * @return status
     */
    public boolean addAssignmentPart(Part part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(a.aid) AS count, a.aid AS aid "
                    + "FROM asgn AS a "
                    + "WHERE a.name == '" + part.getAssignment().getName() + "'");
            int count = rs.getInt("count");
            if (count != 0) {
                int asgnID = rs.getInt("aid");
                rs = _statement.executeQuery("SELECT COUNT(p.pid) AS count "
                        + "FROM part AS p " +
                        "INNER JOIN asgn AS a " +
                        "ON p.aid == a.aid "
                        + "WHERE p.name == '" + part.getName() + "' " +
                        "AND a.name == '" + part.getAssignment().getName() + "'");
                count = rs.getInt("count");
                if (count == 0) {
                    _statement.executeUpdate("INSERT INTO part "
                            + "('name', 'aid') "
                            + "VALUES "
                            + "('" + part.getName()
                            + "', " + asgnID + ")");
                } else {
                    throw new CakeHatDBIOException("An assignment part with that name already exists for that assignment.");
                }
            } else {
                throw new CakeHatDBIOException("The part being added does not have a corresponding assignment in the DB.");
            }
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for an assignment part.");
            this.closeConnection();
            return false;
        }
    }

    /**
     * Inserts a new student into the DB (not check if already exists)
     * TODO: make check for duplicate student
     * TODO: change to just one name
     * @param studentLogin - String login
     * @param studentFirstName - String First Name
     * @param studentLastName - String Last Name
     * @return status
     */
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) {
        this.openConnection();
        try {
            _statement.executeUpdate("INSERT INTO student "
                    + "('login', 'firstname', 'lastname') "
                    + "VALUES "
                    + "('" + studentLogin
                    + "', '" + studentFirstName
                    + "', '" + studentLastName
                    + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for student.");
            this.closeConnection();
            return false;
        }
    }

    /**
     * Set enabled to 0 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    public boolean disableStudent(String studentLogin) {
        this.openConnection();
        try {
            _statement.executeUpdate("UPDATE student "
                    + "SET "
                    + "enabled = 0 "
                    + "WHERE "
                    + "login == '" + studentLogin + "'");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be disabled.");
            this.closeConnection();
            return false;
        }
    }

    /**
     * Set enabled to 1 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    public boolean enableStudent(String studentLogin) {
        this.openConnection();
        try {
            _statement.executeUpdate("UPDATE student "
                    + "SET "
                    + "enabled = 1 "
                    + "WHERE "
                    + "login == '" + studentLogin + "'");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be enabled.");
            this.closeConnection();
            return false;
        }
    }

    /**
     * Checks to see if TA already exists. If does not exist then inserts TA to DB
     * TODO: add full name
     * @param taLogin - String TA Login
     * @param taName - String TA Name
     * @return status
     */
    public boolean addTA(String taLogin, String taName) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(t.tid) AS rowcount "
                    + "FROM ta AS t "
                    + "WHERE t.login == '" + taLogin + "'");
            int rows = rs.getInt("rowcount");
            if (rows == 0) {
                _statement.executeUpdate("INSERT INTO ta "
                        + "('login', 'name') "
                        + "VALUES "
                        + "('" + taLogin
                        + "','" + taName + "')");
            }
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for ta: " + taLogin);
            this.closeConnection();
            return false;
        }
    }

    //TODO: Actually retrieve this from the database
    public Map<String, String> getAllTAs() {
        HashMap<String, String> tas = new HashMap<String, String>();
        for(config.TA ta : Allocator.getCourseInfo().getTAs()) {
            tas.put(ta.getLogin(), Allocator.getGeneralUtilities().getUserName(ta.getLogin()));
        }

        return tas;
    }

    public Map<String, String> getAllStudents() {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin, "
                    + "s.firstname AS fname, "
                    + "s.lastname AS lname "
                    + "FROM student AS s ");
            HashMap<String, String> result = new HashMap<String, String>();
            while (rs.next()) {
                result.put(rs.getString("studlogin"), rs.getString("fname") + " " + rs.getString("lname"));
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get All Students from DB");
            this.closeConnection();
            return new HashMap<String, String>();
        }
    }
    
    public Map<String, String> getEnabledStudents() {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin, "
                    + "s.firstname AS fname, "
                    + "s.lastname AS lname "
                    + "FROM student AS s "
                    + "WHERE enabled == 1");
            HashMap<String, String> result = new HashMap<String, String>();
            while (rs.next()) {
                result.put(rs.getString("studlogin"), rs.getString("fname") + " " + rs.getString("lname"));
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get All Enabled Students from DB");
            this.closeConnection();
            return new HashMap<String, String>();
        }
    }

    /**
     * Check to see if TA has already BlackListed Student. If has not then adds Student to TA's blacklist
     * @param studentLogin - String Student
     * @param taLogin - String TA
     * @return status
     */
    public boolean blacklistStudent(String studentLogin, String taLogin) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(b.sid) AS value "
                    + "FROM blacklist AS b "
                    + "INNER JOIN student AS s "
                    + "ON s.sid == b.sid "
                    + "INNER JOIN ta AS t "
                    + "ON t.tid == b.tid "
                    + "WHERE s.login == '" + studentLogin + "' "
                    + "AND  t.login == '" + taLogin + "' "
                    + "UNION ALL "
                    + "SELECT t.tid AS taid "
                    + "FROM ta AS t "
                    + "WHERE t.login == '" + taLogin + "' "
                    + "UNION ALL "
                    + "SELECT s.sid AS studentid "
                    + "FROM student AS s "
                    + "WHERE s.login == '" + studentLogin + "' ");
            rs.next();
            int rows = rs.getInt("value");
            rs.next();
            int taID = rs.getInt("value");
            rs.next();
            int studentID = rs.getInt("value");
            if (rows == 0) {
                _statement.executeUpdate("INSERT INTO blacklist "
                        + "('sid', 'tid') "
                        + "VALUES "
                        + "(" + studentID
                        + ", " + taID
                        + ")");
            }
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not blacklist the student: " + studentLogin + " for ta: " + taLogin);
            this.closeConnection();
            return false;
        }
    }

    /**
     * Checks to see if the dist for an assignment is empty. If yes return true.
     * @param asgn - Part
     * @return Boolean - Empty = true, Not = false
     */
    public boolean isDistEmpty(HandinPart part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(d.sid) AS rowcount "
                    + "FROM distribution AS d "
                    + "INNER JOIN part AS p "
                    + "ON p.pid == d.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON a.aid == p.aid "
                    + "WHERE a.name == '" + part.getAssignment().getName() + "' "
                    + "AND p.name == '" + part.getName() + "'");
            int rows = rs.getInt("rowcount");
            this.closeConnection();
            return rows == 0;
        } catch (Exception e) {
            new ErrorView(e, "Could not determine the size of the dist for: " + part.getAssignment().getName());
            this.closeConnection();
            return false;
        }
    }

    /**
     * Return all students on any TA's blacklist
     * @return Collection - strings of all the student logins
     */
    public Collection<String> getBlacklistedStudents() {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin "
                    + "FROM student AS s "
                    + "INNER JOIN blacklist AS b "
                    + "ON b.sid == s.sid");
            HashSet<String> result = new HashSet<String>();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get BlackListed Students from DB");
            this.closeConnection();
            return new ArrayList<String>();
        }
    }

    /**
     * Get all the logins of the students assigned to a particular TA for an assignment.
     * Dist could be empty and will then return empty ArrayList
     * @param assignmentName - Part
     * @param taLogin - String TA Login
     * @return Collection - list of student logins
     */
    public Collection<String> getStudentsAssigned(HandinPart part, String taLogin) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin "
                    + "FROM student AS s "
                    + "INNER JOIN distribution AS d "
                    + "ON d.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON d.tid == t.tid "
                    + "INNER JOIN part AS p "
                    + "ON d.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE t.login == '" + taLogin + "' "
                    + "AND p.name == '" + part.getName() + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            ArrayList<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get students assigned to ta: " + taLogin + " for assignment: " + part.getName());
            this.closeConnection();
            return new ArrayList<String>();
        }
    }

    public boolean assignStudentToGrader(String studentLogin, HandinPart part, String taLogin) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(s.login) AS assigned "
                    + "FROM student AS s "
                    + "INNER JOIN distribution AS d "
                    + "ON d.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON d.tid == t.tid "
                    + "INNER JOIN part AS p "
                    + "ON d.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE t.login == '" + taLogin + "' "
                    + "AND s.login == '" + studentLogin + "' "
                    + "AND p.name == '" + part.getName() + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            int isAssigned = rs.getInt("assigned");
            if (isAssigned == 0) {
                rs = _statement.executeQuery("SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "'");
                int sID = rs.getInt("sid");
                rs = _statement.executeQuery("SELECT t.tid FROM ta AS t WHERE t.login == '" + taLogin + "'");
                int tID = rs.getInt("tid");
                rs = _statement.executeQuery("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid WHERE a.name == '" + part.getAssignment().getName() + "' AND p.name == '" + part.getName() + "'");
                int pID = rs.getInt("pid");
                _statement.executeUpdate("INSERT INTO distribution ('sid', 'tid', 'pid') VALUES (" + sID + ", " + tID + ", " + pID + ")");
            }
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not add student: " + studentLogin + " to the dist for assignment: " + part.getAssignment().getName() + " for: " + taLogin);
            this.closeConnection();
            return false;
        }
    }

    public boolean unassignStudentFromGrader(String studentLogin, HandinPart part, String taLogin) {
        this.openConnection();
        try {
                _statement.executeUpdate("DELETE FROM distribution " +
                        "WHERE pid IN " +
                          "(SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid WHERE p.name == '" + part.getName() + "' AND a.name == '" + part.getAssignment().getName() + "')" +
                         "AND sid IN " +
                          "(SELECT sid FROM student WHERE login == '" + studentLogin + "') " +
                         "AND tid IN " +
                          "(SELECT tid FROM ta WHERE login == '" + taLogin + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not remove student: " + studentLogin + " from the dist for assignment: " + part.getAssignment().getName() + " for: " + taLogin);
            this.closeConnection();
            return false;
        }
    }

    public boolean grantExtension(String studentLogin, Part part, Calendar newDate, String note) {
        this.openConnection();
        try {
            _statement.executeUpdate("DELETE FROM extension " +
                        "WHERE pid IN " +
                          "(SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid WHERE p.name == '" + part.getName() + "' AND a.name == '" + part.getAssignment().getName() + "')" +
                         "AND sid IN " +
                          "(SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "')");
                ResultSet rs = _statement.executeQuery("SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "'");
                int sID = rs.getInt("sid");
                rs = _statement.executeQuery("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid WHERE a.name == '" + part.getAssignment().getName() + "' AND p.name == '" + part.getName() + "'");
                int pID = rs.getInt("pid");
                int ontime = (int) (newDate.getTimeInMillis() / 1000);
                _statement.executeUpdate("INSERT INTO extension ('sid', 'pid', 'ontime', 'note') VALUES (" + sID + ", " + pID + ", " + ontime + ", '" + note + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not grant extension for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            this.closeConnection();
            return false;
        }
    }

    public boolean grantExemption(String studentLogin, Part part, String note) {
        this.openConnection();
        try {
            _statement.executeUpdate("DELETE FROM exemption " +
                        "WHERE pid IN " +
                          "(SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid WHERE p.name == '" + part.getName() + "' AND a.name == '" + part.getAssignment().getName() + "')" +
                         "AND sid IN " +
                          "(SELECT s.sid FROM student AS s WHERE login == '" + studentLogin + "')");
                ResultSet rs = _statement.executeQuery("SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "'");
                int sID = rs.getInt("sid");
                rs = _statement.executeQuery("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid WHERE a.name == '" + part.getAssignment().getName() + "' AND p.name == '" + part.getName() + "'");
                int pID = rs.getInt("pid");
                _statement.executeUpdate("INSERT INTO exemption ('sid', 'pid', 'note') VALUES (" + sID + ", " + pID + ", '" + note + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not grant exemption for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            this.closeConnection();
            return false;
        }
    }

    
    public Map<String, Calendar> getExtensions(Part part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin, e.ontime AS date "
                    + "FROM student AS s "
                    + "INNER JOIN extension AS e "
                    + "ON e.sid == s.sid "
                    + "INNER JOIN part AS p "
                    + "ON e.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == '" + part.getName() + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            HashMap<String, Calendar> result = new HashMap<String, Calendar>();
            while (rs.next()) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(rs.getInt("date") * 1000L);
                result.put(rs.getString("studlogin"), cal);
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get students with extensions for assignment part: " + part.getName());
            this.closeConnection();
            return null;
        }
    }
                
    public Calendar getExtension(String studentLogin, Part part) {
        this.openConnection();
        try { 
            ResultSet rs = _statement.executeQuery("SELECT e.ontime AS date "
                    + "FROM extension AS e "
                    + "INNER JOIN student AS s "
                    + "ON e.sid == s.sid "
                    + "INNER JOIN part AS p "
                    + "ON e.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == '" + part.getName() + "' "
                    + "AND s.login == '" + studentLogin + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            Calendar result = null;
            if (rs.next()) {
                result = new GregorianCalendar();
                result.setTimeInMillis(rs.getInt("date") * 1000L);
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get extension for: " + studentLogin + " for assignment part: " + part.getName());
            this.closeConnection();
            return null;
        }
    }

    public String getExtensionNote(String studentLogin, Part part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT e.note AS extnote "
                    + "FROM extension AS e "
                    + "INNER JOIN student AS s "
                    + "ON e.sid == s.sid "
                    + "INNER JOIN part AS p "
                    + "ON e.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == '" + part.getName() + "' "
                    + "AND s.login == '" + studentLogin + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            String result = null;
            if (rs.next()) {
                result = rs.getString("extnote");
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get extension note for: " + studentLogin + " for assignment part: " + part.getName());
            this.closeConnection();
            return null;
        }
    }

    public String getExemptionNote(String studentLogin, Part part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT x.note AS exenote "
                    + "FROM exemption AS x "
                    + "INNER JOIN student AS s "
                    + "ON x.sid == s.sid "
                    + "INNER JOIN part AS p "
                    + "ON x.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == '" + part.getName() + "' "
                    + "AND s.login == '" + studentLogin + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "'");
            String result = null;
            if (rs.next()) {
                result = rs.getString("exenote");
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get exemption note for: " + studentLogin + " for assignment part: " + part.getName());
            this.closeConnection();
            return null;
        }
    }

    //TODO: remove status
    public boolean enterGrade(String studentLogin, Part part, double score) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "'");
            int sID = rs.getInt("sid");
            rs = _statement.executeQuery("SELECT p.pid AS pid FROM part AS p INNER JOIN asgn AS a ON a.aid == p.aid WHERE a.name == '" + part.getAssignment().getName() + "' AND p.name == '" + part.getName() + "'");
            int pID = rs.getInt("pid");
            _statement.executeUpdate("DELETE FROM grade " +
                        "WHERE pid == " + pID + " " +
                         "AND sid == " + sID);
            _statement.executeUpdate("INSERT INTO grade ('sid', 'pid', 'score') VALUES (" + sID + ", " + pID + ", '" + score + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not add a score of: " + score + " to the grade for assignment: " + part.getAssignment().getName() + " part: " + part.getName() + " for: " + studentLogin);
            this.closeConnection();
            return false;
        }
    }

    public double getStudentScore(String studentLogin, Part part) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT g.score AS partscore "
                    + "FROM grade AS g "
                    + "INNER JOIN student AS s ON g.sid == s.sid "
                    + "INNER JOIN part AS p ON g.pid == p.pid "
                    + "INNER JOIN asgn AS a ON p.aid == a.aid "
                    + "WHERE s.login == '" + studentLogin + "' "
                    + "AND p.name == '" + part.getName() + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "' ");
            double grade = 0;
            if (rs.next()) {
                grade = rs.getDouble("partscore");
            }
            this.closeConnection();
            return grade;
        } catch (Exception e) {
            new ErrorView(e, "Could not get a score for: " + studentLogin + " for for assignment: " + part.getAssignment().getName() + " part: " + part.getName());
            this.closeConnection();
            return 0;
        }
    }

    public boolean resetDatabase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //TODO: add all parts if don't exist
    public boolean addAssignment(Assignment asgn) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(a.aid) AS count " +
                    "FROM asgn AS a " +
                    "WHERE a.name == '" + asgn.getName() + "'");
            int count = rs.getInt("count");
            if (count == 0) {
                _statement.executeUpdate("INSERT INTO asgn "
                        + "('name') "
                        + "VALUES "
                        + "('" + asgn.getName() + "')");
            }
            else {
                throw new CakeHatDBIOException("An assignment with that name already exisits.");
            }
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for an assignment part.");
            this.closeConnection();
            return false;
        }
    }

    public boolean unBlacklistStudent(String studentLogin, String taLogin) {
        this.openConnection();
        try {
            _statement.executeUpdate("DELETE FROM blacklist " +
                        "WHERE tid IN " +
                          "(SELECT t.tid FROM ta AS t WHERE t.login == '" + taLogin + "') " +
                         "AND sid IN " +
                          "(SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not remove student: " + studentLogin + " from the blacklist of: " + taLogin);
            this.closeConnection();
            return false;
        }
    }

    public Map<String, Double> getPartScores(Part part, Iterable<String> students) {
        Map<String, Double> scores = new HashMap<String, Double>();

        String studLogins = "";
        for (String student : students) {
            studLogins += ",'" + student + "'";
        }
        if (studLogins.length() > 1) {
            studLogins = studLogins.substring(1);
        }

        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT g.score AS partscore, s.login AS studLogin "
                    + "FROM grade AS g "
                    + "INNER JOIN student AS s ON g.sid == s.sid "
                    + "INNER JOIN part AS p ON g.pid == p.pid "
                    + "INNER JOIN asgn AS a ON p.aid == a.aid "
                    + "WHERE s.login IN (" + studLogins + ") "
                    + "AND p.name == '" + part.getName() + "' "
                    + "AND a.name == '" + part.getAssignment().getName() + "' ");
            while (rs.next()) {
                scores.put(rs.getString("studLogin"), rs.getDouble("partscore"));
            }
            this.closeConnection();
            return scores;
        } catch (Exception e) {
            new ErrorView(e, "Could not get a score for: " + studLogins + " for for assignment: " + part.getAssignment().getName() + " part: " + part.getName());
            this.closeConnection();
            return new HashMap<String, Double>();
        }
    }
    
    public Map<String, Double> getAssignmentScores(Assignment asgn, Iterable<String> students) {
        Map<String, Double> scores = new HashMap<String, Double>();

        String studLogins = "";
        for (String student : students) {
            studLogins += ",'" + student + "'";
        }
        if (studLogins.length() > 1) {
            studLogins = studLogins.substring(1);
        }

        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT g.score AS partscore, s.login AS studLogin "
                    + "FROM grade AS g "
                    + "INNER JOIN student AS s ON g.sid == s.sid "
                    + "INNER JOIN part AS p ON g.pid == p.pid "
                    + "INNER JOIN asgn AS a ON p.aid == a.aid "
                    + "WHERE s.login IN (" + studLogins + ") "
                    + "AND a.name == '" + asgn.getName() + "' ");
            while (rs.next()) {
                String studLogin = rs.getString("studLogin");
                Double score = rs.getDouble("partscore");
                if (scores.containsKey(studLogin)) {
                    score += scores.get(studLogin);
                }
                scores.put(studLogin, score);
            }
            this.closeConnection();
            return scores;
        } catch (Exception e) {
            new ErrorView(e, "Could not get a score for: " + studLogins + " for for assignment: " + asgn.getName());
            this.closeConnection();
            return new HashMap<String, Double>();
        }
    }

    public boolean isStudentEnabled(String studentLogin) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.enabled FROM student "
                    + "AS s "
                    + "WHERE "
                    + "s.login == '" + studentLogin + "'");
            int enabled = rs.getInt("enabled");
            this.closeConnection();
            if (enabled == 1)
                return true;
            else
                return false;
        } catch (Exception e) {
            new ErrorView(e, "Could not determine if enabled.");
            this.closeConnection();
            return false;
        }
    }

    public boolean removeExemption(String studentLogin, Part part) {
        this.openConnection();
        try {
            _statement.executeUpdate("DELETE FROM exemption " +
                        "WHERE pid IN " +
                          "(SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid WHERE p.name == '" + part.getName() + "' AND a.name == '" + part.getAssignment().getName() + "')" +
                         "AND sid IN " +
                          "(SELECT sid FROM student WHERE login == '" + studentLogin + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not remove exemption for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            this.closeConnection();
            return false;
        }
    }

    public boolean removeExtension(String studentLogin, Part part) {
        this.openConnection();
        try {
            _statement.executeUpdate("DELETE FROM extension "
                    + "WHERE pid IN "
                    + "(SELECT p.pid FROM part AS p INNER JOIN asgn AS a ON p.aid == a.aid WHERE p.name == '" + part.getName() + "' AND a.name == '" + part.getAssignment().getName() + "')"
                    + "AND sid IN "
                    + "(SELECT s.sid FROM student AS s WHERE s.login == '" + studentLogin + "')");
            this.closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not remove extension for student: " + studentLogin + " for the assignment: " + part.getAssignment().getName());
            this.closeConnection();
            return false;
        }
    }

    public Map<String, Collection<String>> getDistribution(HandinPart handin) {
        this.openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin, t.login AS talogin "
                    + "FROM student AS s "
                    + "INNER JOIN distribution AS d "
                    + "ON d.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON d.tid == t.tid "
                    + "INNER JOIN part AS p "
                    + "ON d.pid == p.pid "
                    + "INNER JOIN asgn AS a "
                    + "ON p.aid == a.aid "
                    + "WHERE p.name == '" + handin.getName() + "' "
                    + "AND a.name == '" + handin.getAssignment().getName() + "' " +
                    "ORDER BY t.tid");
            Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();
            while (rs.next()) {
                String taLogin = rs.getString("talogin");
                String studLogin = rs.getString("studLogin");
                Collection taDist = result.get(taLogin);
                if (taDist == null) {
                    taDist = new ArrayList<String>();
                    result.put(taLogin,taDist);
                }
                taDist.add(studLogin);
            }
            this.closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get students assigned for handin part: " + handin.getName() + " for assignment: " + handin.getAssignment().getName());
            this.closeConnection();
            return new HashMap<String, Collection<String>>();
        }
    }

    public boolean setGroups(HandinPart handin, Collection<Collection<String>> groupings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setGroup(HandinPart handin, Collection<String> group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getGroup(HandinPart handin, String student) {
        Collection<String> group = new ArrayList<String>();

        group.add(student);

        return group;
    }

    public Map<String, Collection<String>> getGroups(HandinPart handin) {
        Collection<String> students = this.getAllStudents().keySet();

        Map<String, Collection<String>> groups = new HashMap<String, Collection<String>>();

        for(String student : students){
            Collection<String> group = new ArrayList<String>();
            group.add(student);
            groups.put(student, group);
        }

        return groups;
    }

    public boolean removeGroup(HandinPart handin, Collection<String> group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
