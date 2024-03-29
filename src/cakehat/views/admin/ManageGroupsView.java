package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author wyegelwe
 */
class ManageGroupsView extends JDialog {
    
    private final Assignment _asgn;
    private DnDStudentList _studentList;
    private DnDGroupTree _groupTree;
    
    ManageGroupsView(Window owner, Assignment asgn) {
        super(owner, "Manage groups | " + asgn.getName(), ModalityType.APPLICATION_MODAL);
        
        _asgn = asgn;
        
        //Close operation
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent we) {
                try {
                    Allocator.getDataServices().updateDataCache();
                } catch (ServicesException ex) {
                    ErrorReporter.report("Unable to update data in cache.", ex);
                }
            }
        });
        
        //Create main UI elements
        try {
            _studentList  = new DnDStudentList(_asgn);
            _groupTree = new DnDGroupTree(_asgn);
            _studentList.setGroupTree(_groupTree);
            _groupTree.setStudentList(_studentList);
        } catch (SQLException e) {
            ErrorReporter.report("Unable to get data to create UI", e);
        } catch (ServicesException e) {
            ErrorReporter.report("Unable to get data to create UI", e);
        }
        
        //Setup UI
        this.initUI();
        
        //Display
        this.setMinimumSize(new Dimension(500, 450));
        this.setPreferredSize(new Dimension(500, 450));
        this.pack();
        this.setResizable(true);
        this.setLocationRelativeTo(owner);
        this.setVisible(true);
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        this.add(contentPanel, BorderLayout.CENTER);
        contentPanel.add(_studentList);
        contentPanel.add(_groupTree);
    }
}