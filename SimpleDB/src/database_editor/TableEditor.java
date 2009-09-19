/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database_editor;

import cs015Database.DatabaseInterops;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author psastras
 */
public class TableEditor extends cs015Database.Table {

    private Timer _dbTimer = new Timer();
    private String _tableName;

    public TableEditor() {
        super();
        this.setRowHeight(20);
        this.setFillsViewportHeight(true);
        this.setGridColor(new Color(190, 214, 246));
        this.setForeground(new Color(79, 79, 79));
        this.setIntercellSpacing(new Dimension(3, 3));
        initDatabaseWatch();
    }

    public void refresh(String tableName) {
        if (!DatabaseInterops.isValidTable(tableName)) {
            return;
        }
        this.removeAll();
        this.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{}));
        DefaultTableModel m = (DefaultTableModel) this.getModel();
        _textFilter = new TableRowSorter<TableModel>(m);
        this.setRowSorter(_textFilter);
        String[] colNames = DatabaseInterops.getColumnNames(tableName);
        for (String s : colNames) {
            m.addColumn(s);
        }
        m.addColumn("rowID");
        this.removeColumn(this.getColumnModel().getColumn(this.getColumnCount() - 1));
        try {
            ISqlJetCursor cursor = DatabaseInterops.getAllData(tableName);
            while (!cursor.eof()) {
                Object[] rowData = new Object[colNames.length + 1];
                for (int i = 0; i < colNames.length; i++) {
                    rowData[i] = cursor.getString(colNames[i]);
                }
                rowData[rowData.length - 1] = cursor.getRowId();
                m.addRow(rowData);
                cursor.next();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initDatabaseWatch();
    }

    public void removeRows(String tableName) {
        removeDatabaseWatch();
        DefaultTableModel m = (DefaultTableModel) this.getModel();
        int k = 0;
        try {
            for (int i : this.getSelectedRows()) {
                long rowId = Long.parseLong((String) m.getValueAt(i + k, m.getColumnCount() - 1));
                DatabaseInterops.removeDatum(rowId, tableName);
                m.removeRow(i + k--);
            }
        } catch (Exception e) {
        }
    }

    public void initDatabaseWatch() {
        TimerTask task = new DatabaseWatch(new File(DatabaseInterops.FILE_NAME)) {

            protected void onChange(File file) {
                removeDatabaseWatch();
                if (_tableName != null) {
//                    JOptionPane pane = new JOptionPane(
//                            "To be or not to be ?\nThat is the question.");
//                    Object[] options = new String[]{"To be", "Not to be"};
//                    pane.setOptions(options);
//                    JDialog dialog = pane.createDialog(new JFrame(), "Dilaog");
//                    dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
//                    dialog.setVisible(true);
//                    Object obj = pane.getValue();
//                    int result = -1;
//                    for (int k = 0; k < options.length; k++) {
//                        if (options[k].equals(obj)) {
//                            result = k;
//                        }
//                    }
//                    System.out.println("User's choice: " + result);
//                    if (result == 1) {
                    refresh(_tableName);
//                    }
                }
                initDatabaseWatch();
            }
        };
        _dbTimer.schedule(task, new Date(), 100);
    }

    /**
     * Removes the database watch (useful when saving or modifying the databse
     * to prevent us from getting stuck in a loop detecting changes and writing
     * changes)
     */
    public void removeDatabaseWatch() {
        _dbTimer.cancel();
        _dbTimer.purge();
        _dbTimer = new Timer();
    }
}
