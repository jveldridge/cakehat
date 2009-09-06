package database_editor;

import simpledb.DatabaseInterops;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
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
public class GridView extends JTable {

    private static final long serialVersionUID = 1L;
    private TableColumn _idCol;
    private String _tableName;
    private boolean _refreshTable = false;

    public GridView() {
        super();
        //Make the table look somewhat decent
        this.removeAll();
        this.setBackground(Color.white);
        this.setRowHeight(20);
        this.setFillsViewportHeight(true);
        this.setGridColor(new Color(190, 214, 246));
        this.setForeground(new Color(79, 79, 79));
        this.setIntercellSpacing(new Dimension(3, 3));
        initDatabaseWatch();
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
            removeDatabaseWatch();
            super.setValueAt(aValue, row, column);
            this.getColumnModel().addColumn(_idCol);
            long rowId = Long.parseLong(this.getValueAt(this.getSelectedRow(), this.getColumnCount() - 1).toString());
            this.getColumnModel().removeColumn(_idCol);
            Object[] data = new Object[this.getColumnCount()];
            for (int i = 0; i < data.length; i++) {
                data[i] = this.getValueAt(this.getSelectedRow(), i);
            }
            DatabaseInterops.update(rowId, _tableName, data);
            if (_refreshTable) {
                _refreshTable = false;
                refresh(_tableName);
            }
            initDatabaseWatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a column to the current table.
     * @param colName
     * @throws java.lang.IllegalStateException
     */
    public void addColumn(String colName) throws IllegalStateException {
        DefaultTableModel model = (DefaultTableModel) this.getModel();
        TableColumn c = new TableColumn(model.getColumnCount());
        if (this.getAutoCreateColumnsFromModel()) {
            throw new IllegalStateException();
        }
        c.setHeaderValue(colName);
        this.addColumn(c);
        model.addColumn(colName);
    }
    private Timer _dbTimer = new Timer();

    /**
     * Watches the databse file for modifications to reload.
     */
    public void initDatabaseWatch() {
        TimerTask task = new DatabaseWatch(new File(DatabaseInterops.FILE_NAME)) {

            protected void onChange(File file) {
                removeDatabaseWatch();
                if (_tableName != null) {
                    refresh(_tableName);
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

    /**
     * Removes the currently selected rows from the grid and databse.
     */
    public void removeRows(String tableName) {
        removeDatabaseWatch();
        this.getColumnModel().addColumn(_idCol);
        DefaultTableModel m = (DefaultTableModel) this.getModel();
        int k = 0;
        for (int r : this.getSelectedRows()) {
            try {
                long rowId = Long.parseLong(this.getValueAt(r + k, m.getColumnCount() - 1).toString());
                DatabaseInterops.removeDatum(rowId, tableName);
                m.removeRow(r + k--); //When a row is removed all rows shift up by one
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.getColumnModel().removeColumn(_idCol);
        initDatabaseWatch();
    }

    /**
     * Adds a row to the grid and databse.
     * @param tableName
     */
    public void addRow(String tableName) {
        removeDatabaseWatch();
        try {
            this.getColumnModel().addColumn(_idCol);
            Object[] data1 = new Object[DatabaseInterops.getColumnNames(tableName).length];
            data1[0] = "";
            Object[] data2 = new Object[data1.length + 1];
            data2[data1.length] = DatabaseInterops.addDatum(tableName, data1);
            DefaultTableModel m = (DefaultTableModel) this.getModel();
            m.insertRow(this.getRowCount(), data2);
            this.getColumnModel().removeColumn(_idCol);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initDatabaseWatch();
    }
    private TableRowSorter<TableModel> _textFilter;

    /**
     * Grid text filter.
     * @param filterText
     */
    public void filter(String filterText) {
        if (filterText.length() == 0) {
            _textFilter.setRowFilter(null);
        } else {
            _textFilter.setRowFilter(RowFilter.regexFilter(filterText));
        }
    }

    /**
     * Refreshes the table to the database version.  Kind of craptastic because
     * we have to loard the whole table again.  But its okay for small cs015 tables.
     * @param tableName
     */
    public void refresh(String tableName) {
        if (this.isEditing()) {
            _refreshTable = true; //Prevent us from refreshing when editing a cell
            return;
        }
        try {
            if (!DatabaseInterops.isValidTable(tableName)) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeDatabaseWatch();
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
            _idCol = this.getColumnModel().getColumn(columnNames.length);
            this.getColumnModel().removeColumn(_idCol);
            ISqlJetCursor cursor = DatabaseInterops.getAllData(tableName);
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
        initDatabaseWatch();
    }

    /**
     * Save the current table to the database.  Kind of craptastic, since we
     * reset the whole table.  But the current sqljet version is missing
     * the sql commands for a lot of different table modifications.
     */
    public void save(String tableName) {
        removeDatabaseWatch();
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
        initDatabaseWatch();
    }
}
