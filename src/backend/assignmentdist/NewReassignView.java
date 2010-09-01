package backend.assignmentdist;

import components.GenericJList;
import config.Assignment;
import config.HandinPart;
import config.TA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import utils.Allocator;

/**
 * Provides an interface to allow administrators to manually distribute
 * students to TAs and reassign already distributed students.
 *
 * @author jeldridg
 */
public class NewReassignView extends JFrame {

    private static final int LIST_HEIGHT = 300;
    private static final int LIST_WIDTH = 130;
    private static final int TEXT_HEIGHT = 25;

    private List<TA> _tas;
    private GenericJList<String> _fromUnassigned;
    private GenericJList<TA> _fromTAList;
    private GenericJList<String> _fromStudentList;
    private GenericJList<String> _toUnassigned;
    private GenericJList<TA> _toTAList;
    private GenericJList<String> _toStudentList;

    private JButton _assignButton;
    private JTextField _studentFilterBox;

    private Collection<String> _unassignedStudents;

    private Assignment _asgn;

    public NewReassignView(Assignment asgn) {
        boolean resolved = Allocator.getGradingUtilities().resolveMissingStudents(_asgn);

        if (resolved) {
            this.setVisible(true);
        }
        else {
            this.dispose();
            return;
        }

        _asgn = asgn;
        _tas = new LinkedList<TA>(Allocator.getCourseInfo().getTAs());
        Collections.sort(_tas, new Comparator<TA>(){
            @Override
            public int compare(TA o1, TA o2) {
                return o1.getLogin().compareTo(o2.getLogin());
            }

        });
        _unassignedStudents = new Vector<String>();

        this.setLayout(new BorderLayout());
        this.add(this.getTopPanel(), BorderLayout.NORTH);
        this.add(this.getLeftPanel(), BorderLayout.WEST);
        this.add(this.getCenterPanel(), BorderLayout.CENTER);
        this.add(this.getRightPanel(), BorderLayout.EAST);

        this.updateAssignment();
        
        this.pack();
        this.setVisible(true);

        _studentFilterBox.requestFocus();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
    }

