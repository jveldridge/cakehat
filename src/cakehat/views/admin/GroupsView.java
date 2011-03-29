package cakehat.views.admin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import cakehat.Allocator;
import cakehat.config.Assignment;
import cakehat.database.Group;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author aunger
 */
public class GroupsView extends javax.swing.JFrame {

    private FileFilter _groupFilter;
    private AdminView _backend;
    private Assignment _asgn;
    
    public GroupsView(AdminView backend, Assignment asgn) {
        _backend = backend;
        _asgn = asgn;
        _groupFilter = new GroupFilter();
        this.initComponents();
        this.setVisible(true);
        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.pack();
    }

    private void initComponents() {
        JButton removeGroupsButton = new JButton();
        JButton selectGroupsButton = new JButton();
        JLabel directionsLabel = new JLabel();

        removeGroupsButton.setText("Remove Groups");
        removeGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupsActionPerformed();
            }
        });

        selectGroupsButton.setText("Select Groups File");
        selectGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectGroupsFileActionPerformed();
            }
        });

        directionsLabel.setText("<html><p>Import Groups for Assignment: " + _asgn.getName() + "<ul><li>Select the groups file you would like to import.</li>" +
                                           "<li>Click Open to start the import process.</li>" +
                                           "<li>If that file contains invalid groups none will be<br /> added and a warning will apear.</li>" +
                                      "</ul></html>");

        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(directionsLabel);
        this.add(topPanel, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(selectGroupsButton);
        bottomPanel.add(removeGroupsButton);
        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void selectGroupsFileActionPerformed() {
        Collection<Group> groupAlready = null;
        try {
            groupAlready = Allocator.getDatabaseIO().getGroupsForAssignment(_asgn);
        } catch (SQLException ex) {
            new ErrorView(ex,"Groups could not be retrieved from the DB. We therefore can't determine if groups already exist.");
        }

        if (!groupAlready.isEmpty()) {
            int doRemove = JOptionPane.showConfirmDialog(this, "<html><p>Groups already exist for this assignment. They" +
                                                               "<br>must be removed before new groups can be created." +
                                                               "<br>Would you like to remove all the groups for the" +
                                                               "<br>assignment: " + _asgn.getName() + " now before adding" +
                                                               "<br>the new groups?</p>" +
                                                               "<p>Note: this will removed all information associated with" +
                                                               "<br>that group, including grades.</p></html>",
                    "Confirm Remove",
                    JOptionPane.YES_NO_OPTION);
            if (doRemove != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Allocator.getDatabaseIO().removeGroupsForAssignment(_asgn);
            } catch (SQLException ex) {
               new ErrorView(ex, "Groups could not be removed from the database.");
               return;
            }
        }


        JFileChooser fc = new JFileChooser();
        fc.setApproveButtonText("Import Groups File");
        fc.setFileFilter(_groupFilter);
        fc.setCurrentDirectory(Allocator.getPathServices().getCourseDir());
        fc.setMultiSelectionEnabled(false);
        int selection = fc.showOpenDialog(this);

        Set<String> studLogins;
        try {
            studLogins = Allocator.getDatabaseIO().getAllStudents().keySet();
        } catch (SQLException ex) {
            new ErrorView(ex, "Students could not be retrieved from the database; " +
                              "groups info. cannot be imported.  If this problem persists, " +
                              "please send an error report.");
            return;
        }

        if (selection == JFileChooser.APPROVE_OPTION) {

            Multimap<String, String> namesAndGroups = ArrayListMultimap.create();

            File groupFile = fc.getSelectedFile();
            Scanner gScanner = null;
            try {
                gScanner = new Scanner( new FileInputStream(groupFile));
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "File does not exist. Please try again.");
                return;
            }
            while (gScanner.hasNextLine()) {
                String group = gScanner.nextLine();
                group = group.replaceAll("\\s+", "");
                String[] groupParts = group.split(":");
                String groupName = groupParts[0];
                String[] groupMembers = groupParts[1].split(",");
                for (int i = 0; i < groupMembers.length; i++) {
                    if (!studLogins.contains(groupMembers[i])) {
                        JOptionPane.showMessageDialog(this, groupMembers[i] + " is not a student in the DB. Please fix the groups file.");
                        return;
                    }
                    else {
                        namesAndGroups.put(groupName, groupMembers[i]);
                    }
                }
            }
            try {
                Collection<Group> groups = new LinkedList<Group>();
                Map<String, Collection<String>> groupsMap = namesAndGroups.asMap();
                for (String groupName : groupsMap.keySet()) {
                    groups.add(new Group(groupName,groupsMap.get(groupName)));
                }
                Allocator.getDatabaseIO().setGroups(_asgn, groups);
            } catch (SQLException ex) {
                new ErrorView(ex, "Saving groups info to the database failed.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Group Import Successful!");
            _backend.updateGroupsCache(_asgn);
        }
    }

    private void removeGroupsActionPerformed() {
        try {
            int doRemove = JOptionPane.showConfirmDialog(this, "<html><p>Are you sure you want to remove all the groups for assignment: " + _asgn.getName() + "?</p>" +
                                                               "<p>Note: this will removed all information associated with that group, including grades.</p></html>",
                    "Confirm Remove",
                    JOptionPane.YES_NO_OPTION);
            if (doRemove != JOptionPane.YES_OPTION) {
                return;
            }
            Allocator.getDatabaseIO().removeGroupsForAssignment(_asgn);

            JOptionPane.showMessageDialog(this, "Groups removed successfully.");
            _backend.updateGroupsCache(_asgn);
        } catch (SQLException ex) {
            new ErrorView(ex, "Groups could not be removed from the database.");
        }
    }

    private class GroupFilter extends FileFilter {

        public boolean accept(File pathname) {
            if (pathname.isDirectory() || (pathname.isFile() && (pathname.getName().endsWith(".txt") || pathname.getName().endsWith(".grp")))) {
                return true;
            }
            else {
                return false;
            }
        }

        public String getDescription() {
            return ".txt or .grp";
        }
    }
}