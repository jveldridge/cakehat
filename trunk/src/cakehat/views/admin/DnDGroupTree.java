package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.DbGroup;
import cakehat.database.Student;
import cakehat.database.assignment.Assignment;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import support.resources.icons.IconLoader;
import support.ui.FormattedLabel;

/**
 *
 * @author wyegelwe
 */
class DnDGroupTree extends JPanel {
    
    private static final DataFlavor GROUP_TREE_DATA_FLAVOR = new DataFlavor(DnDGroupTree.class, "GroupTree");
    private final JTree _groupTree;
    private final GroupTreeModel _model;
    private final Assignment _asgn;
    private final Set<Student> _enabledStudents;
    private final Set<String> _groupNames;
    private DnDStudentList _studentList;

    DnDGroupTree(Assignment asgn) throws ServicesException, SQLException {
        _asgn = asgn;
        
        _enabledStudents = Allocator.getDataServices().getEnabledStudents();
        _groupNames = new HashSet<String>();
        _model = new GroupTreeModel();
        _groupTree = new JTree(_model);
        
        initUI();
    }

    private void initUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.add(FormattedLabel.asHeader("Groups"));
        this.add(Box.createVerticalStrut(5));

        JScrollPane groupPane = new JScrollPane(_groupTree);
        groupPane.setAlignmentX(LEFT_ALIGNMENT);
        _groupTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        _groupTree.setRootVisible(false);
        _groupTree.setShowsRootHandles(true);
        _groupTree.setDragEnabled(true);
        _groupTree.setTransferHandler(new GroupTreeTransferHandler());

        // Makes use of the JTree's built in node editting functions
        _groupTree.setEditable(true);
        _groupTree.setCellEditor(new GroupEditor());
        _groupTree.setCellRenderer(new GroupTreeCellRenderer());

