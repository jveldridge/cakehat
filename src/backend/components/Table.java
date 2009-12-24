/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.components;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Paul
 */
public class Table extends JTable {

    protected TableRowSorter<TableModel> _textFilter;

    public Table() {
        super();
        //Make the table look somewhat decent
        this.removeAll();
        this.setBackground(Color.white);
        this.setFillsViewportHeight(true);
        this.setGridColor(Color.white);
        this.getTableHeader().setReorderingAllowed(false);
        this.setRowHeight(17);
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

    public void applyFilterSorter() {
        _textFilter = new TableRowSorter<TableModel>((DefaultTableModel) this.getModel());
        this.setRowSorter(_textFilter);
    }

    /**
     * Grid text filter.
     * @param filterText
     */
    public void filter(String filterText) {
        if (filterText.length() == 0) {
            _textFilter.setRowFilter(null);
        } else {
            _textFilter.setRowFilter(RowFilter.regexFilter(filterText));
            _textFilter.sort();
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
