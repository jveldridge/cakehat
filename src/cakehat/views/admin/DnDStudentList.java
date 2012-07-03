package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.assignment.Assignment;
import cakehat.services.ServicesException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import support.ui.DescriptionProvider;
import support.ui.FormattedLabel;
import support.ui.GenericJList;

/**
 *
 * @author wyegelwe
 */
class DnDStudentList extends JPanel {
    
    private static final DataFlavor STUDENT_LIST_DATA_FLAVOR = new DataFlavor(DnDStudentList.class, "StudentList");
    private final GenericJList<Student> _studentList;
    private final Set<Student> _enabledStudents;
    private DnDGroupTree _groupTree;

    DnDStudentList(Assignment asgn) throws ServicesException {
        // Generate sorted list of all students not in a group
        List<Student> unassignedStudents = new ArrayList<Student>(Allocator.getDataServices().getStudents());
        unassignedStudents.removeAll(this.getStudentsInGroups(Allocator.getDataServices().getGroups(asgn)));
        Collections.sort(unassignedStudents);
        _studentList = new GenericJList<Student>(unassignedStudents);

        _enabledStudents = Allocator.getDataServices().getEnabledStudents();
        
        this.init();
    }

    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(FormattedLabel.asHeader("Unassigned Students"));
        this.add(Box.createVerticalStrut(5));
        
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _studentList.setDescriptionProvider(new DescriptionProvider<Student>() {
            @Override
            public String getDisplayText(Student student) {
                String displayText;
                if (_enabledStudents.contains(student)) {
                    displayText = student.getLogin();
                } else {
                    displayText = "<html><S>" + student.getLogin() + "</S></html>";
                }
                
                return displayText;
            }

            @Override
            public String getToolTipText(Student student) {
                String tooltip = student.getName();
                if (!_enabledStudents.contains(student)) {
                    tooltip += " is not enabled";
                }
                
                return tooltip;
            }
        });
        _studentList.setDragEnabled(true);
        _studentList.setTransferHandler(new StudentListTransferHandler(this));

        JScrollPane scrollPane = new JScrollPane(_studentList);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        this.add(scrollPane);
    }

    /*
     * Makes a set of all group members from a set of groups. 
     *
     * @param groups
     * @return
     */
    private Set<Student> getStudentsInGroups(Set<Group> groups) {
        Set<Student> toReturn = new HashSet<Student>();
        for (Group g : groups) {
            toReturn.addAll(g.getMembers());
        }

        return toReturn;
    }

    void addStudents(Collection<Student> studentsToAdd) {
        List<Student> listData = _studentList.getListData();
        List<Student> newListData = new ArrayList<Student>(listData);

        newListData.addAll(studentsToAdd);

        Collections.sort(newListData);
        _studentList.setListData(newListData);
    }

    void removeSelectedStudents() {
        List<Student> newListData = new ArrayList<Student>(_studentList.getListData());
        newListData.removeAll(_studentList.getGenericSelectedValues());
        _studentList.setListData(newListData);
    }

    void setGroupTree(DnDGroupTree groupTree) {
        _groupTree = groupTree;
    }

    static DataFlavor getStudentDataFlavor() {
        return STUDENT_LIST_DATA_FLAVOR;
    }

    List<Student> getSelectedStudents(){
        return _studentList.getGenericSelectedValues();
    }

    private class StudentListTransferHandler extends TransferHandler {

        private final DataFlavor _groupTreeDataFlavor;
        private final DnDStudentList _studentList;
        private final ManageGroupTransferable _transferable;

        public StudentListTransferHandler(DnDStudentList studentList) {
            _groupTreeDataFlavor = DnDGroupTree.getGroupDataFlavor();
            _studentList = studentList;
            _transferable = new ManageGroupTransferable(DnDStudentList.getStudentDataFlavor());
        }

        @Override
        public boolean importData(JComponent c, Transferable t) {
            boolean allowImport = Arrays.asList(t.getTransferDataFlavors()).contains(_groupTreeDataFlavor);
            if (allowImport) {
                _studentList.addStudents(_groupTree.getSelectedStudents());
            }

            return allowImport;
        }

        /**
         * This will only be called if the transfer was done to a Group tree, otherwise no transfer would have occurred.
         */
        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            if (action == MOVE) {
                _studentList.removeSelectedStudents();
            }
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            return Arrays.asList(flavors).contains(_groupTreeDataFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return _transferable;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }      
    }
}