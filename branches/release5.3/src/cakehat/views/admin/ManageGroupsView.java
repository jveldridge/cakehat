package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.database.assignment.Assignment;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author wyegelwe
 */
class ManageGroupsView extends JDialog{
    private final Assignment _asgn;
    private DnDStudentList _studentList;
    private DnDGroupTree _groupTree;
    
     ManageGroupsView(JFrame owner, Assignment asgn) {
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
                    new ErrorView(ex, "Unable to update data in cache.");
                }
            }
        });
        try {
            _studentList  = new DnDStudentList(_asgn);
            _groupTree = new DnDGroupTree(_asgn);
            _studentList.setGroupTree(_groupTree);
            _groupTree.setStudentList(_studentList);
        } catch (SQLException e) {
            new ErrorView(e, "Unable to get data to create ui");
        } catch (ServicesException e) {
            new ErrorView(e, "Unable to get data to create ui");
        }
        //Setup UI
        this.initUI();
        //Display
        this.setMinimumSize(new Dimension(500, 550));
        this.setPreferredSize(new Dimension(500, 550));
        this.pack();

        this.setResizable(true);
        this.setLocationRelativeTo(owner);
        this.setVisible(true);
    }

    private void initUI(){
        //General setup
        this.setLayout(new BorderLayout(0, 0));
        JPanel contentPanel = new JPanel();
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        //Student list
        Dimension studentListSize = new Dimension(180, Short.MAX_VALUE);
        _studentList.setMinimumSize(studentListSize);
        _studentList.setPreferredSize(studentListSize);
        _studentList.setMaximumSize(studentListSize);

        contentPanel.add(_studentList);
        contentPanel.add(Box.createHorizontalStrut(50));

        //Group Tree
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.add(_groupTree);

        Dimension groupTreeSize = new Dimension(220, Short.MAX_VALUE);
        groupPanel.setMinimumSize(groupTreeSize);
        groupPanel.setPreferredSize(groupTreeSize);
        groupPanel.setMaximumSize(groupTreeSize);

        contentPanel.add(groupPanel);
        this.add(contentPanel);
    }
}