    /**
     * Sets up and returns a JPanel to be shown at the top of the screen.
     * This panel allows the user to select the assignment for which the
     * distribution should be modified.
     * 
     * @return a JPanel to be shown at the top of the screen.
     */
    private JPanel getTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));

        final JComboBox asgnComboBox = new javax.swing.JComboBox();
        for (Assignment s : Allocator.getCourseInfo().getHandinAssignments()) {
            asgnComboBox.insertItemAt(s, asgnComboBox.getItemCount());
        }
        asgnComboBox.setSelectedItem(_asgn);

        asgnComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _asgn = (Assignment) asgnComboBox.getSelectedItem();
                updateAssignment();
            }
        });

        topPanel.add(new JLabel("Modify Distribution for Assignment: "));
        topPanel.add(asgnComboBox);

        return topPanel;
    }

    /**
     * Sets up and returns a JPanel to be shown on the left-hand side of the
     * screen.  This JPanel contains two JLists.  The first is a list of all
     * TAs for the course, including a special "UNASSIGNED" entry.  The second is a
     * list of all of the students who are assigned to the TA selected in the
     * first list.  The student list has a text box above it to filter students
     * by start of login.
     *
     * @return a JPanel to be shown on the left-hand side of the screen.
     */
    private JPanel getLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(5, 5));

        _fromUnassigned = new GenericJList<String>(new String[] {"UNASSIGNED"});
        _fromUnassigned.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _fromUnassigned.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFromList();
            }
        });
        _fromUnassigned.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _fromTAList.clearSelection();
            }
        });

        _fromTAList = new GenericJList<TA>(_tas);
        _fromTAList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _fromTAList.setSelectedIndex(0);
        _fromTAList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFromList();
            }
        });
        _fromTAList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _fromUnassigned.clearSelection();
            }

        });

        JScrollPane fromTASP = new JScrollPane();
        fromTASP.setViewportView(_fromTAList);
        fromTASP.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));

        _fromStudentList = new GenericJList<String>();
        JScrollPane fromStudentSP = new JScrollPane();
        fromStudentSP.setViewportView(_fromStudentList);
        fromStudentSP.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));

        _studentFilterBox = new JTextField();
        _studentFilterBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterStudentLogins(e);
            }
        });
        _studentFilterBox.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));

        JPanel leftPanel_upper = new JPanel();
        leftPanel_upper.setLayout(new BorderLayout(5, 5));
        leftPanel_upper.add(_fromUnassigned, BorderLayout.WEST);
        leftPanel_upper.add(_studentFilterBox, BorderLayout.EAST);

        leftPanel.add(fromTASP, BorderLayout.WEST);
        leftPanel.add(leftPanel_upper, BorderLayout.NORTH);
        leftPanel.add(fromStudentSP, BorderLayout.EAST);

        return leftPanel;
    }
    
    /**
     * Sets up and returns a JPanel to be shown in the center of the screen.
     * This panel contains a JButton used to assign selected students from the
     * left-hand side of the screen to the TA (or UNASSIGNED) selected on the
     * right-hand side of the screen.
     *
     * @return a JPanel to be shown in the center of the screen.
     */
    private JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setPreferredSize(new Dimension(2*LIST_WIDTH, LIST_HEIGHT));
        centerPanel.setLayout(new GridLayout(0, 1));

        JPanel selectedStudentsPanel = new JPanel();
        selectedStudentsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel selectedStudentsLabel = new JLabel("Selected Student(s):");
        selectedStudentsLabel.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        selectedStudentsPanel.add(selectedStudentsLabel);

        _assignButton = new JButton("Assign >>");
        _assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAssignButtonClick();
            }
        });

        _assignButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleAssignButtonClick();
                }
            }
        });

        _assignButton.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        selectedStudentsPanel.add(_assignButton);

        centerPanel.add(selectedStudentsPanel);

        return centerPanel;
    }

    /**
     * Sets up and returns a JPanel to be shown on the right-hand side of the
     * screen.  Like the left panel, this JPanel contains two JLists.  The first
     * is a list of all TAs for the course, including a special "UNASSIGNED" entry.
     * The second is a list of all of the students who are assigned to the TA
     * selected in the first list.  This student list is disabled because selecting
     * items in it would have no effect.
     *
     * @return a JPanel to be shown on the right-hand side of the
     *         screen.
     */
    private JPanel getRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(5, 5));

        _toUnassigned = new GenericJList<String>(new String[] {"UNASSIGNED"});
        _toUnassigned.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _toUnassigned.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateToList();
            }
        });
        _toUnassigned.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _toTAList.clearSelection();
            }
        });

        _toTAList = new GenericJList<TA>(_tas);
        _toTAList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _toTAList.setSelectedIndex(0);
        _toTAList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateToList();
            }
        });
        _toTAList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _toUnassigned.clearSelection();
            }
        });

        JScrollPane toTASP = new JScrollPane();
        toTASP.setViewportView(_toTAList);

        //make toTAPane small enough to fit the toUnassigned JList above it
        //and have the total height be equal to the height of the toStudent pane
        int toTAPaneHeight = LIST_HEIGHT - (TEXT_HEIGHT + 5);
        toTASP.setPreferredSize(new Dimension(LIST_WIDTH, toTAPaneHeight));

        _toStudentList = new GenericJList<String>();
        _toStudentList.setEnabled(false);
        JScrollPane toStudentSP = new JScrollPane();
        toStudentSP.setViewportView(_toStudentList);
        toStudentSP.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));

        JPanel rightPanel_upper = new JPanel();
        rightPanel_upper.setLayout(new BorderLayout(5, 5));
        
        JLabel toTALabel = new JLabel("To TA:");
        toTALabel.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        rightPanel_upper.add(toTALabel, BorderLayout.WEST);
        
        JLabel toStudentsLabel = new JLabel("Students:");
        toStudentsLabel.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        rightPanel_upper.add(toStudentsLabel, BorderLayout.EAST);

        rightPanel.add(rightPanel_upper, BorderLayout.NORTH);

        JPanel toTAPanel = new JPanel();
        toTAPanel.setLayout(new BorderLayout());
        toTAPanel.add(_toUnassigned, BorderLayout.NORTH);
        toTAPanel.add(toTASP, BorderLayout.SOUTH);
        rightPanel.add(toTAPanel, BorderLayout.WEST);
        rightPanel.add(toStudentSP, BorderLayout.EAST);

        return rightPanel;
    }

    private void updateAssignment() {
        this.setTitle(_asgn + " - [" + Allocator.getCourseInfo().getCourse() +"] Assignment Distributor");

        this.updateFromList();
        this.updateToList();
    }

    private void updateFromList() {
        HandinPart handinPart = _asgn.getHandinPart();
        
        if (!_fromUnassigned.isSelectionEmpty()) {
            _unassignedStudents = handinPart.getHandinLogins();
            for (TA ta : _tas) {
                _unassignedStudents.removeAll(Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(), ta.getLogin()));
            }

            _fromStudentList.setListData(_unassignedStudents);
        }
        else {
            String fromTALogin = _fromTAList.getSelectedValue().getLogin();
            _fromStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(handinPart, fromTALogin));
        }
    }

    private void updateToList() {
        HandinPart handinPart = _asgn.getHandinPart();
        if (!_toUnassigned.isSelectionEmpty()) {
            _unassignedStudents = handinPart.getHandinLogins();
            for (TA ta : _tas) {
                _unassignedStudents.removeAll(Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(), ta.getLogin()));
            }
            
            _toStudentList.setListData(_unassignedStudents);
        }
        else {
            String toTALogin = _toTAList.getSelectedValue().getLogin();
            _toStudentList.setListData(Allocator.getDatabaseIO().getStudentsAssigned(handinPart, toTALogin));
        }
    }

    private void handleAssignButtonClick() {
        Collection<String> students = _fromStudentList.getGenericSelectedValues();
        HandinPart handinPart = _asgn.getHandinPart();

        //assigning a student who was previously assigned to UNASSIGNED
        if (!_fromUnassigned.isSelectionEmpty()) {

            //only need to do anything if we're not "reassigning" back to UNASSIGNED
            if (_toUnassigned.isSelectionEmpty()) {
                TA ta = _toTAList.getSelectedValue();

                Iterator<String> iterator = students.iterator();
                while (iterator.hasNext()) {
                    String student = iterator.next();
                    if (!this.isOkToDistribute(student, ta)) {
                        iterator.remove();
                        continue;
                    }

                    //modify the distribution
                    Allocator.getDatabaseIO().assignStudentToGrader(student, handinPart, ta.getLogin());

                    //reassign rubrics for students who already have them
                    if (Allocator.getRubricManager().hasRubric(handinPart, student)) {
                        Allocator.getRubricManager().reassignRubric(handinPart, student, ta.getLogin());
                        iterator.remove();
                    }

                }

                //create and assign rubrics for students who previously did not have them
                Map<String, Collection<String>> distribution = new HashMap<String, Collection<String>>();
                distribution.put(ta.getLogin(), students);
                Allocator.getRubricManager().distributeRubrics(handinPart, distribution,
                                                               Allocator.getCourseInfo().getMinutesOfLeniency());
            }
        }

        //reassigning a student from one TA to another
        else {
            //"reassigning" to UNASSIGNED (i.e., unassigning)
            if (!_toUnassigned.isSelectionEmpty()) {
                TA oldTA = _fromTAList.getSelectedValue();
                
                for (String student : students) {
                    //modify the distribution
                    Allocator.getDatabaseIO().unassignStudentFromGrader(student, handinPart, oldTA.getLogin());

                    //reassign the rubric
                    Allocator.getRubricManager().reassignRubric(handinPart, student, "Nobody");
                }
            }

            //reassigning to another TA
            else {
                TA oldTA = _fromTAList.getSelectedValue();
                TA newTA = _toTAList.getSelectedValue();

                for (String student : students) {
                    if (!this.isOkToDistribute(student, newTA)) {
                        continue;
                    }

                    //modify the distribution
                    Allocator.getDatabaseIO().unassignStudentFromGrader(student, handinPart, oldTA.getLogin());
                    Allocator.getDatabaseIO().assignStudentToGrader(student, handinPart, newTA.getLogin());

                    //reassign the rubric
                    Allocator.getRubricManager().reassignRubric(handinPart, student, newTA.getLogin());
                }
            }
        }

        this.updateFromList();
        this.updateToList();
        _studentFilterBox.requestFocus();
    }

    private boolean isOkToDistribute(String student, TA ta) {
        if (Allocator.getGradingUtilities().groupMemberOnTAsBlacklist(student, _asgn.getHandinPart(), ta)) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + student + "' is on TA "
                                                    + ta.getLogin() + "'s blacklist.  Continue?",
                                                    "Distribute Blacklisted Student?",
                                                    JOptionPane.YES_NO_OPTION);
            return (shouldContinue == JOptionPane.YES_OPTION);
        }
        return true;
    }

    private void filterStudentLogins(KeyEvent evt) {
        //term to filter against
        String filterTerm = _studentFilterBox.getText();

        List<String> matchingLogins = new LinkedList<String>();

        //if "UNASSIGNED" is selected, filter from unassigned students
        if (!_fromUnassigned.isSelectionEmpty()) {
            for (String login : _unassignedStudents) {
                if (login.startsWith(filterTerm)) {
                    matchingLogins.add(login);
                }
            }
        }

        //otherwise, filter from the selected TA's assigned students
        else {
            TA selectedTA = _fromTAList.getSelectedValue();
            for (String login : Allocator.getDatabaseIO().getStudentsAssigned(_asgn.getHandinPart(), selectedTA.getLogin())) {
                if (login.startsWith(filterTerm)) {
                    matchingLogins.add(login);
                }
            }
        }

        _fromStudentList.setListData(matchingLogins);

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (matchingLogins.size() > 0) {
                _studentFilterBox.setText(matchingLogins.get(0));
                _fromStudentList.setSelectedIndex(0);
                _assignButton.requestFocus();
            }
        }
    }

    public static void main(String[] argv) {
        new NewReassignView(Allocator.getCourseInfo().getHandinAssignments().toArray(new Assignment[0])[0]);
    }
}

