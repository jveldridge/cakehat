/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledb;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Set;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import java.lang.Runtime;

/**
 *
 * @author psastras
 */
public class TADB {

    public static final String FILE_NAME = "cs015Database.db";
    private static final boolean regenerate_tables = false;
    private static final String[][] TABLES = new String[][]{
        {"create table taData (login text not null, comment text)", "create index login_names on taData (login)"},
        {"create index login_names on taData (login)"}
    };
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
        db = SqlJetDb.open(new File(FILE_NAME), true);
        String[] s = new String[db.getSchema().getTable(tableName).getColumns().size()];
        for (int i = 0; i < s.length; i++) {
            s[i] = db.getSchema().getTable(tableName).getColumns().get(i).getName();
        }
        return s;
    }

    public static ISqlJetCursor getAllData(String tableName) throws SqlJetException {
        return db.getTable(tableName).open();
    }

    public static ISqlJetCursor getData(String tableName, String login) throws SqlJetException {
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

    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }
}
