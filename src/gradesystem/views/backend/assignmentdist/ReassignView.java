package gradesystem.views.backend.assignmentdist;

import gradesystem.components.GenericJList;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.TA;
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
import java.util.ArrayList;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import gradesystem.Allocator;

/**
 * Provides an interface to allow administrators to manually distribute
 * students to TAs and reassign already distributed students.
 *
 * @author jeldridg
 */
public class ReassignView extends JFrame {

    private static final int LIST_HEIGHT = 300;
    private static final int LIST_WIDTH = 130;
    private static final int BUTTON_WIDTH = 160;
    private static final int TEXT_HEIGHT = 25;

    private List<TA> _tas;
    private GenericJList<String> _fromUnassigned;
    private GenericJList<TA> _fromTAList;
    private GenericJList<String> _fromRandom;
    private GenericJList<String> _fromStudentList;
    private GenericJList<String> _toUnassigned;
    private GenericJList<TA> _toTAList;
    private GenericJList<String> _toStudentList;

    private JButton _assignButton;
    private JTextField _studentFilterBox;
    private JSpinner _numStudentsSpinner;
    private JLabel _numUnassignedLabel;
    private JLabel _assignTypeLabel;

    private Collection<String> _unassignedStudents;

    private Collection<String> _unresolvedStudents;

    private Assignment _asgn;

