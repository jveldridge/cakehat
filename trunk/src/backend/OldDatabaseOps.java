/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import config.NonHandinPart;
import config.TA;
import utils.AssignmentType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Set;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import utils.Allocator;

/**
 *
 * @author psastras
 */
public class OldDatabaseOps {

    public static final String[] GRADE_RUBRIC_FIELDS = {"DQPoints", "ProjectPoints"};
    private static SqlJetDb db;
    public static final String DISTRIBUTION_TABLE = "assignment_dist";

    /**
     * Open the database.
     * @throws SqlJetException
     */
    public static void open() {
        try {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns string array containing all the students currently assigned to
     * be graded for the given project.  Returns empty string if proj not found.
     * @return String[]
     */
    public static String[] getStudentsAssigned(String projectName) {
        String[] columns = getColumnNames(DISTRIBUTION_TABLE);
        Arrays.sort(columns);
        int i = Arrays.binarySearch(columns, projectName);
        if (i < 0) {
            return new String[0];
        }
        try {
            ISqlJetCursor cursor = getAllData(DISTRIBUTION_TABLE);
            StringBuilder sb = new StringBuilder();
            while (!cursor.eof()) {
                String s = cursor.getString(columns[i]);
                if (s != null) {
                    sb.append(s + ",");
                }
                cursor.next();
            }

            cursor.close();
            return sb.toString().replace(" ", "").split(",");
        } catch (Exception e) {
        }
        return Arrays.toString(getColumnData(columns[i], DISTRIBUTION_TABLE)).replace(" ", "").split(",");
    }

    public static double getStudentDQScore(String assignmentName, String studentName) {
        try {
            ISqlJetCursor cursor = getData("grades_" + assignmentName, "stud_login_" + assignmentName, studentName);
            if (cursor.getString("studLogins").compareToIgnoreCase(studentName) == 0 && getAssignmentDQ(assignmentName) != 0) {
                return Double.parseDouble(cursor.getString("DQPoints"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     *
     * @param assignmentName
     * @param studLogin
     * @return
     */
    public static double getStudentProjectScore(final String assignmentName, final String studLogin) {
        try {
            ISqlJetCursor cursor = getData("grades_" + assignmentName, "stud_login_" + assignmentName, studLogin);
            if (cursor.getString("studLogins").compareToIgnoreCase(studLogin) == 0) {
                return Double.parseDouble(cursor.getString(GRADE_RUBRIC_FIELDS[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static double getAverage(String assignmentName) {
        try {
            ISqlJetCursor cursor = getAllData("grades_" + assignmentName);
            double sum = 0.0;
            //int i = getColumnNames("grades_" + assignmentName).length;
            double n = 0;
            while (!cursor.eof()) {
                double d = Double.parseDouble(cursor.getString(GRADE_RUBRIC_FIELDS[1]));
                sum += d;
//                if (i == 3) {
//                    sum += Double.parseDouble(cursor.getString(GRADE_RUBRIC_FIELDS[0]));
//                }
                if (d != 0) { //zeros are probably students that have dropped
                    n++;
                }
                cursor.next();
            }
            cursor.close();
            return sum / (n * getAssignmentTotal(assignmentName)) * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Clears the table of all data.
     * @param tableName
     * @throws SqlJetException
     */
    public static void resetTable(final String tableName) throws SqlJetException {
        db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb arg0) throws SqlJetException {
                db.getTable(tableName).clear();
                return null;
            }
        });

    }

    public static AssignmentType getAssignmentType(String assignName) {
        try {
            ISqlJetCursor cursor = getData("assignments", "assignmentNameIndex", assignName);
            if (cursor.getString("assignmentNames").compareToIgnoreCase(assignName) == 0) {
                return AssignmentType.getInstance((String) cursor.getString("type"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Utility method to get student names as array from table
     * @return
     * @throws org.tmatesoft.sqljet.core.SqlJetException
     */
    public static String[] getStudentNames() {
        try {
            return getColumnData("studLogin", "studlist");
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Returns the 1 if success.  Returns -1 if addition failed.
     * @param studentName
     * @return
     */
    public static long addStudent(final String studentName) {
        try {
            for (String s : getTableNames()) {
                if (s.startsWith("grades_")) {
                    if (getData(s, s.replaceFirst("grades_", "stud_login_"), studentName).getString("studLogins").trim().compareToIgnoreCase(studentName.trim()) == 0) {
                        continue;
                    }
                    String[] ss = new String[getColumnNames(s).length];
                    Arrays.fill(ss, "0");
                    ss[0] = studentName;
                    addDatum(s, (Object[]) ss);
                } else if (s.compareToIgnoreCase("studlist") == 0) {
                    if (getData(s, "studlist", studentName).getString("studLogin").trim().compareToIgnoreCase(studentName.trim()) == 0) {
                        continue;
                    }
                    addDatum("studlist", studentName);
                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Utility method to get ta names as array from table
     * @return
     * @throws org.tmatesoft.sqljet.core.SqlJetException
     */
    public static String[] getTANames() {
        try {
            return getColumnData("taLogin", "talist");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String[] getProjectNames() {
        try {
            String[] assignNames = getColumnData("assignmentNames", "assignments");
            String[] assignTypes = getColumnData("type", "assignments");
            ArrayList<String> al = new ArrayList<String>();
            for (int i = 0; i < assignNames.length; i++) {
                if (assignTypes[i].compareToIgnoreCase("project") == 0) {
                    al.add(assignNames[i]);
                }
            }
            return al.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Return a string array of ALL assignment names.
     * @return
     */
    public static String[] getAssignmentNames() {
        try {
            return getColumnData("assignmentNames", "assignments");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String[] getLabNames() {
        try {
            String[] assignNames = getColumnData("assignmentNames", "assignments");
            String[] assignTypes = getColumnData("type", "assignments");
            ArrayList<String> al = new ArrayList<String>();
            for (int i = 0; i < assignNames.length; i++) {
                if (assignTypes[i].compareToIgnoreCase("lab") == 0) {
                    al.add(assignNames[i]);
                }
            }
            return al.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String[] getHomeworkNames() {
        try {
            String[] assignNames = getColumnData("assignmentNames", "assignments");
            String[] assignTypes = getColumnData("type", "assignments");
            ArrayList<String> al = new ArrayList<String>();
            for (int i = 0; i < assignNames.length; i++) {
                if (assignTypes[i].compareToIgnoreCase("homework") == 0) {
                    al.add(assignNames[i]);
                }
            }
            return al.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Get the possible amount of points minus dq points
     * @param assignmentName
     * @return
     */
    public static int getAssignmentPoints(final String assignmentName) {
        return getAssignmentTotal(assignmentName) - getAssignmentDQ(assignmentName);
    }

    /**
     * Get the maximum score for the given assignment name.
     * @param assignmentName
     * @return
     */
    public static int getAssignmentTotal(final String assignmentName) {
        try {
            ISqlJetCursor cursor = getData("assignments", "assignmentNameIndex", assignmentName);
            if (cursor.getString("assignmentNames").compareToIgnoreCase(assignmentName) == 0) {
                return Integer.parseInt(cursor.getString("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get the maximum score for the given assignment name.
     * @param assignmentName
     * @return
     */
    public static int getAssignmentDQ(final String assignmentName) {
        try {
            ISqlJetCursor cursor = getData("assignments", "assignmentNameIndex", assignmentName);
            if (cursor.getString("assignmentNames").compareToIgnoreCase(assignmentName) == 0) {
                if (!cursor.getString("design").isEmpty()) {
                    return Integer.parseInt(cursor.getString("design"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param assignmentName
     * @param studLogin
     * @return
     */
    public static double getStudentEarnedScore(final String assignmentName, final String studLogin) {
        try {
            ISqlJetCursor cursor = getData("grades_" + assignmentName, "stud_login_" + assignmentName, studLogin);
            if (cursor.getString("studLogins").compareToIgnoreCase(studLogin) == 0) {
                return Double.parseDouble(cursor.getString(GRADE_RUBRIC_FIELDS[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static String[] getColumnData(final String colName, final String tableName) {
        try {
            LinkedList<String> ll = new LinkedList<String>();
            if (db == null) {
                db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
            }
            ISqlJetCursor cursor = db.getTable(tableName).open();
            while (!cursor.eof()) {
                ll.add(cursor.getString(colName));
                cursor.next();
            }
            return ll.toArray(new String[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Returns an array containing the list of student logins.
     * If no students to grade or ta login not found, will return a zero length array.
     * @param taName assignmentName
     * @return
     */
    public static String[] getStudentsToGrade(final String taLogin, final String assignmentName) {
        try {
            ISqlJetCursor cursor = getData(DISTRIBUTION_TABLE, "ta_login_dist", taLogin);
            String s = cursor.getString(assignmentName);
            return (s == null || s.isEmpty() || cursor.getString("taLogin").compareToIgnoreCase(taLogin) != 0) ? new String[0] : s.replace(" ", "").split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static boolean isDistEmpty(String asgn) {
        boolean isDistEmpty = true;
        for (TA grader : Allocator.getCourseInfo().getDefaultGraders()) {
            String[] s = OldDatabaseOps.getStudentsToGrade(grader.getLogin(), asgn);
            if (s.length != 0) {
                isDistEmpty = false;
            }
        }
        return isDistEmpty;
    }

    /**
     * Returns a string array of table names in the database.
     * @return
     * @throws SqlJetException
     */
    public static String[] getTableNames() throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        Set<String> s = db.getSchema().getTableNames();
        return s.toArray(new String[0]);
    }

    /**
     * Returns a string array of column names in the given table.
     * @param tableName
     * @return
     * @throws SqlJetException
     */
    public static String[] getColumnNames(final String tableName) {
        try {
            if (db == null) {
                db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
            }
            String[] s = new String[db.getSchema().getTable(tableName).getColumns().size()];
            for (int i = 0; i < s.length; i++) {
                s[i] = db.getSchema().getTable(tableName).getColumns().get(i).getName();
            }
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Returns a cursor with all the data of the given table.
     * @param tableName
     * @return
     * @throws SqlJetException
     */
    public static ISqlJetCursor getAllData(final String tableName) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }

        return db.getTable(tableName).open();
    }

    /**
     * Returns a cursor with just the data of the given lookup item found in the
     * given table.
     * @param tableName
     * @param indexName
     * @param lookupItem
     * @return
     * @throws SqlJetException
     */
    public static ISqlJetCursor getData(final String tableName, final String indexName, final String lookupItem) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        return db.getTable(tableName).lookup(indexName, lookupItem);
    }

    /**
     * Returns a cursor with just the row in the given table name.
     * @param tableName
     * @param rowid
     * @return
     */
    public static Object[] getDataRow(final String tableName, final long rowid) {
        try {
            ISqlJetCursor cursor = db.getTable(tableName).open();
            cursor.goTo(rowid);
            Object[] o = new String[getColumnNames(tableName).length];
            for (int i = 0; i < o.length; i++) {
                o[i] = cursor.getString(i);
            }
            cursor.close();
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a cursor with the given (row, column name) data.
     * @param tableName
     * @param rowid
     * @param columnName
     * @return
     * @throws SqlJetException
     */
    public static String getDataCell(final String tableName, final long rowid, final String columnName) throws SqlJetException {
        ISqlJetCursor cursor = db.getTable(tableName).open();
        try {
            if (cursor.goTo(rowid)) {
                return cursor.getString(columnName);

            }
        } finally {
            cursor.close();
        }
        return null;

    }

    /**
     * Create a new table in the database.  You should never need to call this.
     * It is simply provided in case something really really bad happens
     * to the database.
     * @param sqlNewTableString
     * @param sqlIndexString
     * @throws SqlJetException
     */
    public static void createTable(final String sqlNewTableString, final String sqlIndexString) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb arg0) throws SqlJetException {
                db.createTable(sqlNewTableString);
                db.createIndex(sqlIndexString);
                return null;
            }
        });
    }

    /**
     * Get the row number where the given string is found in the table.
     * @param tableName
     * @param indexName
     * @param objectName
     * @return
     */
    public static long getRowID(final String tableName, final String indexName, final String objectName) {
        try {
            return getData(tableName, indexName, objectName).getRowId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Remove the given row from the specified table.
     * @param rowid
     * @param tableName
     * @throws SqlJetException
     */
    public static void removeDatum(final long rowid, final String tableName) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb db) throws SqlJetException {
                ISqlJetCursor cursor = db.getTable(tableName).open();
                try {
                    if (cursor.goTo(rowid)) {
                        cursor.delete();
                    }
                } finally {
                    cursor.close();
                }
                return null;
            }
        });
    }

    /**
     * Checks to see if the table name is valid (does it exist in the database?)
     * @param tableName
     * @return
     * @throws SqlJetException
     */
    public static boolean isValidTable(final String tableName) {
        try {
            if (db == null) {
                db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
            }
            if (tableName == null) {
                return false;

            }
            for (String s : db.getSchema().getTableNames()) {
                if (s.compareTo(tableName) == 0) {
                    return true;

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds the data (as an argument list or array) to the end of the given
     * table.
     * @param tableName
     * @param data
     * @return
     * @throws SqlJetException
     */
    public static long addDatum(final String tableName, final Object... data) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        return (Long) db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb db) throws SqlJetException {
                return db.getTable(tableName).insert(data);
            }
        });
    }

    /**
     * Drops / removes the specified table from the database.
     * @param tableName
     * @throws SqlJetException
     */
    public static void dropTable(final String tableName) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb arg0) throws SqlJetException {
                db.dropTable(tableName);
                return null;
            }
        });
    }

    public static void runOnce() {
        try {
            if (db == null) {
                db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
            }
            if (!isValidTable("grades_References_bak")) {
                db.createTable("create table grades_References_bak (studLogins text not null, ProjectPoints text)");
                ISqlJetCursor c = getAllData("grades_References");
                while (!c.eof()) {
                    addDatum("grades_References_bak", c.getString("studLogins"), c.getString("ProjectPoints"));
                    c.next();
                }
                c.close();
            }

            //c.delete();
            db.dropTable("grades_References");
            System.out.println(Arrays.toString((db.getSchema().getIndexNames()).toArray(new String[0])));
//            db.dropIndex("stud_login_References");
            db.createTable("create table grades_References (studLogins text not null, ProjectPoints text)");
            db.createIndex("create index stud_login_References on grades_References (studLogins)");
            ISqlJetCursor c2 = getAllData("grades_References_bak");
            while (!c2.eof()) {
                addDatum("grades_References", c2.getString("studLogins"), c2.getString("ProjectPoints"));
                c2.next();
            }
            c2.close();
            //c.delete();
            db.dropTable("grades_References_bak");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces the values in the specified table at the given row number.  Values
     * may be given as a list of arguments or as an array.
     * @param rowid
     * @param tableName
     * @param values
     * @throws SqlJetException
     */
    public static void update(final long rowid, final String tableName, final Object... values) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb db) throws SqlJetException {
                ISqlJetCursor cursor = db.getTable(tableName).open();
                try {
                    if (cursor.goTo(rowid)) {
                        cursor.update(values);
                    }
                } finally {
                    cursor.close();
                }
                return null;
            }
        });
    }

    /**
     * Get a cursor getting the specified filter item from the given table.
     * @param tableName
     * @param indexName
     * @param filter
     * @return
     * @throws SqlJetException
     */
    public static ISqlJetCursor getItemWithFilter(final String tableName, final String indexName, final String filter) throws SqlJetException {
        return db.getTable(tableName).lookup(indexName, filter);
    }

    /**
     * Resets the whole database to beginning of semester settings.  This will
     * remove all the data in the database.  You may want to back up the database
     * first before running this.
     * @throws SqlJetException
     */
    public static void regenerateDatabase() throws SqlJetException {
        //Remove the old data
        if (db == null) {
            db = SqlJetDb.open(new File(Allocator.getCourseInfo().getDatabaseFilePath()), true);
        }
        for (String s : db.getSchema().getTableNames()) {
            db.dropTable(s);
        }
        for (String s : db.getSchema().getIndexNames()) {
            db.dropIndex(s);
        }

        db.createTable("create table assignments (assignmentNames text not null, type text, earlydate text, ontimedate text, latedate text, design text, total text)");
        db.createIndex("create index assignmentNameIndex on assignments (assignmentNames)");
        db.createTable("create table blacklist (taLogin text not null, studLogins text)");
        db.createIndex("create index ta_blist_logins on blacklist (taLogin)");
        db.createTable("create table studlist (studLogin text not null)");
        db.createIndex("create index stud_logins on studlist (studLogin)");
        db.createTable("create table talist (taLogin text not null)");
        for (String s : Allocator.getGeneralUtilities().getStudentLogins()) {
            addDatum("studlist", s);
        }
        String sqlCreateTableString1 = "Create table assignment_dist (taLogin text not null";

        /*
        // Original code that worked off of old configuration code
        for (Assignment a : ConfigurationManager.getAssignments()) {
            addDatum("assignments",
                    a.Name,
                    a.Type.toString(),
                    Allocator.getGeneralUtilities().getCalendarAsString(a.Early),
                    Allocator.getGeneralUtilities().getCalendarAsString(a.Ontime),
                    Allocator.getGeneralUtilities().getCalendarAsString(a.Late),
                    "" + ((a.Points.DQ == 0) ? "" : a.Points.DQ),
                    "" + a.Points.TOTAL);
            String sqlCreateTableString2 = "create table grades_" + a.Name + " (studLogins text not null";
            if (a.Points.DQ != 0) {
                sqlCreateTableString2 += ", " + GRADE_RUBRIC_FIELDS[0] + " text";
            }
            sqlCreateTableString2 += ", " + GRADE_RUBRIC_FIELDS[1] + " text";
            sqlCreateTableString2 += ")";
            db.createTable(sqlCreateTableString2);
            db.createIndex("create index stud_login_" + a.Name + " on grades_" + a.Name + " (studLogins)");
            if (a.Type == AssignmentType.PROJECT) {
                sqlCreateTableString1 += ", " + a.Name + " text";
            }
        }
        */

        //Huge hack to make it work with new configuration
        for (config.Assignment a : Allocator.getCourseInfo().getAssignments()) {
            String type = "";
            if(a.hasHandinPart()) type = "PROJECT";
            else if(a.hasLabParts()) type = "LAB";
            else type = "HOMEWORK";

            Calendar early = null, ontime = null, late = null;
            if(a.hasHandinPart())
            {
                early = a.getHandinPart().getTimeInformation().getEarlyDate();
                ontime = a.getHandinPart().getTimeInformation().getOntimeDate();
                late = a.getHandinPart().getTimeInformation().getLateDate();
            }

            int DQ = 0;
            if(a.hasHandinPart())
            {
                for(NonHandinPart p : a.getNonHandinParts())
                {
                    if(p.getName().equals("Design Questions"))
                    {
                        DQ = p.getPoints();
                    }
                }
            }


            addDatum("assignments",
                    a.getName(),
                    type,
                    Allocator.getGeneralUtilities().getCalendarAsString(early),
                    Allocator.getGeneralUtilities().getCalendarAsString(ontime),
                    Allocator.getGeneralUtilities().getCalendarAsString(late),
                    "" + ((DQ == 0) ? "" : DQ),
                    "" + a.getTotalPoints());
            String sqlCreateTableString2 = "create table grades_" + a.getName() + " (studLogins text not null";
            if (DQ != 0) {
                sqlCreateTableString2 += ", " + GRADE_RUBRIC_FIELDS[0] + " text";
            }
            sqlCreateTableString2 += ", " + GRADE_RUBRIC_FIELDS[1] + " text";
            sqlCreateTableString2 += ")";
            db.createTable(sqlCreateTableString2);
            db.createIndex("create index stud_login_" + a.getName() + " on grades_" + a.getName() + " (studLogins)");
            if (type.equals("PROJECT")) {
                sqlCreateTableString1 += ", " + a.getName() + " text";
            }
        }



        sqlCreateTableString1 += ")";
        db.createTable(sqlCreateTableString1);
        db.createIndex("create index ta_login_dist on assignment_dist (taLogin)");
        for (TA ta : Allocator.getCourseInfo().getTAs()) {
            addDatum(DISTRIBUTION_TABLE, ta.getLogin());
            addDatum("blacklist", ta.getLogin());
            addDatum("talist", new Object[]{ta.getLogin()});
        }
        //autoPopulate();
        /*
        //code that worked with original configuration code
        for (Assignment a : ConfigurationManager.getAssignments()) {
            for (String ss : Allocator.getGeneralUtilities().getStudentLogins()) {
                Object[] data;
                if (a.Points.DQ == 0) {
                    data = new String[2];
                    data[0] = ss;
                    data[1] = "0";
                } else {
                    data = new String[3];
                    data[0] = ss;
                    data[1] = "0";
                    data[2] = "0";
                }
                addDatum("grades_" + a.Name, data);
            }

        }
         */

        //huge hack to work with new configuration code
        for (config.Assignment a : Allocator.getCourseInfo().getAssignments()) {
            int DQ = 0;
            if(a.hasHandinPart())
            {
                for(NonHandinPart p : a.getNonHandinParts())
                {
                    if(p.getName().equals("Design Questions"))
                    {
                        DQ = p.getPoints();
                    }
                }
            }

            for (String ss : Allocator.getGeneralUtilities().getStudentLogins()) {
                Object[] data;
                if (DQ == 0) {
                    data = new String[2];
                    data[0] = ss;
                    data[1] = "0";
                } else {
                    data = new String[3];
                    data[0] = ss;
                    data[1] = "0";
                    data[2] = "0";
                }
                addDatum("grades_" + a.getName(), data);
            }

        }



    }

    /**
     * Closes the database.
     * @throws SqlJetException
     */
    public static void close() {
        try {
            db.close();
            db = null;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
