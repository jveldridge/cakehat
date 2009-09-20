/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import cs015.tasupport.grading.config.Assignment;
import cs015.tasupport.grading.config.AssignmentType;
import cs015.tasupport.grading.config.ConfigurationManager;
import cs015.tasupport.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 *
 * @author psastras
 */
public class DatabaseInterops {

    public static final String FILE_NAME = "cs015Database.db";
    public static final String[] ASSIGNMENT_NAMES = {"Objects", "ClockDesign", "Clock", "LiteBriteDesign", "LiteBrite", "References", "TASafeHouse", "CartoonDesign", "Cartoon", "SwarmDesign", "Swarm", "TetrisDesign", "Tetris", "PizzaDex", "lab0", "lab1", "lab2", "lab3", "lab4", "lab5", "lab6", "lab7"};
    public static final String[] GRADE_RUBRIC_FIELDS = {"DQPoints", "ProjectPoints"};
    public static final String[] TA_LOGINS = {"Paul", "psastras", "jeldridg"};
    public static final String STUD_TABLE = "studlist";
    private static SqlJetDb db;

    /**
     * Open the database.
     * @throws SqlJetException
     */
    public static void open() throws SqlJetException {
        db = SqlJetDb.open(new File(FILE_NAME), true);
    }

    /**
     * Clears the table of all data.
     * @param tableName
     * @throws SqlJetException
     */
    public static void resetTable(final String tableName) throws SqlJetException {
        db = SqlJetDb.open(new File(FILE_NAME), true);
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
                if (s.startsWith("grade")) {
                    addDatum(s, new Object[]{studentName, "", "" + getAssignmentTotal(s.split("_")[1])});
                } else if (s.compareToIgnoreCase("studlist") == 0) {
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
    public static String getStudentScore(final String assignmentName, final String studLogin) {
        try {
            ISqlJetCursor cursor = getData("grades_" + assignmentName, "stud_login_" + assignmentName, studLogin);
            if (cursor.getString("studLogins").compareToIgnoreCase(studLogin) == 0) {
                return cursor.getString("ProjectPoints");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] getColumnData(final String colName, final String tableName) {
        try {
            LinkedList<String> ll = new LinkedList<String>();
            if (db == null) {
                db = SqlJetDb.open(new File(FILE_NAME), true);
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
            ISqlJetCursor cursor = getData("assignment_dist", "ta_login_dist", taLogin);
            String s = cursor.getString(assignmentName);
            return (s == null || s.isEmpty() || cursor.getString("taLogin").compareToIgnoreCase(taLogin) != 0) ? new String[0] : s.replace(" ", "").split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Returns a string array of table names in the database.
     * @return
     * @throws SqlJetException
     */
    public static String[] getTableNames() throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
                db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
                db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb arg0) throws SqlJetException {
                db.dropTable(tableName);
                return null;
            }
        });
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
            db = SqlJetDb.open(new File(FILE_NAME), true);
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
        for (String s : Utils.getCS015Students()) {
            addDatum("studlist", s);
        }
        String sqlCreateTableString1 = "Create table assignment_dist (taLogin text not null";
        for (Assignment a : ConfigurationManager.getAssignments()) {
            addDatum("assignments", a.Name, a.Type.toString(), Utils.getCalendarAsString(a.Early), Utils.getCalendarAsString(a.Ontime), Utils.getCalendarAsString(a.Late), "" + ((a.Points.DQ == 0) ? "" : a.Points.DQ), "" + a.Points.TOTAL);
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
//            if (a.Points.DQ != 0) {
//                addDatum("assignments", a.Name + "Design", "DESIGN", "", "", "", "" + a.Points.DQ);
//                sqlCreateTableString2 = "create table grades_" + a.Name + "Design (studLogins text not null";
//                for (String ss : GRADE_RUBRIC_FIELDS) {
//                    sqlCreateTableString2 += ", " + ss + " text";
//                }
//                sqlCreateTableString2 += ")";
//                db.createTable(sqlCreateTableString2);
//                db.createIndex("create index stud_login_" + a.Name + "Design" + " on grades_" + a.Name + "Design (studLogins)");
//                sqlCreateTableString1 += ", " + a.Name + "Design text";
//            }
        }
        sqlCreateTableString1 += ")";
        db.createTable(sqlCreateTableString1);
        db.createIndex("create index ta_login_dist on assignment_dist (taLogin)");
        for (String s : ConfigurationManager.getTALogins()) {
            addDatum("assignment_dist", s);
            addDatum("blacklist", s);
            addDatum("talist", new Object[]{s});
        }
        for (Assignment a : ConfigurationManager.getAssignments()) {
            for (String ss : Utils.getCS015Students()) {
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
    }

    private static void autoPopulate() throws SqlJetException {
        //@TODO:tester...remove this when done

        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        for (Assignment a : ConfigurationManager.getAssignments()) {
            for (String ss : Utils.getCS015Students()) {
                Object[] data = new String[2];
                data[0] = ss;
                int grade = (int) (Math.random() * 50 + 50);
                data[1] = Integer.toString(grade);
                addDatum("grades_" + a.Name, data);
                if (a.Points.DQ != 0) {
                    addDatum("grades_" + a.Name + "Design", data);
                }
            }

        }
    }

    /**
     * Closes the database.
     * @throws SqlJetException
     */
    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }
}
