package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * all DB accessor and mutator methods for cakehat
 * @author alexku
 */
public class DBWrapper {

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
            new ErrorView(e,"Could not open a connection to the DB.");
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
                new ErrorView(e,"Could not close connection to the DB.");
            }
    }

    /**
     * delete's assignment's current distribution and adds a new distribution
     * @param messageFrame - frame in which errors will show up
     * @param asgn - string of assignment name
     * @param distribution - hashmap of ta login to arraylist of student logins
     */
    public static void setAsgnDist(JFrame messageFrame, String asgn, HashMap<String,ArrayList<String>> distribution) {
        //add the distribution to the DB
        openConnection();
        try {
            HashMap<String,Integer> studentIDs = new HashMap<String,Integer>();
            HashMap<String,Integer> taIDs = new HashMap<String,Integer>();
            int asgnID;

            ResultSet rs = _statement.executeQuery("SELECT s.login, s.sid FROM student AS s");
            while (rs.next()) {
                studentIDs.put(rs.getString("login"), rs.getInt("sid"));
            }
            rs = _statement.executeQuery("SELECT t.login, t.tid FROM ta AS t");
            while (rs.next()) {
                taIDs.put(rs.getString("login"), rs.getInt("tid"));
            }
            rs = _statement.executeQuery("SELECT a.aid FROM asgn AS a WHERE a.name == '"+asgn+"'");
            rs.first();
            asgnID = rs.getInt("tid");

            rs = _statement.executeQuery("DELETE FROM asgn WHERE name == '"+asgn+"'");

            String insertCommand = "INSERT INTO distribution ('aid', 'sid', 'tid') VALUES"; //start of insert command
            for (String ta : distribution.keySet()) {
                ArrayList<String> distributedStudents = distribution.get(ta);
                for (String student : distributedStudents) {
                    insertCommand += " (" + asgnID + ", " + student + ", " + ta + "),";
                }
            }
            if (insertCommand.endsWith(",")) { //cut off trailing comma
                insertCommand = insertCommand.substring(0, insertCommand.length() - 1);
            }
            rs = _statement.executeQuery(insertCommand);
            JOptionPane.showMessageDialog(messageFrame, "Assignments have been successfully distributed to the grading TAs.", "Success", JOptionPane.INFORMATION_MESSAGE);
            closeConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(messageFrame, "There was an error distributing the assignments to the grading TAs.", "Error", JOptionPane.ERROR_MESSAGE);
            closeConnection();
        }
    }

    /**
     * gets a single TA's blacklist from the DB as arraylist of student logins
     * @param messageFrame - frame in which errors will show up
     * @param taLogin - string of single TA's login
     * @return list of student logins that have been blacklisted by the ta
     */
    public static ArrayList<String> getBlacklist(JFrame messageFrame, String taLogin) {
        openConnection();
        try {
            ArrayList<String> blackList = new ArrayList<String>();

            ResultSet rs = _statement.executeQuery("SELECT s.login " +
                                                  "FROM student AS s " +
                                                  "INNER JOIN distribution AS d " +
                                                    "ON d.sid == s.sid " +
                                                  "INNER JOIN ta AS t " +
                                                    "ON d.tid == t.tid " +
                                                  "WHERE t.login == " + taLogin);
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
     *
     * @param assignmentName
     * @return
     */
    public static boolean addAssignment(String assignmentName) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("INSERT INTO asgn " +
                                                    "('name') " +
                                                  "VALUES " +
                                                    "('" + assignmentName +
                                                    "')");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e,"Could not insert new row for assignment.");
            closeConnection();
            return false;
        }
    }

    public static boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("INSERT INTO student " +
                                                    "('login', 'firstname', 'lastname') " +
                                                   "VALUES " +
                                                    "('" + studentLogin +
                                                    "', '" + studentFirstName +
                                                    "', '" + studentLastName +
                                                    "')");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e,"Could not insert new row for student.");
            closeConnection();
            return false;
        }
    }

    public static boolean disableStudent(String studentLogin) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("UPDATE student " +
                                                   "SET " +
                                                     "enabled=0 " +
                                                   "WHERE " +
                                                     "login == '" + studentLogin + "'");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e,"Could not update student "+studentLogin+" to be disabled.");
            closeConnection();
            return false;
        }
    }

    public static boolean enableStudent(String studentLogin) {
        openConnection();
        try {
            ResultSet rs = _statement.executeQuery("UPDATE student " +
                                                   "SET " +
                                                     "enabled=1 " +
                                                   "WHERE " +
                                                     "login == '" + studentLogin + "'");
            closeConnection();
            return true;
        } catch (Exception e) {
            new ErrorView(e,"Could not update student "+studentLogin+" to be enabled.");
            closeConnection();
            return false;
        }
    }
}
