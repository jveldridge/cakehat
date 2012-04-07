package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.assignment.Assignment;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
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
class DnDStudentList extends JPanel{
    private final GenericJList<Student> _studentList;
    private static final DataFlavor _studentListDataFlavor = new DataFlavor(DnDStudentList.class, "StudentList");
    private DnDGroupTree _groupTree;
    private Set<Student> _enabledStudents;

    DnDStudentList(Assignment asgn) throws ServicesException {
        //Must get the students that are not already in groups
        Set<Group> groups = new HashSet<Group>();
        groups = Allocator.getDataServices().getGroups(asgn);
        /*Get set of assigned students and the unassigned students is the set difference
         * of the assigned students and the full set of students*/
        Set<Student> assignedStudents = getStudentsInGroups(groups);
        Set<Student> students = new HashSet<Student>();
        Set<Student> unassignedStudents = new HashSet<Student>();
        students = Allocator.getDataServices().getStudents();
        unassignedStudents.addAll(students);
        unassignedStudents.removeAll(assignedStudents);
        List<Student> sortedStudents = new ArrayList<Student>(unassignedStudents);
        Collections.sort(sortedStudents);
        _studentList = new GenericJList<Student>(sortedStudents);

        _enabledStudents = Allocator.getDataServices().getEnabledStudents();

         this.init();
    }

    private void init(){
        //Student list
        this.setLayout(new BorderLayout(0, 0));

        JPanel studentLabelPanel = new JPanel();
        studentLabelPanel.setLayout(new BoxLayout(studentLabelPanel, BoxLayout.Y_AXIS));
        this.add(studentLabelPanel, BorderLayout.NORTH);
        studentLabelPanel.add(FormattedLabel.asHeader("Unassigned Students"));
        studentLabelPanel.add(Box.createVerticalStrut(5));
        this.add(studentLabelPanel, BorderLayout.NORTH);
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _studentList.setDescriptionProvider(new DescriptionProvider<Student>()
        {
            @Override
            public String getDisplayText(Student student)
            {
                if (!_enabledStudents.contains(student)){
                    return "<html><S>" + student.getLogin() + "</S></html>";
                }
                return student.getLogin();
            }

            @Override
            public String getToolTipText(Student student)
            {
                String tooltip = student.getName();
                if (!_enabledStudents.contains(student)){
                        tooltip += " is not enabled";
                }
                return tooltip;
            }
        });
        JScrollPane studentPane = new JScrollPane(_studentList);

        _studentList.setDragEnabled(true);
        _studentList.setTransferHandler(new StudentListTransferHandler(this));

        this.add(studentPane);
    }

    /*
     * Makes a set of all group members from a set of groups
     * This is used to get the set of ungrouped students.
     *
     * Will return an empty set if the input set has no groups, or none of the groups
     * has members.
     *
     * @param groups
     * @return
     */
    private Set<Student> getStudentsInGroups(Set<Group> groups){
        Set<Student> toReturn = new HashSet<Student>();
        for (Group g : groups){
            toReturn.addAll(g.getMembers());
        }

        return toReturn;
    }

    void addStudents(Collection<Student> studentsToAdd){
        List<Student> listData = _studentList.getListData();
        List<Student> newListData = new ArrayList<Student>(listData);

        newListData.addAll(studentsToAdd);

        Collections.sort(newListData);
        _studentList.setListData(newListData);
    }

    void removeSelectedStudents(){
        List<Student> selected = _studentList.getGenericSelectedValues();
        List<Student> listData = _studentList.getListData();
        List<Student> newListData = new ArrayList<Student>(listData);

        newListData.removeAll(selected);
        _studentList.setListData(newListData);
    }

    void setGroupTree(DnDGroupTree groupTree){
        _groupTree = groupTree;

    }

    static DataFlavor getStudentDataFlavor(){
        return _studentListDataFlavor;
    }

    List<Student> getSelectedStudents(){
        return _studentList.getGenericSelectedValues();
    }

    private class StudentListTransferHandler extends TransferHandler {

        private final DataFlavor _studentListDataFlavor;
        private final DataFlavor _groupTreeDataFlavor;
        private final DnDStudentList _studentList;
        private final ManageGroupTransferable _transferable;

        public StudentListTransferHandler(DnDStudentList studentList) {
            _studentListDataFlavor = DnDStudentList.getStudentDataFlavor();
            _groupTreeDataFlavor = DnDGroupTree.getGroupDataFlavor();
            _studentList = studentList;
            _transferable = new ManageGroupTransferable(_studentListDataFlavor);
        }

        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (!this.hasSupportedFlavor(t.getTransferDataFlavors())) {
                return false;
            }
            DataFlavor[] flavors = t.getTransferDataFlavors();
            for (DataFlavor f : flavors){
                if (_groupTreeDataFlavor.equals(f) ){

                    _studentList.addStudents(_groupTree.getSelectedStudents());
                    return true;
                }
            }

            return false;
        }

        /* This will only be called if the transfer was done to a Group tree,
         * otherwise no transfer would have occured
         */
        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            if (action == MOVE) {
                _studentList.removeSelectedStudents();
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
                return _transferable;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
       
    }
}