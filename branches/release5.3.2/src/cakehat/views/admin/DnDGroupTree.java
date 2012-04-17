package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.database.DbGroup;
import cakehat.database.Student;
import cakehat.database.assignment.Assignment;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Component;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import support.ui.FormattedLabel;

/**
 *
 * @author wyegelwe
 */
class DnDGroupTree extends JPanel {
    private static final DataFlavor _groupTreeDataFlavor = new DataFlavor(DnDGroupTree.class, "GroupTree");
    private final  JTree _groupTree;
    private final GroupTreeModel _model;
    private final Assignment _asgn;
    private DnDStudentList _studentList;
    private Set<Student> _enabledStudents;
    private Set<String> _groupNames;

    DnDGroupTree(Assignment asgn) throws ServicesException, SQLException {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        _asgn = asgn;
        try {
            _enabledStudents = Allocator.getDataServices().getEnabledStudents();
        } catch (ServicesException e) {
            new ErrorView(e, "Could not get enabled students from database");
        }
        _groupNames = new HashSet<String>();
        _model = new GroupTreeModel();
        _groupTree = new JTree(_model);
        initUI();
    }

    private void initUI(){
        this.add(FormattedLabel.asHeader("Groups"));
        this.add(Box.createVerticalStrut(5));

        JScrollPane groupPane = new JScrollPane(_groupTree);
        _groupTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        _groupTree.setRootVisible(false);
        _groupTree.setShowsRootHandles(true);

        _groupTree.setAlignmentX(LEFT_ALIGNMENT);

        _groupTree.setDragEnabled(true);
        _groupTree.setTransferHandler(new GroupTreeTransferHandler());

        // Makes use of the JTrees built in node editting functions
        _groupTree.setEditable(true);
        _groupTree.setCellEditor(new GroupEditor());
        _groupTree.setCellRenderer(new GroupTreeCellRenderer());

        this.expandTree();
        this.add(groupPane);
        this.add(Box.createVerticalStrut(5));

        JPanel groupButtonPanel = new JPanel();
        JPanel newButtonButtonPanel = new JPanel();
        JPanel removeButtonButtonPanel = new JPanel();

        groupButtonPanel.setLayout(new GridLayout(1,2));
        groupButtonPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewGroup();
            }
        });
        newButtonButtonPanel.add(newButton);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                Set<Student> removedStudents = removeSelectedElements();
                _studentList.addStudents(removedStudents);
            }
        });
        removeButtonButtonPanel.add(removeButton);

        groupButtonPanel.add(newButtonButtonPanel);
        groupButtonPanel.add(removeButtonButtonPanel);

        this.add(groupButtonPanel);
    }

    /**
     * Creates a new group and adds it to the tree. The model creates a new DbGroup
     * and with the provided name. This method is only called from the add button
     *
     * @param name new group will have
     */
    void createNewGroup(){
        String groupName = promptUserForName();
        if (groupName != null && !groupName.isEmpty()){
            DbGroup group = new DbGroup(_asgn, groupName, new HashSet<Student>());
            try {
                Allocator.getDatabase().putGroups(ImmutableSet.of(group));
                _model.addGroup(group);
                this.updateTreeUI();
                _groupTree.setSelectionRow(_groupTree.getRowCount() - 1); //newly added group will be the last item in the tree
            } catch (SQLException e) {
                new ErrorView(e, "Unable to add new group to the database");
            }
        }
    }

    /**
     * This method may return a null string, this signifies that the user cancelled
     * or x-d out
     *
     * @return
     */
    String promptUserForName(){
        String response = (String) JOptionPane.showInputDialog(this, "Enter unique group name", null);
        while (response != null && _groupNames.contains(response)){
            response = (String) JOptionPane.showInputDialog(this, "<html>Group name entered already exists, <br> enter unique group name<html>", null);
        }
        return response;
    }

    /**
     * Adds students to the group and updates ui
     * @param g group to add to
     * @param students Students to add
     */
    void addStudentsToGroup(DbGroupWrapper g, Iterable<Student> students){
        for (Student s : students){
            g.addStudent(s);
        }
        try {
            Allocator.getDatabase().putGroups(ImmutableSet.of(g.getDbGroup()));
            this.updateTreeUI();
        } catch (SQLException e) {
            for (Student s: students){
                g.removeStudent(s);
            }
            new ErrorView(e, "Unable to add students to group");
        }
    }

     /**
      * Removes students from the group and updates ui
      *
      * @param group to removes students from
      * @param iterable of students to remove from group
      */
    private void removeStudentsFromGroup(DbGroupWrapper g, Iterable<Student> studentsToRemove){
        //Note this will only remove the student if they are present in the group
        for (Student s: studentsToRemove){
            g.removeStudent(s);
        }
        try {
            Allocator.getDatabase().putGroups(ImmutableSet.of(g.getDbGroup()));
            this.updateTreeUI();
        } catch (SQLException e) {
            for (Student s: studentsToRemove){
                g.addStudent(s);
            }
            new ErrorView(e, "Unable to remove students from group");
        }
    }

    private void changeGroupName(DbGroupWrapper g, String newName) {
        if (!_groupNames.contains(newName)){
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

    /**
     * Returns an immutable copy of the groups
     */
    List<DbGroup> getGroups(){
        return ImmutableList.copyOf(_model.getGroups());
    }

    /* *
     * Removes all elements currently selected, if it is group it is removed
     * from the list of groups, if it is a student, it is added to a set of
     * students so that they can be added back to the JList of students
     *
     * @return a set of students that were removed.
     */
    Set<Student> removeSelectedElements(){
        Set<Student> toReturn = new HashSet<Student>();
        /*These will have the same elements, just one the wrapper and one the group
         * This is done because the database methods need the group while the model methods
         * need the wrapper. One could be built from the other, but speed seemed more
         * imporant than memory */
        Set<DbGroupWrapper> groupWrappersToRemove = new HashSet<DbGroupWrapper>();
        Set<DbGroup> groupsToRemove = new HashSet<DbGroup>();
        Map<Student, DbGroupWrapper> studentGroupToRemove = new HashMap<Student, DbGroupWrapper>();
        Set<DbGroup> groupsToUpdate = new HashSet<DbGroup>();
        //Collect which groups to remove and which students to remove
        if (_groupTree.getSelectionCount() > 0){
            List<TreePath> paths = Arrays.asList(_groupTree.getSelectionPaths());
            for (TreePath path : paths){
                if (path.getLastPathComponent() instanceof DbGroupWrapper){
                    DbGroupWrapper group = (DbGroupWrapper) path.getLastPathComponent();
                    groupWrappersToRemove.add(group);
                    groupsToRemove.add(group.getDbGroup());
                }
                else if (path.getLastPathComponent() instanceof Student){
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
                if (groupsToRemove.size() > 0){
                    Allocator.getDatabase().removeGroups(groupsToRemove);
                    for (DbGroupWrapper gw : groupWrappersToRemove){
                        toReturn.addAll(_model.getStudentsOfGroup(gw.getDbGroup()));
                        _model.removeGroup(gw);
                    }
                }
                if (groupsToUpdate.size() > 0){
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
     
    private void expandTree(){
        int row = 0;
        while(row < _groupTree.getRowCount()){
            _groupTree.expandRow(row);
            row++;
        }
    }

    /**
     * Updates the tree ui by making the changes visible and expanding the tree
     */
    private void updateTreeUI(){
        _groupTree.updateUI();
        this.expandTree();
    }

    void setStudentList(DnDStudentList studentList){
        _studentList = studentList;
    }

    Set<Student> getSelectedStudents(){
        List<TreePath> selected = Arrays.asList(_groupTree.getSelectionPaths());
        return getStudentsFromPaths(selected);
    }

    private Set<Student> getStudentsFromPaths(List<TreePath> paths){
        Set<Student> studentsToTransfer = new HashSet<Student>();

        for (TreePath path : paths){
            if (path.getLastPathComponent() instanceof Student){
                studentsToTransfer.add((Student) path.getLastPathComponent());
            }
        }

        return studentsToTransfer;
    }

    static DataFlavor getGroupDataFlavor(){
        return _groupTreeDataFlavor;
    }

     private class GroupTreeCellRenderer extends DefaultTreeCellRenderer{

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                             boolean leaf, int row, boolean hasFocus)  {
            if (value instanceof Student){
                Student s = (Student) value;
                String text = s.getLogin();
                if (!_enabledStudents.contains(s)){
                    text = "<html><S>" + text + "</S></html>";
                }
                Component comp = super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);

                //JComponent has a setToolTipText(String) method but Component does not
                if(comp instanceof JComponent){
                    JComponent c = (JComponent) comp;
                    String tooltip = s.getName();
                    if (!_enabledStudents.contains(s)){
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

        DbGroupWrapper(DbGroup group, List<Student> students){
            _group = group;
            _students = students;
        }

        void removeStudent(Student s){
            _students.remove(s);
            _group.removeMember(s);
        }

        void addStudent(Student s){
            _students.add(s);
            Collections.sort(_students);
            _group.addMember(s);
        }

        int getStudentIndex(Student s){
            return _students.indexOf(s);
        }

        Student get(int i){
            return _students.get(i);
        }

        int getNumStudents(){
            return _students.size();
        }

        DbGroup getDbGroup(){
            return _group;
        }

        @Override
        public String toString(){
            return _group.getName();
        }
    }

    private class GroupTreeModel implements TreeModel{
        private final List<DbGroupWrapper> _groups;
        private final Object ROOT = "Groups";
        private final Map<Integer, Student> _idToStudent; //expects the full set of students

        public GroupTreeModel() throws ServicesException, SQLException{
            Set<Student> students = new HashSet<Student>();
            Set<DbGroup> groups = new HashSet<DbGroup>();

            students = Allocator.getDataServices().getStudents();
            groups = Allocator.getDatabase().getGroups(_asgn.getId());
            ImmutableMap.Builder<Integer, Student> builder =  new ImmutableMap.Builder<Integer, Student>();
            for (Student s : students){
                builder.put(s.getId(), s);
            }
            _idToStudent = builder.build();

            _groups = new ArrayList<DbGroupWrapper>();
            for (DbGroup group : groups){
                _groups.add(new DbGroupWrapper(group, this.getStudentsOfGroup(group)));
                _groupNames.add(group.getName());
            }

            this.sortGroups();
        }

        public void sortGroups(){
            Collections.sort(_groups, new Comparator<DbGroupWrapper>(){
                @Override
                public int compare(DbGroupWrapper g1, DbGroupWrapper g2) {
                    return g1.getDbGroup().getName().compareToIgnoreCase(g2.getDbGroup().getName());
                }
            });
        }

        public List<DbGroup> getGroups(){
            Builder<DbGroup> builder = ImmutableList.builder();
            for (DbGroupWrapper wrapper : _groups){
                builder.add(wrapper.getDbGroup());
            }
            return builder.build();
        }

        public List<Student> getStudentsOfGroup(DbGroup g){
            List<Student> toReturn = new ArrayList<Student>();
            for (Integer sid : g.getMemberIds()){
                toReturn.add(_idToStudent.get(sid));
            }

            return toReturn;
        }

        @Override
        public Object getRoot(){
            return ROOT;
        }

        @Override
        public Object getChild(Object o, int i){
            Object child = null;
            if(o == ROOT) {
                child = _groups.get(i);
            }
            else if(o instanceof DbGroupWrapper){
                child = ((DbGroupWrapper) o).get(i);
            }
            
            return child;
        }

        @Override
        public int getChildCount(Object o){
            int count = 0;
            if(o == ROOT){
                count = _groups.size();
            }
            else if(o instanceof DbGroupWrapper){
                count = ((DbGroupWrapper) o).getNumStudents();
            }

            return count;
        }

        @Override
        public boolean isLeaf(Object o) {
            return (o instanceof Student);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child){
            int index = -1;
            if(parent == ROOT){
                index = _groups.indexOf(child);
            }
            else if(parent instanceof DbGroupWrapper) {
                index = ((DbGroupWrapper) parent).getStudentIndex((Student) child);
            }

            return index;
        }

        public void addGroup(DbGroup group){
            _groupNames.add(group.getName());
            _groups.add(new DbGroupWrapper(group, new ArrayList<Student>()));
            this.sortGroups();
        }

        public void removeGroup(DbGroupWrapper group){
            _groupNames.remove(group.getDbGroup().getName());
            _groups.remove(group);
        }

        public void valueForPathChanged(TreePath tp, Object o) { }
        public void addTreeModelListener(TreeModelListener tl) { }
        public void removeTreeModelListener(TreeModelListener tl) { }
    }

    /**
     * Currently makes use of the JTrees built in editting function to display
     * a textfield
     */
    private class GroupEditor extends AbstractCellEditor implements TreeCellEditor{
        private JTextField _textField;
        private DbGroupWrapper _selected;

        public GroupEditor() {
            _textField = new JTextField();
            _textField.setColumns(10);
            _textField.addActionListener( new ActionListener(){
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
            /* value is guarenteed to be a DbGroup because isCellEditable is called
             * before this method, and isCellEditable ensures the component is
             */
            _selected = (DbGroupWrapper) value;
            _textField.setText(_selected.toString());

            return _textField;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            // Get initial setting
            boolean editable = super.isCellEditable(event);
            if(event instanceof MouseEvent) {
                // If still possible, check if current tree node is a group
                if (editable) {
                    Object node = _groupTree.getLastSelectedPathComponent();
                    //only want to edit if the object selected is a group
                    editable =  ((node != null) && (node instanceof DbGroupWrapper));
                }
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
            if (!this.hasSupportedFlavor(support.getDataFlavors())) {
                return false;
            }
            /* This handler is only applied to a tree, so it is guarenteed that
             * drop location will be a JTree droplocation
             */
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath path = dropLocation.getPath();

            /*This ensures that the path points to a group and allows a user to
             * to drop onto a student in a group and have the new students added
             * to that group. Path will not be null because the drop location is returning
             * a path that had returned true for accepting transfer. Get path only return null if:
             *
             * The tree has no model, it has no root, the root is collapsed, root is a leaf node.
             * None of these can happen
             */
            if ((path.getLastPathComponent() instanceof Student)){
                path = path.getParentPath();
            }
            //the above check ensures that the lastPathComponent is a DbGroup
            DbGroupWrapper group = (DbGroupWrapper) path.getLastPathComponent();

            DataFlavor[] flavors = support.getDataFlavors();

            //adds students to the grouptree -- the removal of students is done in the handlers export done method
            for (DataFlavor f : flavors){
                if (_groupTreeDataFlavor.equals(f)){
                    Set<Integer> ids = group.getDbGroup().getMemberIds();
                    /* This check is done for the case when a student is dropped into the
                     * group it is already a member of. In this case we want to remove
                     * that student from being selected because it is already in the
                     * group the user wanted it to be in */
                    for (TreePath p : _selected){
                        if (p.getLastPathComponent() instanceof Student){
                            /*checks if the student to be added is already in the group
                             * and if so deselectes the student */
                            Student s = (Student) p.getLastPathComponent();
                            if (ids.contains(s.getId())){
                                _selected.remove(p);
                            }
                        }
                    }
                    DnDGroupTree.this.addStudentsToGroup(group, getStudentsFromPaths(_selected));
                    return true;
                }
                else if (_studentListDataFlavor.equals(f)){
                    DnDGroupTree.this.addStudentsToGroup(group, _studentList.getSelectedStudents());
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            if (action == MOVE) {
                for (TreePath path : _selected){
                    if (path.getLastPathComponent() instanceof Student){
                        DbGroupWrapper g =  (DbGroupWrapper) path.getParentPath().getLastPathComponent();
                        Student s =  (Student) path.getLastPathComponent();
                        DnDGroupTree.this.removeStudentsFromGroup(g, Arrays.asList(s));
                    }
                }
            }
        }

        private boolean hasSupportedFlavor(DataFlavor[] flavors) {
            for (DataFlavor f : flavors){
                if (_studentListDataFlavor.equals(f) || _groupTreeDataFlavor.equals(f)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            return (hasSupportedFlavor(flavors));
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
                /*Store the selection paths on transfer so that on export can remove
                 * the correct students
                 */
                _selected = Arrays.asList(_groupTree.getSelectionPaths());
                return _transferable;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }
}
