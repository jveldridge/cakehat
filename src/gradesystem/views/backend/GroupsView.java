package gradesystem.views.backend;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gradesystem.config.HandinPart;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.application.Action;
import gradesystem.Allocator;

/**
 *
 * @author aunger
 */
public class GroupsView extends javax.swing.JFrame {

    private FileFilter _groupFilter;
    private HandinPart _handin;

    /** Creates new form GroupsView */
    public GroupsView(HandinPart handin) {
        _handin = handin;
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
        Open = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getActionMap(GroupsView.class, this);
        removeGroupsButton.setAction(actionMap.get("removeGroups")); // NOI18N
        removeGroupsButton.setName("removeGroupsButton"); // NOI18N

        Open.setAction(actionMap.get("OpenAction")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(GroupsView.class);
        Open.setText(resourceMap.getString("Open.text")); // NOI18N
        Open.setName("Open"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem1.setAction(actionMap.get("closeWindow")); // NOI18N
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(Open)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeGroupsButton)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeGroupsButton)
                    .addComponent(Open))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Open;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JButton removeGroupsButton;
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

    @Action
    public void removeGroups() {
        Allocator.getDatabaseIO().removeGroups(_handin);
    }

    @Action
    public void OpenAction() {
        JFileChooser fc = new JFileChooser();
        fc.setApproveButtonText("Import Groups File");
        fc.setFileFilter(_groupFilter);
        fc.setCurrentDirectory(new File(Allocator.getCourseInfo().getCourseDir()));
        fc.setMultiSelectionEnabled(false);
        int selection = fc.showOpenDialog(this);

        Set<String> studLogins = Allocator.getDatabaseIO().getAllStudents().keySet();

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
            Allocator.getDatabaseIO().setGroups(_handin, namesAndGroups.asMap());
        }
    }

    @Action
    public void closeWindow() {
        //this.dispose();
    }

}