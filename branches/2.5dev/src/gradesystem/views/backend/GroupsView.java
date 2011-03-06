package gradesystem.views.backend;

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
import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.database.Group;
import gradesystem.views.shared.ErrorView;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author aunger
 */
public class GroupsView extends javax.swing.JFrame {

    private FileFilter _groupFilter;
    private Assignment _asgn;

    /** Creates new form GroupsView */
    public GroupsView(Assignment asgn) {
        _asgn = asgn;
        _groupFilter = new GroupFilter();
        initComponents();
        this.setVisible(rootPaneCheckingEnabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        removeGroupsButton = new javax.swing.JButton();
        selectGroupsButton = new javax.swing.JButton();
        directionsLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N
        setResizable(false);

        removeGroupsButton.setText("Remove Groups");
        removeGroupsButton.setName("removeGroupsButton"); // NOI18N
        removeGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupsActionPerformed(evt);
            }
        });

        selectGroupsButton.setText("Select Groups File");
        selectGroupsButton.setName("selectGroupsButton"); // NOI18N
        selectGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectGroupsFileActionPerformed(evt);
            }
        });

        directionsLabel.setText("<html><ul><li>Select the groups file you would like to import.</li><li>Click Open to start the import process.</li><li>If that file contains invalid groups none will be<br /> added and a warning will apear.</li></ul></html>");
        directionsLabel.setName("directionsLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(directionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(selectGroupsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeGroupsButton)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(directionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeGroupsButton)
                    .addComponent(selectGroupsButton))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectGroupsFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectGroupsFileActionPerformed
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
                String[] groupParts = group.split("\\s");
                String groupName = groupParts[0];
                for (int i = 1; i < groupParts.length; i++) {
                    if (!studLogins.contains(groupParts[i])) {
                        JOptionPane.showMessageDialog(this, groupParts[i] + " is not a student in the DB. Please fix the groups file.");
                    }
                    else {
                        namesAndGroups.put(groupName, groupParts[i]);
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
                new ErrorView(ex, "Saving groups info. to the database failed.");
            }
        }
    }//GEN-LAST:event_selectGroupsFileActionPerformed

    private void removeGroupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupsActionPerformed
        try {
            Allocator.getDatabaseIO().removeGroupsForAssignment(_asgn);
        } catch (SQLException ex) {
            new ErrorView(ex, "Groups could not be removed from the database.");
        }
    }//GEN-LAST:event_removeGroupsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel directionsLabel;
    private javax.swing.JButton removeGroupsButton;
    private javax.swing.JButton selectGroupsButton;
    // End of variables declaration//GEN-END:variables

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
            return "only files that end in .txt or .grp";
        }
    }
}