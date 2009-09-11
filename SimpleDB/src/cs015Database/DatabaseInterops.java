/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import java.io.File;
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
    public static final String[] ASSIGNMENT_NAMES = {"Clock", "LiteBrite", "TASafeHouse", "Cartoon", "Swarm", "Tetris", "PizzaDex", "lab0", "lab1", "lab2", "lab3", "lab4", "lab5", "lab6", "lab7"};
    public static final String[] GRADE_RUBRIC_FIELDS = {"BaseGrade", "Extras", "LateEarly", "Total"};
    public static final String[] TA_LOGINS = {"Paul", "psastras", "jeldridg"};
    public static final String[] STUD_LOGINS = {"andy", "tree", "dog", "cat", "fox", "mouse", "cookie", "cake", "shoe", "sock", "puppet", "bird", "fish", "earth", "sun", "moon", "sky", "cloud", "bee", "honey", "apple", "orange", "tomato"};
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

    public static String getDatum(String tableName, long rowid) throws SqlJetException {
        ISqlJetCursor cursor = db.getTable(tableName).open();
        try {
            if (cursor.goTo(rowid)) {
                return cursor.getString("login");
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
      

        //Add new tables  should be read from xml file

        db.createTable("create table assignments (assignmentNames text not null)");
        db.createIndex("create index assignmentNameIndex on assignments (assignmentNames)");
        db.createTable("create table blacklist (taLogin text not null)");

        db.createTable("create table studlist (studLogin text not null)");
        db.createIndex("create index stud_logins on studlist (studLogin)");
        for(String s : STUD_LOGINS) {
            addDatum("studlist", s);
        }
        String sqlCreateTableString1 = "Create table assignment_dist (taLogin text not null";
        for (String s : ASSIGNMENT_NAMES) {
            addDatum("assignments", s);
            String sqlCreateTableString2 = "create table grades_" + s + " (studLogins text not null";

            for (String ss : GRADE_RUBRIC_FIELDS) {
                sqlCreateTableString2 += ", " + ss + " text";
            }
            sqlCreateTableString2 += ")";
            db.createTable(sqlCreateTableString2);
            sqlCreateTableString1 += ", " + s + " text";
        }
        sqlCreateTableString1 += ")";
        db.createTable(sqlCreateTableString1);
        db.createIndex("create index taLoginDist on assignment_dist (taLogin)");
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
        for (String s : ASSIGNMENT_NAMES) {
            for (String ss : STUD_LOGINS) {
                Object[] data = new String[5];
                data[0] = ss;
                int grade = (int) (Math.random() * 50 + 50);
                data[1] = Integer.toString(grade);
                data[2] = Integer.toString((int) (Math.random() * 5));
                data[3] = "0";
                data[4] = Integer.toString(Integer.parseInt((String) data[1]) + Integer.parseInt((String) data[2]) + Integer.parseInt((String) data[3]));
                addDatum("grades_" + s, data);
            }
        }

    }

    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }
}
