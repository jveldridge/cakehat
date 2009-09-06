/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import java.io.File;
import java.util.Set;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.internal.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 *
 * @author psastras
 */
public class DatabaseInterops {

    public static final String FILE_NAME = "cs015Database.db";
    private static final boolean regenerate_tables = false;
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

    public static ISqlJetCursor getData(String tableName, String login) throws SqlJetException {
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        return db.getTable(tableName).lookup("login_names", login);
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
        if (db == null) {
            db = SqlJetDb.open(new File(FILE_NAME), true);
        }
        for (String s : db.getSchema().getTableNames()) {
            db.dropTable(s);
        }
        for (String s : db.getSchema().getIndexNames()) {
            db.dropIndex(s);
        }

        //Add new tables  should be read from xml file

        db.createTable("create table assignments (assignmentNames text not null)");
        db.createIndex("create index assignmentNameIndex on assignments (assignmentNames)");
        db.createTable("create table gradingDist (taLogin text not null, Clock text, LiteBrite text, Cartoon text)");
        db.createIndex("create index taLoginDist on gradingDist (taLogin)");
    }

    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }
}
