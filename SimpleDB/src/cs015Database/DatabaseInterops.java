/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
    public static final String[] GRADE_RUBRIC_FIELDS = {"Earned", "Total"};
    public static final String[] TA_LOGINS = {"Paul", "psastras", "jeldridg"};
    public static final String[] STUD_LOGINS = {"andy", "tree", "dog", "cat", "fox", "mouse", "cookie", "cake", "shoe", "sock", "puppet", "bird", "fish", "earth", "sun", "moon", "sky", "cloud", "bee", "honey", "apple", "orange", "tomato"};
    public static final String STUD_TABLE = "studlist";
    private static SqlJetDb db;

    public static void open() throws SqlJetException {
        db = SqlJetDb.open(new File(FILE_NAME), true);
    }

    public static void resetTable(final String tableName) throws SqlJetException {
        db = SqlJetDb.open(new File(FILE_NAME), true);
        db.runWriteTransaction(new ISqlJetTransaction() {

            public Object run(SqlJetDb arg0) throws SqlJetException {
                db.getTable(tableName).clear();
                return null;
            }
        });

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
    public static long addStudent(String studentName) {
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

    public static String[] getAssignmentNames() {
        try {
            return getColumnData("assignmentNames", "assignments");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static int getAssignmentTotal(String assignmentName) {
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

    public static String getStudentScore(String assignmentName, String studLogin) {
        try {
            ISqlJetCursor cursor = getData("grades_" + assignmentName, "stud_login_" + assignmentName, studLogin);
            if (cursor.getString("studLogins").compareToIgnoreCase(studLogin) == 0) {
                return cursor.getString("Earned");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] getColumnData(String colName, String tableName) {
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
    public static String[] getStudentsToGrade(String taLogin, String assignmentName) {
        try {
            ISqlJetCursor cursor = getData("assignment_dist", "ta_login_dist", taLogin);
            String s = cursor.getString(assignmentName);
            return (s == null || s.isEmpty() || cursor.getString("taLogin").compareToIgnoreCase(taLogin) != 0) ? new String[0] : s.replace(" ", "").split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String[] getTableNames() throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        Set<String> s = db.getSchema().getTableNames();
        return s.toArray(new String[0]);
    }

    public static String[] getColumnNames(String tableName) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        String[] s = new String[db.getSchema().getTable(tableName).getColumns().size()];
        for (int i = 0; i < s.length; i++) {
            s[i] = db.getSchema().getTable(tableName).getColumns().get(i).getName();
        }
        return s;
    }

    public static ISqlJetCursor getAllData(String tableName) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        return db.getTable(tableName).open();
    }

    public static ISqlJetCursor getData(String tableName, String indexName, String lookupItem) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        return db.getTable(tableName).lookup(indexName, lookupItem);
    }

    public static Object[] getDataRow(String tableName, long rowid) {
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

    public static String getDataCell(String tableName, long rowid, String columnName) throws SqlJetException {
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

    public static long getRowID(final String tableName, final String indexName, final String objectName) {
        try {
            return getData(tableName, indexName, objectName).getRowId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

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

//    /**
//     * Check for invalid characters in sql name.
//     * @return
//     */
//    public static boolean isValidName(String name) {
//
//    }
    /**
     * Check if the table actually exists
     */
    public static boolean isValidTable(final String tableName) throws SqlJetException {
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
        return false;
    }

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

    public static ISqlJetCursor getItemWithFilter(final String tableName, final String indexName, final String filter) throws SqlJetException {
        return db.getTable(tableName).lookup(indexName, filter);
    }

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

        //Grab data from config file


        //Add new tables for grades stuff should be read from xml rubric file

        db.createTable("create table assignments (assignmentNames text not null, total text)");
        db.createIndex("create index assignmentNameIndex on assignments (assignmentNames)");
        db.createTable("create table blacklist (taLogin text not null, studLogins text)");
        db.createIndex("create index ta_blist_logins on blacklist (taLogin)");
        db.createTable("create table studlist (studLogin text not null)");
        db.createIndex("create index stud_logins on studlist (studLogin)");
        db.createTable("create table talist (taLogin text not null)");

        String[] cmd = {"/bin/sh", "-c", "members cs015student"};
        String studentLogins = "";
        try {
            String s = null;
            Process p = Runtime.getRuntime().exec(cmd);
            int i = p.waitFor();
            if (i == 0) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((s = stdInput.readLine()) != null) {
                    studentLogins += s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        studentLogins.replaceFirst("cs015000", ""); //Remove the test account cause its stupid
        for (String s : studentLogins.trim().split(" ")) {
            addDatum("studlist", s);
        }
        String sqlCreateTableString1 = "Create table assignment_dist (taLogin text not null";
        for (String s : ASSIGNMENT_NAMES) {
            addDatum("assignments", s, "100");
            String sqlCreateTableString2 = "create table grades_" + s + " (studLogins text not null";

            for (String ss : GRADE_RUBRIC_FIELDS) {
                sqlCreateTableString2 += ", " + ss + " text";
            }
            sqlCreateTableString2 += ")";
            db.createTable(sqlCreateTableString2);
            db.createIndex("create index stud_login_" + s + " on grades_" + s + " (studLogins)");
            sqlCreateTableString1 += ", " + s + " text";
        }
        sqlCreateTableString1 += ")";
        db.createTable(sqlCreateTableString1);
        db.createIndex("create index ta_login_dist on assignment_dist (taLogin)");
        for (String s : TA_LOGINS) {
            addDatum("assignment_dist", s);
            addDatum("blacklist", s);
        }
        autoPopulate();
    }

    private static void autoPopulate() throws SqlJetException {
        //@TODO:tester...remove this when done

        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        String[] cmd = {"/bin/sh", "-c", "members cs015student"};
        String studentLogins = "";
        try {
            String s = null;
            Process p = Runtime.getRuntime().exec(cmd);
            int i = p.waitFor();
            if (i == 0) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((s = stdInput.readLine()) != null) {
                    studentLogins += s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        studentLogins.replaceFirst("cs015000", ""); //Remove the test account cause its stupid
        for (String s : ASSIGNMENT_NAMES) {
            for (String ss : studentLogins.trim().split(" ")) {
                Object[] data = new String[3];
                data[0] = ss;
                int grade = (int) (Math.random() * 50 + 50);
                data[1] = Integer.toString(grade);
                data[2] = "100";
                addDatum("grades_" + s, data);
            }
        }
        Object[] data = new String[1];
        for (String s : TA_LOGINS) {
            data[0] = s;
            addDatum("talist", data);
        }

    }

    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }
}