        // Expand the tree and prevent the user from collapsing the tree
        this.expandTree();
        _groupTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent tee) throws ExpandVetoException { }

            @Override
            public void treeWillCollapse(TreeExpansionEvent tee) throws ExpandVetoException {
                throw new ExpandVetoException(tee);
            }
        });
        
        this.add(groupPane);
        this.add(Box.createVerticalStrut(5));

        JPanel groupButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        groupButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
        groupButtonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        this.add(groupButtonPanel);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewGroup();
            }
        });
        groupButtonPanel.add(addButton);

        final JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _studentList.addStudents(removeSelectedElements());
            }
        });
        _groupTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                removeButton.setEnabled(_groupTree.getSelectionCount() != 0);
            }
        });
        groupButtonPanel.add(removeButton);
    }

    /**
     * Creates a new group and adds it to the tree. The model creates a new DbGroup with a user provided named.
     */
    private void createNewGroup() {
        String groupName = promptUserForName();
        if (groupName != null && !groupName.isEmpty()) {
            DbGroup group = new DbGroup(_asgn, groupName, new HashSet<Student>());
            try {
                Allocator.getDatabase().putGroups(ImmutableSet.of(group));
                _model.addGroup(group);
                this.updateTreeUI();
                
                //newly added group will be the last item in the tree
                _groupTree.setSelectionRow(_groupTree.getRowCount() - 1); 
            } catch (SQLException e) {
                new ErrorView(e, "Unable to add new group to the database");
            }
        }
    }

    /**
     * This method may return a null string, this signifies that the user canceled or closed the window
     *
     * @return
     */
    private String promptUserForName() {
        String response = (String) JOptionPane.showInputDialog(this, "Enter unique group name", null);
        while (response != null && _groupNames.contains(response)) {
            response = (String) JOptionPane.showInputDialog(this, "<html>Group name entered already exists,<br>"+
                    "enter unique group name</html>", null);
        }
        return response;
    }

    /**
     * Adds students to the group and updates ui
     * 
     * @param g group to add to
     * @param students Students to add
     */
    private void addStudentsToGroup(DbGroupWrapper g, Iterable<Student> students) {
        for (Student s : students) {
            g.addStudent(s);
        }
        try {
            Allocator.getDatabase().putGroups(ImmutableSet.of(g.getDbGroup()));
            this.updateTreeUI();
        } catch (SQLException e) {
            for (Student s: students) {
                g.removeStudent(s);
            }
            new ErrorView(e, "Unable to add students to group");
        }
    }
    
    /**
     * Removes students from the groups and update the UI.
     * 
     * @param studentsToRemove 
     */
    private void removeStudentsFromGroups(Multimap<DbGroupWrapper, Student> studentsToRemove) {
        Set<DbGroup> groupsToUpdate = new HashSet<DbGroup>();
        for (DbGroupWrapper group : studentsToRemove.keySet()) {
            groupsToUpdate.add(group.getDbGroup());
            for (Student student : studentsToRemove.get(group)) {
                group.removeStudent(student);
            }
        }
        
        try {
            Allocator.getDatabase().putGroups(groupsToUpdate);
            this.updateTreeUI();
        } catch (SQLException e) {
            for (DbGroupWrapper group : studentsToRemove.keySet()) {
                for (Student student : studentsToRemove.get(group)) {
                    group.addStudent(student);
                }
            }
            new ErrorView(e, "Unable to remove student from groups");
        }
    }

    private void changeGroupName(DbGroupWrapper g, String newName) {
        if (!g.getDbGroup().getName().equals(newName)) {
            if (!_groupNames.contains(newName)) {
                String oldName = g.getDbGroup().getName();
                g.getDbGroup().setName(newName);
                try {
                    Allocator.getDatabase().putGroups(ImmutableSet.of(g.getDbGroup()));
                    _groupNames.remove(oldName);
                    _groupNames.add(newName);
                    _model.sortGroups();
                    this.updateTreeUI();
                } catch (SQLException e) {
                    g.getDbGroup().setName(oldName);
                    new ErrorView(e, "Unable to change group name");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a unique group name");
            }
        }
    }

    /**
     * Removes all elements currently selected, if it is group it is removed from the list of groups, if it is a
     * student, it is added to a set of students so that they can be added back to the JList of students
     *
     * @return a set of students that were removed.
     */
    private Set<Student> removeSelectedElements(){
        Set<Student> toReturn = new HashSet<Student>();
        // These will have the same elements, just one the wrapper and one the group
        // This is done because the database methods need the group while the model methods
        // need the wrapper
        Set<DbGroupWrapper> groupWrappersToRemove = new HashSet<DbGroupWrapper>();
        Set<DbGroup> groupsToRemove = new HashSet<DbGroup>();
        Map<Student, DbGroupWrapper> studentGroupToRemove = new HashMap<Student, DbGroupWrapper>();
        Set<DbGroup> groupsToUpdate = new HashSet<DbGroup>();
        //Collect which groups to remove and which students to remove
        if (_groupTree.getSelectionCount() > 0) {
            for (TreePath path : Arrays.asList(_groupTree.getSelectionPaths())) {
                if (path.getLastPathComponent() instanceof DbGroupWrapper){
                    DbGroupWrapper group = (DbGroupWrapper) path.getLastPathComponent();
                    groupWrappersToRemove.add(group);
                    groupsToRemove.add(group.getDbGroup());
                } else if (path.getLastPathComponent() instanceof Student) {
                    DbGroupWrapper group = (DbGroupWrapper) path.getParentPath().getLastPathComponent();
                    Student student = (Student) path.getLastPathComponent();
                    studentGroupToRemove.put(student, group);
                    groupsToUpdate.add(group.getDbGroup());
                    group.removeStudent(student);
                    toReturn.add(student);
                }
            }
            //Do removals
            try{
                if (!groupsToRemove.isEmpty()) {
                    Allocator.getDatabase().removeGroups(groupsToRemove);
                    for (DbGroupWrapper gw : groupWrappersToRemove){
                        toReturn.addAll(_model.getStudentsOfGroup(gw.getDbGroup()));
                        _model.removeGroup(gw);
                    }
                }
                if (!groupsToUpdate.isEmpty()) {
                    Allocator.getDatabase().putGroups(groupsToUpdate);
                }
                _groupTree.clearSelection();
                this.updateTreeUI();
            }
            catch (SQLException ex) {
                //Need to undo visual changes
                Set<Student> studentsToAddBack = studentGroupToRemove.keySet();
                toReturn.removeAll(studentsToAddBack);
                for (Student s : studentsToAddBack){
                    studentGroupToRemove.get(s).addStudent(s);
                }
                new ErrorView(ex, "Unable to remove items");
            }
            _groupTree.clearSelection();
            this.updateTreeUI();
        }

        return toReturn;
    }
     
    private void expandTree() {
        int row = 0;
        while (row < _groupTree.getRowCount()) {
            _groupTree.expandRow(row);
            row++;
        }
    }

    /**
     * Updates the tree ui by making the changes visible
     */
    private void updateTreeUI() {
        _groupTree.updateUI();
    }

    void setStudentList(DnDStudentList studentList) {
        _studentList = studentList;
    }

    Set<Student> getSelectedStudents() {
        return getStudentsFromPaths(Arrays.asList(_groupTree.getSelectionPaths()));
    }

    private Set<Student> getStudentsFromPaths(List<TreePath> paths) {
        Set<Student> studentsToTransfer = new HashSet<Student>();

        for (TreePath path : paths) {
            if (path.getLastPathComponent() instanceof Student) {
                studentsToTransfer.add((Student) path.getLastPathComponent());
            }
        }

        return studentsToTransfer;
    }

    static DataFlavor getGroupDataFlavor() {
        return GROUP_TREE_DATA_FLAVOR;
    }
    
    private class GroupTreeCellRenderer extends DefaultTreeCellRenderer {

        private GroupTreeCellRenderer() {
            this.setLeafIcon(null);
            this.setOpenIcon(IconLoader.loadIcon(IconLoader.IconSize.s16x16, IconLoader.IconImage.SYSTEM_USERS));
            this.setClosedIcon(IconLoader.loadIcon(IconLoader.IconSize.s16x16, IconLoader.IconImage.SYSTEM_USERS));
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            if (value instanceof Student) {
                Student s = (Student) value;
                String text = s.getLogin();
                if (!_enabledStudents.contains(s)) {
                    text = "<html><S>" + text + "</S></html>";
                }
                Component comp = super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);

                //JComponent has a setToolTipText(String) method but Component does not
                if (comp instanceof JComponent) {
                    JComponent c = (JComponent) comp;
                    String tooltip = s.getName();
                    if (!_enabledStudents.contains(s)) {
                        tooltip += " is not enabled";
                    }
                    c.setToolTipText(tooltip);
                }

                return comp;
            }
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }

    /**
     * Wrapper for {@link DbGroup} that is used by {@link DnDGroupTree}
     *
     * @author wyegelwe
     */
    private class DbGroupWrapper {
        private final DbGroup _group;
        private final List<Student> _students;

        DbGroupWrapper(DbGroup group, List<Student> students) {
            _group = group;
            _students = students;
        }

        void removeStudent(Student s) {
            _students.remove(s);
            _group.removeMember(s);
        }

        void addStudent(Student s) {
            _students.add(s);
            Collections.sort(_students);
            _group.addMember(s);
        }

        int getStudentIndex(Student s) {
            return _students.indexOf(s);
        }

        Student get(int i) {
            return _students.get(i);
        }

        int getNumStudents() {
            return _students.size();
        }

        DbGroup getDbGroup() {
            return _group;
        }

        @Override
        public String toString() {
            return _group.getName();
        }
    }

    private class GroupTreeModel implements TreeModel {
        private final List<DbGroupWrapper> _groups;
        private final Object ROOT = "Groups";
        private final Map<Integer, Student> _idToStudent;

        GroupTreeModel() throws ServicesException, SQLException {
            Set<Student> students = Allocator.getDataServices().getStudents();
            Set<DbGroup> groups = Allocator.getDatabase().getGroups(_asgn.getId());

            ImmutableMap.Builder<Integer, Student> builder = ImmutableMap.builder();
            for (Student s : students) {
                builder.put(s.getId(), s);
            }
            _idToStudent = builder.build();

            _groups = new ArrayList<DbGroupWrapper>();
            for (DbGroup group : groups) {
                _groups.add(new DbGroupWrapper(group, this.getStudentsOfGroup(group)));
                _groupNames.add(group.getName());
            }

            this.sortGroups();
        }

        void sortGroups() {
            Collections.sort(_groups, new Comparator<DbGroupWrapper>() {
                @Override
                public int compare(DbGroupWrapper g1, DbGroupWrapper g2) {
                    return g1.getDbGroup().getName().compareToIgnoreCase(g2.getDbGroup().getName());
                }
            });
        }

        List<Student> getStudentsOfGroup(DbGroup g) {
            List<Student> toReturn = new ArrayList<Student>();
            for (Integer sid : g.getMemberIds()) {
                toReturn.add(_idToStudent.get(sid));
            }

            return toReturn;
        }

        @Override
        public Object getRoot() {
            return ROOT;
        }

        @Override
        public Object getChild(Object o, int i) {
            Object child = null;
            if (o == ROOT) {
                child = _groups.get(i);
            } else if (o instanceof DbGroupWrapper) {
                child = ((DbGroupWrapper) o).get(i);
            }
            
            return child;
        }

        @Override
        public int getChildCount(Object o) {
            int count = 0;
            if (o == ROOT) {
                count = _groups.size();
            } else if (o instanceof DbGroupWrapper) {
                count = ((DbGroupWrapper) o).getNumStudents();
            }

            return count;
        }

        @Override
        public boolean isLeaf(Object o) {
            return (o instanceof Student);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            int index = -1;
            if (parent == ROOT) {
                index = _groups.indexOf(child);
            } else if (parent instanceof DbGroupWrapper) {
                index = ((DbGroupWrapper) parent).getStudentIndex((Student) child);
            }

            return index;
        }

        public void addGroup(DbGroup group) {
            _groupNames.add(group.getName());
            _groups.add(new DbGroupWrapper(group, new ArrayList<Student>()));
            this.sortGroups();
        }

        public void removeGroup(DbGroupWrapper group) {
            _groupNames.remove(group.getDbGroup().getName());
            _groups.remove(group);
        }

        @Override
        public void valueForPathChanged(TreePath tp, Object o) { }
        
        @Override
        public void addTreeModelListener(TreeModelListener tl) { }
        
        @Override
        public void removeTreeModelListener(TreeModelListener tl) { }
    }

    /**
     * Currently makes use of the JTrees built in editting function to display a textfield
     */
    private class GroupEditor extends AbstractCellEditor implements TreeCellEditor {
        private final JTextField _textField;
        private DbGroupWrapper _selected;

        public GroupEditor() {
            _textField = new JTextField();
            _textField.setColumns(10);
            _textField.addActionListener(new ActionListener() {
                //This is called when the user presses enter
                @Override
                public void actionPerformed(ActionEvent e) {
                    DnDGroupTree.this.changeGroupName(_selected, _textField.getText());
                }
            });
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                    boolean expanded, boolean leaf, int row) {
            // value is guarenteed to be a DbGroup because isCellEditable is called
            // before this method, and isCellEditable ensures the component is
            _selected = (DbGroupWrapper) value;
            _textField.setText(_selected.toString());

            return _textField;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            boolean editable = super.isCellEditable(event);
            if(event instanceof MouseEvent) {
                editable = editable && (_groupTree.getLastSelectedPathComponent() instanceof DbGroupWrapper);
            }
            
            return editable;
        }

        @Override
        public Object getCellEditorValue() {
            return _textField.getText();
        }
    }

    private class GroupTreeTransferHandler extends TransferHandler {
        private final DataFlavor _studentListDataFlavor;
        private final DataFlavor _groupTreeDataFlavor;
        private final ManageGroupTransferable _transferable;
        private List<TreePath> _selected;

        public GroupTreeTransferHandler() {
            _studentListDataFlavor = DnDStudentList.getStudentDataFlavor();
            _groupTreeDataFlavor = DnDGroupTree.getGroupDataFlavor();
            _selected = new ArrayList<TreePath>();
            _transferable = new ManageGroupTransferable(_groupTreeDataFlavor);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            boolean supportedFlavor = this.hasSupportedFlavor(support.getDataFlavors());
            if (supportedFlavor) {
                // This handler is only applied to a tree, so it is guarenteed that drop location will be a JTree
                // droplocation
                TreePath path = ((JTree.DropLocation) support.getDropLocation()).getPath();
                
                // Determine the group the user dragged on top of. If a user dragged on top of a student, consider the
                // target the group that student belongs to
                DbGroupWrapper group = null;
                if (path.getLastPathComponent() instanceof DbGroupWrapper) {
                    group = (DbGroupWrapper) path.getLastPathComponent();
                } else if (path.getLastPathComponent() instanceof Student) {
                    group = (DbGroupWrapper) path.getParentPath().getLastPathComponent();
                }
                
                Set<DataFlavor> flavors = ImmutableSet.copyOf(support.getDataFlavors());

                //Adds students to the tree, the removal of students is done in the handler's exportDone(...) method
                
                if(flavors.contains(_groupTreeDataFlavor)) {
                    // This check is done for the case when a student is dropped into the group it is already a member
                    // of. In this case we want to remove that student from being selected because it is already in the
                    // group the user wanted it to be in.
                    for (TreePath p : _selected) {
                        if (p.getLastPathComponent() instanceof Student) {
                            // Check if the student to be added is already in the group and if so deselect the student
                            Student s = (Student) p.getLastPathComponent();
                            if (group.getDbGroup().getMemberIds().contains(s.getId())) {
                                _selected.remove(p);
                            }
                        }
                    }
                    DnDGroupTree.this.addStudentsToGroup(group, getStudentsFromPaths(_selected));
                } else if(flavors.contains(_studentListDataFlavor)) {
                    DnDGroupTree.this.addStudentsToGroup(group, _studentList.getSelectedStudents());
                }
            }
            
            return supportedFlavor;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            if (action == MOVE) {
                Multimap<DbGroupWrapper, Student> studentsToRemove = HashMultimap.create();
                for (TreePath path : _selected) {
                    if (path.getLastPathComponent() instanceof Student) {
                        DbGroupWrapper g = (DbGroupWrapper) path.getParentPath().getLastPathComponent();
                        Student s = (Student) path.getLastPathComponent();
                        studentsToRemove.put(g, s);
                    }
                }
                
                if (!studentsToRemove.isEmpty()) {
                    removeStudentsFromGroups(studentsToRemove);
                }
            }
        }

        private boolean hasSupportedFlavor(DataFlavor[] flavors) {
            List<DataFlavor> flavorsList = Arrays.asList(flavors);
            
            return flavorsList.contains(_studentListDataFlavor) || flavorsList.contains(_groupTreeDataFlavor);
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            return hasSupportedFlavor(flavors);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            // Store the selection paths on transfer so that on export can remove the correct students
            _selected = Arrays.asList(_groupTree.getSelectionPaths());
            
            return _transferable;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }
}