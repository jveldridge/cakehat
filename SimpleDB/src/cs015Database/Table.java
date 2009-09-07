/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;
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
        this.setRowHeight(20);
        this.setFillsViewportHeight(true);
        this.setGridColor(new Color(190, 214, 246));
        this.setForeground(new Color(79, 79, 79));
        this.setIntercellSpacing(new Dimension(3, 3));
        this.setDragEnabled(false);
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
}
