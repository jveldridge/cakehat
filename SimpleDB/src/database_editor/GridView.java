package database_editor;

import cs015Database.DatabaseInterops;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 * Provides a grid view which automatically handles database interactions.
 * @author psastras 
 */
public class GridView extends cs015Database.Table {

    private static final long serialVersionUID = 1L;
    private TableColumn _idCol;
    private String _tableName;
    private boolean _refreshTable = false;

    public GridView() {
        super();
        this.setRowHeight(20);
        this.setFillsViewportHeight(true);
        this.setGridColor(new Color(190, 214, 246));
        this.setForeground(new Color(79, 79, 79));
        this.setIntercellSpacing(new Dimension(3, 3));
    }

    /**
     * Alternate row color backgrounds (cause overriding the prepareRender is
     * perfectly obvious...thanks swing.
     * @param renderer
     * @param rowIndex
     * @param vColIndex
     * @return
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
            c.setBackground(new Color(247, 250, 255));
        } else if (!isCellSelected(rowIndex, vColIndex)) {
            c.setBackground(Color.white);
        }
        return c;
    }

    /**
     * This gets called whenver a cell is changed/modified.  So we can add it
     * to the database.
     * @param aValue
     * @param row
     * @param column
     */
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            super.setValueAt(aValue, row, column);
            long rowId = Long.parseLong(this.getModel().getValueAt(this.getSelectedRow(), this.getModel().getColumnCount() - 1).toString());
            Object[] data = new Object[this.getColumnCount()];
            for (int i = 0; i < data.length; i++) {
                data[i] = this.getValueAt(this.getSelectedRow(), i);
            }
            DatabaseInterops.update(rowId, _tableName, data);
            if (_refreshTable) {
                _refreshTable = false;
                refresh(_tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the currently selected rows from the grid and databse.
     */
    public void removeRows(String tableName) {
        try {
            DefaultTableModel m = (DefaultTableModel) this.getModel();
            for (int i = 0; i < this.getSelectedRows().length; i++) {

                long rowId = Long.parseLong(m.getValueAt(this.convertRowIndexToModel(this.getSelectedRow()), m.getColumnCount() - 1).toString());
                DatabaseInterops.removeDatum(rowId, tableName);
            }
            m.removeRow(this.convertRowIndexToModel(this.getSelectedRow()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a row to the grid and databse.
     * @param tableName
     */
    public void addRow(String tableName) {
        try {
            Object[] data1 = new Object[DatabaseInterops.getColumnNames(tableName).length];
            data1[0] = "";
            Object[] data2 = new Object[data1.length + 1];
            data2[data1.length] = DatabaseInterops.addDatum(tableName, data1);
            DefaultTableModel m = (DefaultTableModel) this.getModel();
            m.insertRow(this.getRowCount(), data2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    /**
     * Refreshes the table to the database version.  Kind of craptastic because
     * we have to loard the whole table again.  But its okay for small cs015 tables.
     * @param tableName
     */
    public void refresh(String tableName) {
//        if (this.isEditing()) {
//            _refreshTable = true; //Prevent us from refreshing when editing a cell
//            return;
//        }
//        try {
//            if (!DatabaseInterops.isValidTable(tableName)) {
//                return;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        this.setVisible(false);
        if (!DatabaseInterops.isValidTable(tableName)) {
            return;
        }
        _tableName = tableName;
        this.removeAll();
        this.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{}));
        DefaultTableModel m = (DefaultTableModel) this.getModel();
        _textFilter = new TableRowSorter<TableModel>(m);
        this.setRowSorter(_textFilter);
        try {
            String[] columnNames = DatabaseInterops.getColumnNames(tableName);
            for (String s : columnNames) {
                m.addColumn(s);
            }
            m.addColumn("rowID"); //Hidden rowId information
            this.removeColumn(this.getColumnModel().getColumn(this.getColumnCount() - 1));
            ISqlJetCursor cursor = DatabaseInterops.getAllData(tableName);
            if (cursor == null) {
                return;
            }
            try {
                while (!cursor.eof()) {
                    Object[] rowData = new Object[columnNames.length + 1];
                    for (int i = 0; i < columnNames.length; i++) {
                        rowData[i] = cursor.getString(columnNames[i]);
                    }
                    rowData[rowData.length - 1] = cursor.getRowId();
                    m.addRow(rowData);
                    cursor.next();
                }

            } finally {
                cursor.close();
            }
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
        this.repaint();
        this.setVisible(true);
    }

    /**
     * Save the current table to the database.  Kind of craptastic, since we
     * reset the whole table.  But the current sqljet version is missing
     * the sql commands for a lot of different table modifications.
     */
    public void save(String tableName) {
        try {
            DatabaseInterops.resetTable(tableName);
            Object[] rowData = new Object[(this.getColumnCount()) - 1];
            for (int i = 0; i < this.getRowCount(); i++) {
                for (int j = 0; j < this.getColumnCount() - 1; j++) {
                    rowData[j] = this.getValueAt(i, j);
                }
                DatabaseInterops.addDatum(tableName, rowData);
            }
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }
}
