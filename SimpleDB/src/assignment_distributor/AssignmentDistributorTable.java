/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package assignment_distributor;

import cs015Database.Table;

/**
 *
 * @author psastras
 */
public class AssignmentDistributorTable extends Table {
    public AssignmentDistributorTable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 1) ? true : false;
    }

}
