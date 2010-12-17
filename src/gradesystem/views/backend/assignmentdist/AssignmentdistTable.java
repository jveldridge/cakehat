/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gradesystem.views.backend.assignmentdist;

import gradesystem.components.Table;


/**
 *
 * @author psastras
 */
public class AssignmentdistTable extends Table {
    public AssignmentdistTable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 1) ? true : false;
    }

}
