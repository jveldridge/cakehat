package utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import rubric.TimeStatus;

/**
 * all DB accessor and mutator methods for cakehat
 * @author alexku
 */
public class DBWrapper implements DatabaseIO {

    private static Connection _connection;
    private static Statement _statement;

    /**
     * opens a new connection to the DB
     * @param messageFrame - frame in which errors will show up
     */
    private static void openConnection() {
        _connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            _connection = DriverManager.getConnection("jdbc:sqlite:/Users/alexku/EclipseWorkspace/grade_tests/lib/cakehatcs015.sqlite");

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
    private static void closeConnection() {
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
    public void setAsgnDist(String asgn, Map<String, ArrayList<String>> distribution) {
        //add the distribution to the DB
        openConnection();
        try {
            HashMap<String, Integer> studentIDs = new HashMap<String, Integer>();
            HashMap<String, Integer> taIDs = new HashMap<String, Integer>();
            int asgnID;

            ResultSet rs = _statement.executeQuery("SELECT s.login, s.sid FROM student AS s");
            while (rs.next()) {
                studentIDs.put(rs.getString("login"), rs.getInt("sid"));
            }
            rs = _statement.executeQuery("SELECT t.login, t.tid FROM ta AS t");
            while (rs.next()) {
                taIDs.put(rs.getString("login"), rs.getInt("tid"));
            }
            rs = _statement.executeQuery("SELECT a.aid FROM asgn AS a WHERE a.name == '" + asgn + "'");
            asgnID = rs.getInt("tid");

            _statement.executeQuery("DELETE FROM asgn WHERE name == '" + asgn + "'");

            String insertCommand = "INSERT INTO distribution ('aid', 'sid', 'tid') VALUES"; //start of insert command
            for (String ta : distribution.keySet()) {
                ArrayList<String> distributedStudents = distribution.get(ta);
                for (String student : distributedStudents) {
                    insertCommand += " (" + asgnID + ", '" + student + "', '" + ta + "'),";
                }
            }
            if (insertCommand.endsWith(",")) { //cut off trailing comma
                insertCommand = insertCommand.substring(0, insertCommand.length() - 1);
            }
            _statement.executeUpdate(insertCommand);
            JOptionPane.showMessageDialog(null, "Assignments have been successfully distributed to the grading TAs.", "Success", JOptionPane.INFORMATION_MESSAGE);
            closeConnection();
        } catch (Exception e) {
            new ErrorView(e, "There was an error distributing the assignments to the grading TAs.");
            closeConnection();
        }
    }

    /**
     * gets a single TA's blacklist from the DB as arraylist of student logins
     * @param messageFrame - frame in which errors will show up
     * @param taLogin - string of single TA's login
     * @return list of student logins that have been blacklisted by the ta
     */
    public Collection<String> getTABlacklist(String taLogin) {
        openConnection();
        try {
            ArrayList<String> blackList = new ArrayList<String>();

            ResultSet rs = _statement.executeQuery("SELECT s.login "
                    + "FROM student AS s "
                    + "INNER JOIN distribution AS d "
                    + "ON d.sid == s.sid "
                    + "INNER JOIN ta AS t "
                    + "ON d.tid == t.tid "
                    + "WHERE t.login == '" + taLogin + "'");
            while (rs.next()) {
                blackList.add(rs.getString("login"));
            }
            closeConnection();
            return blackList;
        } catch (Exception e) {
            new ErrorView(e, "Could not get black list for TA.");
            closeConnection();
            return null;
        }
    }

    /**
     * Inserts a new assignment into the DB (not checking if that assignment already exists)
     * @param assignmentName - String of name
     * @return status
     */
    public boolean addAssignment(String assignmentName) {
        openConnection();
        try {
            _statement.executeUpdate("INSERT INTO asgn "
                    + "('name') "
                    + "VALUES "
                    + "('" + assignmentName
                    + "')");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for assignment.");
            closeConnection();
            return false;
        }
    }

    /**
     * Inserts a new student into the DB (not check if already exists)
     * @param studentLogin - String login
     * @param studentFirstName - String First Name
     * @param studentLastName - String Last Name
     * @return status
     */
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) {
        openConnection();
        try {
            _statement.executeUpdate("INSERT INTO student "
                    + "('login', 'firstname', 'lastname') "
                    + "VALUES "
                    + "('" + studentLogin
                    + "', '" + studentFirstName
                    + "', '" + studentLastName
                    + "')");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for student.");
            closeConnection();
            return false;
        }
    }

    /**
     * Set enabled to 0 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    public boolean disableStudent(String studentLogin) {
        openConnection();
        try {
            _statement.executeUpdate("UPDATE student "
                    + "SET "
                    + "enabled = 0 "
                    + "WHERE "
                    + "login == '" + studentLogin + "'");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be disabled.");
            closeConnection();
            return false;
        }
    }

    /**
     * Set enabled to 1 for student passed in
     * @param studentLogin - String Student Login
     * @return status
     */
    public boolean enableStudent(String studentLogin) {
        openConnection();
        try {
            _statement.executeUpdate("UPDATE student "
                    + "SET "
                    + "enabled = 1 "
                    + "WHERE "
                    + "login == '" + studentLogin + "'");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not update student " + studentLogin + " to be enabled.");
            closeConnection();
            return false;
        }
    }

    /**
     * Checks to see if TA already exists. If does not exist then inserts TA to DB
     * @param taLogin - String TA Login
     * @param taFirstName - String TA First Name
     * @param taLastName - String TA Last Name
     * @param type - String HTA or UTA
     * @return status
     */
    public boolean addTA(String taLogin, String taFirstName, String taLastName, String type) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(t.tid) AS rowcount "
                    + "FROM ta AS t "
                    + "WHERE t.login == '" + taLogin + "'");
            int rows = rs.getInt("rowcount");
            if (rows == 0) {
                _statement.executeUpdate("INSERT INTO ta "
                        + "('login', 'firstname', 'lastname', 'type') "
                        + "VALUES "
                        + "('" + taLogin
                        + "', '" + taFirstName
                        + "', '" + taLastName
                        + "', '" + type
                        + "')");
            }
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not insert new row for ta: " + taLogin);
            closeConnection();
            return false;
        }
    }

    public Map<String, String> getStudents() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Check to see if TA has already BlackListed Student. If has not then adds Student to TA's blacklist
     * @param studentLogin - String Student
     * @param taLogin - String TA
     * @return status
     */
    public boolean blacklistStudent(String studentLogin, String taLogin) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(b.sid) AS rowcount, "
                    + "b.tid AS taid, "
                    + "b.sid AS studentid "
                    + "FROM backlist AS b "
                    + "INNER JOIN student AS s "
                    + "ON s.sid == b.sid "
                    + "INNER JOIN ta AS t "
                    + "ON t.tid == b.tid "
                    + "WHERE s.login == '" + studentLogin + "' "
                    + "AND t.login == '" + taLogin + "'");
            int rows = rs.getInt("rowcount");
            int studentID = rs.getInt("studentid");
            int taID = rs.getInt("taid");
            if (rows == 0) {
                _statement.executeUpdate("INSERT INTO blacklist "
                        + "('sid', 'tid') "
                        + "VALUES "
                        + "(" + studentID
                        + ", " + taID
                        + ")");
            }
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e, "Could not blacklist the student: " + studentLogin + " for ta: " + taLogin);
            closeConnection();
            return false;
        }
    }

    /**
     * Checks to see if the dist for an assignment is empty. If yes return true.
     * @param asgn - String Asgn Name
     * @return Boolean - Empty = true, Not = false
     */
    public boolean isDistEmpty(String asgn) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT COUNT(d.sid) AS rowcount " +
                                                   "FROM distribution AS d " +
                                                   "INNER JOIN asgn AS a " +
                                                    "ON a.aid == d.aid " +
                                                   "WHERE a.name == '" + asgn + "'");
            int rows = rs.getInt("rowcount");
            closeConnection();
            return rows == 0;
        } catch (Exception e) {
            new ErrorView(e, "Could not determine the size of the dist for: " + asgn);
            closeConnection();
            return false;
        }
    }

    /**
     * Return all students on any TA's blacklist
     * @return Collection - strings of all the student logins
     */
    public Collection<String> getBlacklistedStudents() {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin " +
                                                   "FROM student AS s " +
                                                   "INNER JOIN blacklist AS b " +
                                                    "ON b.sid == s.sid");
            ArrayList<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("student"));
            }
            closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get BlackListed Students from DB");
            closeConnection();
            return new ArrayList<String>();
        }
    }

    public boolean assignStudentToGrader(String studentLogin, String assignmentName, String taLogin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean unassignStudentFromGrader(String studentLogin, String assignmentName, String taLogin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get all the logins of the students assigned to a particular TA for an assignment.
     * Dist could be empty and will then return empty ArrayList
     * @param assignmentName - String Name of asgn
     * @param taLogin - String TA Login
     * @return Collection - list of student logins
     */
    public Collection<String> getStudentsAssigned(String assignmentName, String taLogin) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("SELECT s.login AS studlogin " +
                                                   "FROM student AS s " +
                                                   "INNER JOIN distribution AS d " +
                                                    "ON d.sid == s.sid " +
                                                   "INNER JOIN ta AS t " +
                                                    "ON d.tid == t.tid " +
                                                   "INNER JOIN assignment AS a " +
                                                    "ON d.aid == a.aid " +
                                                   "WHERE t.login == '" + taLogin + "' " +
                                                    "AND a.name == '" + assignmentName + "'");
            ArrayList<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("studlogin"));
            }
            closeConnection();
            return result;
        } catch (Exception e) {
            new ErrorView(e, "Could not get students assigned to ta: " + taLogin + " for assignment: " + assignmentName);
            closeConnection();
            return new ArrayList<String>();
        }
    }

    public boolean grantExtension(String studentLogin, String assignmentName, Calendar newDate, String note) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean grantExemption(String studentLogin, String assignmentName, String note) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Calendar getExtension(String studentLogin, String assignmentName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getExtensionNote(String studentLogin, String assignmentName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getExemptionNote(String studentLogin, String assignmentName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean enterGrade(String studentLogin, String assignmentName, double score, TimeStatus status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getStudentScore(String studentLogin, String assignmentName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNumberOfGrades(String assignmentName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean exportDatabase(File exportFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean resetDatabase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