    public ReassignView(Assignment asgn) {
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

        //initialize starting selection state
        _fromUnassigned.setSelectedIndex(0);
        _toTAList.setSelectedIndex(0);

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
        _fromUnassigned.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _fromTAList.clearSelection();
            }
        });

        _fromTAList = new GenericJList<TA>(_tas);
        _fromTAList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        _studentFilterBox = new JTextField();
        _studentFilterBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterStudentLogins(e);
            }
        });
        _studentFilterBox.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));

        _fromRandom = new GenericJList<String>(new String[] {"RANDOM"});
        _fromRandom.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _fromRandom.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableUseRandomAssignment();
            }
        });

        _fromStudentList = new GenericJList<String>();
        _fromStudentList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableUseSelectedAssignment();
            }
        });

        JScrollPane fromStudentSP = new JScrollPane();
        fromStudentSP.setViewportView(_fromStudentList);

        //height of fromStudentList needs to be shrunk by the height of the RANDOM list
        //plus the buffer space to align with the bottoms of the fromTAList and the screen
        int fromStudentListHeight = LIST_HEIGHT - (TEXT_HEIGHT + 10);
        fromStudentSP.setPreferredSize(new Dimension(LIST_WIDTH, fromStudentListHeight));

        JPanel leftPanel_upper = new JPanel();
        leftPanel_upper.setLayout(new BorderLayout(5, 5));
        leftPanel_upper.add(_fromUnassigned, BorderLayout.WEST);
        leftPanel_upper.add(_studentFilterBox, BorderLayout.EAST);

        JPanel studentPanel = new JPanel();
        studentPanel.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        studentPanel.add(_fromRandom);
        studentPanel.add(fromStudentSP);

        leftPanel.add(fromTASP, BorderLayout.WEST);
        leftPanel.add(leftPanel_upper, BorderLayout.NORTH);
        leftPanel.add(studentPanel, BorderLayout.EAST);

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

        JPanel assignControlPanel = new JPanel();
        assignControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        _assignTypeLabel = new JLabel();
        _assignTypeLabel.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        assignControlPanel.add(_assignTypeLabel);

        _numStudentsSpinner = new JSpinner(new SpinnerNumberModel());
        _numStudentsSpinner.setPreferredSize(new Dimension(LIST_WIDTH / 2, TEXT_HEIGHT));
        assignControlPanel.add(_numStudentsSpinner);
        _numStudentsSpinner.setVisible(false);

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
                    _studentFilterBox.setText(null);
                    _studentFilterBox.requestFocus();
                }
            }
        });

        _assignButton.setPreferredSize(new Dimension(BUTTON_WIDTH, TEXT_HEIGHT));
        assignControlPanel.add(_assignButton);

        _numUnassignedLabel = new JLabel();
        assignControlPanel.add(_numUnassignedLabel);

        //using JPanel buffers to center asgnControlPanel
        centerPanel.add(new JPanel());
        centerPanel.add(assignControlPanel);
        centerPanel.add(new JPanel());

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

    private void enableUseRandomAssignment() {
        _fromStudentList.clearSelection();

        _numStudentsSpinner.setVisible(true);
        _assignTypeLabel.setText("Random Student(s):");

        _assignButton.setEnabled(_fromStudentList.getModel().getSize() > 0);
    }

    private void enableUseSelectedAssignment() {
        _fromRandom.clearSelection();

        _numStudentsSpinner.setVisible(false);
        _assignTypeLabel.setText("Selected Student(s):");

        _assignButton.setEnabled(_fromStudentList.getSelectedValue() != null);
    }

    private void updateAssignment() {
        this.setTitle(_asgn + " - [" + Allocator.getCourseInfo().getCourse() +"] Assignment Distributor");

        _unresolvedStudents = Allocator.getGradingServices().resolveMissingStudents(_asgn);

        if (_unresolvedStudents == null) {
            this.dispose();
            return;
        }

        this.updateFromList();
        this.updateToList();
    }

    private void updateFromList() {
        HandinPart handinPart = _asgn.getHandinPart();

        List<String> loginsToDisplay;

        //if UNASSIGNED is selected
        if (!_fromUnassigned.isSelectionEmpty()) {
            _unassignedStudents = handinPart.getHandinLogins();
            _unassignedStudents.removeAll(Allocator.getDatabaseIO().getAllAssignedStudents(handinPart));
            _unassignedStudents.removeAll(_unresolvedStudents);
            loginsToDisplay = new LinkedList<String>(_unassignedStudents);

            _assignButton.setEnabled(_unassignedStudents.size() > 0);
            _numUnassignedLabel.setText(String.format("%d unassigned students to choose from", _unassignedStudents.size()));
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMaximum(_unassignedStudents.size());
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setValue(_unassignedStudents.size() == 0 ? 0 : 1);
        }
        else {
            String fromTALogin = _fromTAList.getSelectedValue().getLogin();
            Collection<String> studentsAssigned = Allocator.getDatabaseIO().getStudentsAssigned(handinPart, fromTALogin);
            loginsToDisplay = new LinkedList<String>(studentsAssigned);

            _assignButton.setEnabled(studentsAssigned.size() > 0);
            _numUnassignedLabel.setText(String.format("%d students to chose from TA %s",
                                                      studentsAssigned.size(), fromTALogin));
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMaximum(studentsAssigned.size());
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setValue(studentsAssigned.size() == 0 ? 0 : 1);
        }

        Collections.sort(loginsToDisplay);
        _fromStudentList.setListData(loginsToDisplay);

        if (_fromRandom.isSelectionEmpty()) {
            _fromStudentList.setSelectedIndex(0);
        }
    }

    private void updateToList() {
        HandinPart handinPart = _asgn.getHandinPart();
        List<String> loginsToDisplay;

        if (!_toUnassigned.isSelectionEmpty()) {
            _unassignedStudents = handinPart.getHandinLogins();
            _unassignedStudents.removeAll(Allocator.getDatabaseIO().getAllAssignedStudents(handinPart));
            loginsToDisplay = new LinkedList<String>(_unassignedStudents);
        }
        else {
            String toTALogin = _toTAList.getSelectedValue().getLogin();
            loginsToDisplay = new LinkedList<String>(Allocator.getDatabaseIO().getStudentsAssigned(handinPart, toTALogin));
        }

        Collections.sort(loginsToDisplay);
        _toStudentList.setListData(loginsToDisplay);
    }

    private void handleAssignButtonClick() {
        if (!_fromRandom.isSelectionEmpty()) {
            this.handleRandomAssignButtonClick();
        }
        else {
            this.handleSelectedAssignButtonClick();
        }
    }

    private void handleSelectedAssignButtonClick() {
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

                    //don't need to make rubrics for students who already have them
                    if (Allocator.getRubricManager().hasRubric(handinPart, student)) {
                        iterator.remove();
                    }

                }

                //create and assign rubrics for students who previously did not have them
                Map<String, Collection<String>> distribution = new HashMap<String, Collection<String>>();
                distribution.put(ta.getLogin(), students);
                Allocator.getRubricManager().distributeRubrics(handinPart, distribution,
                                                               Allocator.getCourseInfo().getMinutesOfLeniency(),
                                                               DistributionRequester.DO_NOTHING_REQUESTER);
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
                }
            }
        }

        this.updateFromList();
        this.updateToList();
        _studentFilterBox.requestFocus();
    }

    private void handleRandomAssignButtonClick() {
        HandinPart handinPart = _asgn.getHandinPart();
        TA toTA = _toTAList.getSelectedValue();
        TA fromTA = _fromTAList.getSelectedValue();

        List<String> studentsToChoseFrom;

        //assigning students who were previously assigned to UNASSIGNED
        if (!_fromUnassigned.isSelectionEmpty()) {
            studentsToChoseFrom = new ArrayList<String>(_unassignedStudents);
        }
        else {
            studentsToChoseFrom = new ArrayList<String>(Allocator.getDatabaseIO().getStudentsAssigned(handinPart, fromTA.getLogin()));
        }
        Collections.shuffle(studentsToChoseFrom);

        Collection<String> studentsToAssign = new LinkedList<String>();

        int numStudentsToAssign = (Integer) _numStudentsSpinner.getValue();
        int numStudsAssignedSoFar = 0;

        //assigning to UNASSIGNED; no need to check blacklist
        if (toTA == null) {
            for (String student : studentsToChoseFrom) {
                if (numStudsAssignedSoFar == numStudentsToAssign) {
                    break;
                }

                studentsToAssign.add(student);
                numStudsAssignedSoFar++;
            }
        }

        //attempting to assing to a new TA; need to check blacklist
        else {
            for (String student : studentsToChoseFrom) {
                if (numStudsAssignedSoFar == numStudentsToAssign) {
                    break;
                }

                if (toTA != null && !Allocator.getGradingServices().groupMemberOnTAsBlacklist(student, handinPart, toTA.getLogin())) {
                    studentsToAssign.add(student);
                    numStudsAssignedSoFar++;
                }
            }
        }

        //we weren't able to assign as many students as requested; show error and return
        if (numStudsAssignedSoFar < numStudentsToAssign) {
            String errMsg = "Cannot assign this many students " +
                                  "without violating the blacklist.\nIf you would like to " +
                                  "override the blacklist, please manually select students " +
                                  "to be distributed.\n";
            JOptionPane.showMessageDialog(ReassignView.this, errMsg, "Distribution Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //if we haven't yet encountered a problem, modify the distribution and reassign rubrics

        //assigning to a new TA
        if (toTA != null) {
            Iterator<String> iterator = studentsToAssign.iterator();
            while (iterator.hasNext()) {
                String student = iterator.next();

                //update distribution
                Allocator.getDatabaseIO().assignStudentToGrader(student, handinPart, toTA.getLogin());
                if (fromTA != null) {
                    Allocator.getDatabaseIO().unassignStudentFromGrader(student, handinPart, fromTA.getLogin());
                }
            }

            Map<String, Collection<String>> distribution = new HashMap<String, Collection<String>>();
            distribution.put(toTA.getLogin(), studentsToAssign);
            Allocator.getRubricManager().distributeRubrics(handinPart, distribution,
                                                           Allocator.getCourseInfo().getMinutesOfLeniency(),
                                                           DistributionRequester.DO_NOTHING_REQUESTER);
        }

        //assigning to UNASSIGNED from a TA
        else if (fromTA != null) {
            for (String student : studentsToAssign) {
                Allocator.getDatabaseIO().unassignStudentFromGrader(student, handinPart, fromTA.getLogin());
            }
        }

        this.updateFromList();
        this.updateToList();
    }

    private boolean isOkToDistribute(String student, TA ta) {
        if (Allocator.getGradingServices().groupMemberOnTAsBlacklist(student, _asgn.getHandinPart(), ta.getLogin())) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + student + " is on TA "
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
        new ReassignView(Allocator.getCourseInfo().getHandinAssignments().toArray(new Assignment[0])[0]);
    }
}