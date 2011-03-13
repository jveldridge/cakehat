package gradesystem.views.backend.assignmentdist;

import gradesystem.components.GenericJList;
import gradesystem.config.Assignment;
import gradesystem.config.TA;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.rubric.RubricException;
import gradesystem.services.ServicesException;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
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
import gradesystem.components.GenericJComboBox;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.views.shared.ErrorView;
import utils.FileSystemUtilities.OverwriteMode;

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
    private GenericJList<Group> _fromGroupList;
    private GenericJList<String> _toUnassigned;
    private GenericJList<TA> _toTAList;
    private GenericJList<Group> _toGroupList;

    private JButton _assignButton;
    private JTextField _studentFilterBox;
    private JSpinner _numStudentsSpinner;
    private JLabel _numUnassignedLabel;
    private JLabel _assignTypeLabel;

    private Collection<Group> _unassignedGroups;
    private Collection<String> _unresolvedHandins;

    private Assignment _asgn;
    private DistributablePart _dp;

    public ReassignView(Assignment asgn, DistributablePart dp) {
        _asgn = asgn;
        _dp = dp;

        _tas = new LinkedList<TA>(Allocator.getConfigurationInfo().getTAs());
        Collections.sort(_tas);

        _unassignedGroups = new ArrayList<Group>();

        this.setLayout(new BorderLayout());
        this.add(this.getTopPanel(), BorderLayout.NORTH);
        this.add(this.getLeftPanel(), BorderLayout.WEST);
        this.add(this.getCenterPanel(), BorderLayout.CENTER);
        this.add(this.getRightPanel(), BorderLayout.EAST);

        if (_dp == null) {
            this.disableAll();
        }
        else {
            //initialize starting selection state
            _fromUnassigned.setSelectedIndex(0);
            _toTAList.setSelectedIndex(0);
            try {
                this.updateAssignmentAndPart();
            } catch (ServicesException ex) {
                new ErrorView(ex, "An error occurred while initializing the interface. " +
                                  "The ReassignView will now close.  If this problem " +
                                  "persists, please send an error report.");
                ReassignView.this.dispose();
            } catch (SQLException ex) {
                new ErrorView(ex, "An error occurred while initializing the interface. " +
                                  "The ReassignView will now close.  If this problem " +
                                  "persists, please send an error report.");
                ReassignView.this.dispose();
            }
        }

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

        final GenericJComboBox<DistributablePart> dpComboBox = new GenericJComboBox<DistributablePart>(_asgn.getDistributableParts());
        dpComboBox.setSelectedItem(_dp);

        dpComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _dp = dpComboBox.getSelectedItem();
                try {
                    updateAssignmentAndPart();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (SQLException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                }
            }
        });

        final GenericJComboBox<Assignment> asgnComboBox =
                new GenericJComboBox<Assignment>(Allocator.getConfigurationInfo().getHandinAssignments());
        asgnComboBox.setSelectedItem(_asgn);

        asgnComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _asgn = asgnComboBox.getSelectedItem();
                dpComboBox.removeAllItems();
                for (DistributablePart dp : _asgn.getDistributableParts()) {
                    dpComboBox.addItem(dp);
                }
                _dp = null;
                dpComboBox.setSelectedItem(null);
                
                try {
                    updateAssignmentAndPart();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (SQLException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                }
            }
        });

        topPanel.add(new JLabel("Modify Distribution for Assignment: "));
        topPanel.add(asgnComboBox);
        topPanel.add(dpComboBox);

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

        _fromUnassigned = new GenericJList<String>("UNASSIGNED");
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
                try {
                    updateFromList();
                } catch (SQLException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. " +
                                      "The ReassignView will now close.  If this problem " +
                                      "persists, please send an error report.");
                    ReassignView.this.dispose();
                }
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
                try {
                    filterStudentLogins(e);
                } catch (SQLException ex) {
                    new ErrorView(ex, "There was an error filtering student logins.");
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "There was an error filtering student logins.");
                }
            }
        });
        _studentFilterBox.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));

        _fromRandom = new GenericJList<String>("RANDOM");
        _fromRandom.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _fromRandom.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableUseRandomAssignment();
            }
        });

        _fromGroupList = new GenericJList<Group>();
        _fromGroupList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableUseSelectedAssignment();
            }
        });

        JScrollPane fromStudentSP = new JScrollPane();
        fromStudentSP.setViewportView(_fromGroupList);

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
                try {
                    handleAssignButtonClick();
                } catch (SQLException ex) {
                    //TODO ensure that all changes are rolled back properly!
                    new ErrorView(ex, "An error occurred during assignment. " +
                                      "No changes have been made.");
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred during assignment. " +
                                      "No changes have been made.");
                } catch (RubricException ex) {
                    new ErrorView(ex, "An error occurred during assignment. " +
                                      "No changes have been made.");
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "An error occurred during assignment. " +
                                      "No changes have been made.");
                }
            }
        });

        _assignButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        handleAssignButtonClick();
                    } catch (SQLException ex) {
                        //TODO ensure that all changes are rolled back properly!
                        new ErrorView(ex, "An error occurred during assignment. " +
                                          "No changes have been made.");
                    } catch (ServicesException ex) {
                        new ErrorView(ex, "An error occurred during assignment. " +
                                          "No changes have been made.");
                    } catch (RubricException ex) {
                        new ErrorView(ex, "An error occurred during assignment. " +
                                          "No changes have been made.");
                    } catch (CakeHatDBIOException ex) {
                        new ErrorView(ex, "An error occurred during assignment. " +
                                          "No changes have been made.");
                    }

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

        _toUnassigned = new GenericJList<String>("UNASSIGNED");
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
                try {
                    updateToList();
                } catch (SQLException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. "
                            + "The ReassignView will now close.  If this problem "
                            + "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. "
                            + "The ReassignView will now close.  If this problem "
                            + "persists, please send an error report.");
                    ReassignView.this.dispose();
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. "
                            + "The ReassignView will now close.  If this problem "
                            + "persists, please send an error report.");
                    ReassignView.this.dispose();
                }
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

        _toGroupList = new GenericJList<Group>();
        _toGroupList.setEnabled(false);
        JScrollPane toStudentSP = new JScrollPane();
        toStudentSP.setViewportView(_toGroupList);
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
        _fromGroupList.clearSelection();

        _numStudentsSpinner.setVisible(true);
        _assignTypeLabel.setText("Random Student(s):");

        _assignButton.setEnabled(_fromGroupList.getModel().getSize() > 0);
    }

    private void enableUseSelectedAssignment() {
        _fromRandom.clearSelection();

        _numStudentsSpinner.setVisible(false);
        _assignTypeLabel.setText("Selected Student(s):");

        _assignButton.setEnabled(_fromGroupList.getSelectedValue() != null);
    }

    private void updateAssignmentAndPart() throws ServicesException, SQLException {
        this.setTitle(_asgn + " - [" + Allocator.getCourseInfo().getCourse() +"] Assignment Distributor");

        _unresolvedHandins = Allocator.getGradingServices().resolveMissingStudents(_asgn);
        if (_unresolvedHandins == null) {
            this.dispose();
            return;
        }

        if (_dp == null) {
            this.disableAll();
            return;
        }
        else {
            this.enableAll();
        }

        //default selections
        _fromTAList.clearSelection();
        _fromUnassigned.setSelectedIndex(0);
        _toTAList.clearSelection();
        _toUnassigned.setSelectedIndex(0);

        try {
            this.updateFromList();
            this.updateToList();
        } catch (CakeHatDBIOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateFromList() throws SQLException, ServicesException, CakeHatDBIOException {
        List<Group> groupsToDisplay;

        //if UNASSIGNED is selected
        if (!_fromUnassigned.isSelectionEmpty()) {
            _unassignedGroups = Allocator.getGradingServices().getGroupsForHandins(_asgn, _unresolvedHandins).values();
            _unassignedGroups.removeAll(Allocator.getDatabaseIO().getAllAssignedGroups(_dp));
            groupsToDisplay = new LinkedList<Group>(_unassignedGroups);

            _assignButton.setEnabled(_unassignedGroups.size() > 0);
            _numUnassignedLabel.setText(String.format("%d unassigned students to choose from", _unassignedGroups.size()));
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMaximum(_unassignedGroups.size());
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setValue(_unassignedGroups.size() == 0 ? 0 : 1);
        }
        else {
            TA fromTA = _fromTAList.getSelectedValue();
            Collection<Group> studentsAssigned = Allocator.getDatabaseIO().getGroupsAssigned(_dp, fromTA);
            groupsToDisplay = new LinkedList<Group>(studentsAssigned);

            _assignButton.setEnabled(studentsAssigned.size() > 0);
            _numUnassignedLabel.setText(String.format("%d students to chose from TA %s",
                                                      studentsAssigned.size(), fromTA));
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setMaximum(studentsAssigned.size());
            ((SpinnerNumberModel) _numStudentsSpinner.getModel()).setValue(studentsAssigned.size() == 0 ? 0 : 1);
        }

        Collections.sort(groupsToDisplay);
        _fromGroupList.setListData(groupsToDisplay);

        if (_fromRandom.isSelectionEmpty()) {
            _fromGroupList.setSelectedIndex(0);
        }
    }

    private void updateToList() throws SQLException, ServicesException, CakeHatDBIOException {
        List<Group> groupsToDisplay;

        if (!_toUnassigned.isSelectionEmpty()) {
            _unassignedGroups = Allocator.getGradingServices().getGroupsForHandins(_asgn, _unresolvedHandins).values();
            _unassignedGroups.removeAll(Allocator.getDatabaseIO().getAllAssignedGroups(_dp));
            groupsToDisplay = new LinkedList<Group>(_unassignedGroups);
        }
        else if (!_toTAList.isSelectionEmpty()) {
            TA toTA = _toTAList.getSelectedValue();
            groupsToDisplay = new LinkedList<Group>(Allocator.getDatabaseIO().getGroupsAssigned(_dp, toTA));
        }
        else {
            groupsToDisplay = new LinkedList<Group>();
        }

        Collections.sort(groupsToDisplay);
        _toGroupList.setListData(groupsToDisplay);
    }

    private void handleAssignButtonClick() throws SQLException, ServicesException, RubricException, CakeHatDBIOException {
        if (!_fromRandom.isSelectionEmpty()) {
            this.handleRandomAssignButtonClick();
        }
        else {
            this.handleSelectedAssignButtonClick();
        }
    }

    private void handleSelectedAssignButtonClick() throws SQLException, ServicesException, RubricException, CakeHatDBIOException {
        Collection<Group> groups = new ArrayList<Group>(_fromGroupList.getGenericSelectedValues());

        //assigning a student who was previously assigned to UNASSIGNED
        if (!_fromUnassigned.isSelectionEmpty()) {

            //only need to do anything if we're not "reassigning" back to UNASSIGNED
            if (_toUnassigned.isSelectionEmpty()) {
                TA ta = _toTAList.getSelectedValue();

                Iterator<Group> iterator = groups.iterator();
                while (iterator.hasNext()) {
                    Group group = iterator.next();
                    if (!Allocator.getGradingServices().isOkToDistribute(group, ta)) {
                        iterator.remove();
                        continue;
                    }

                    try {
                        Allocator.getDatabaseIO().assignGroupToGrader(group, _dp, ta);
                    } catch (CakeHatDBIOException ex) {
                        new ErrorView(ex, "Reassigning failed because the student"
                                + " was still in another TA's distribution even-though"
                                + " they were listed as unassigned.");
                    }
                }

                //create rubrics for any DPs for which the Group does not already
                //have a rubric; do not overwrite existing rubrics
                Allocator.getRubricManager().distributeRubrics(_dp.getHandin(), groups,
                                                               Allocator.getConfigurationInfo().getMinutesOfLeniency(),
                                                               DistributionRequester.DO_NOTHING_REQUESTER,
                                                               OverwriteMode.KEEP_EXISTING);
            }
        }

        //reassigning a student from one TA to another
        else {
            //"reassigning" to UNASSIGNED (i.e., unassigning)
            if (!_toUnassigned.isSelectionEmpty()) {
                TA oldTA = _fromTAList.getSelectedValue();

                for (Group group : groups) {
                    //modify the distribution
                    Allocator.getDatabaseIO().unassignGroupFromGrader(group, _dp, oldTA);
                }
            }

            //reassigning to another TA
            else {
                TA oldTA = _fromTAList.getSelectedValue();
                TA newTA = _toTAList.getSelectedValue();

                for (Group group : groups) {
                    if (!Allocator.getGradingServices().isOkToDistribute(group, newTA)) {
                        continue;
                    }

                    //modify the distribution
                    Allocator.getDatabaseIO().unassignGroupFromGrader(group, _dp, oldTA);

                    try {
                        Allocator.getDatabaseIO().assignGroupToGrader(group, _dp, newTA);
                    } catch (CakeHatDBIOException ex) {
                        new ErrorView(ex, "Reassigning failed because the student"
                                + " was still in another TA's distribution. There"
                                + " must have been an issue removing them from the"
                                + " old TAs distribution.");
                    }
                }
            }
        }

        this.updateFromList();
        this.updateToList();
        _studentFilterBox.requestFocus();
    }

    private void handleRandomAssignButtonClick() throws SQLException, ServicesException, RubricException, CakeHatDBIOException {
        TA toTA = _toTAList.getSelectedValue();
        TA fromTA = _fromTAList.getSelectedValue();

        List<Group> groupsToChoseFrom;

        //assigning students who were previously assigned to UNASSIGNED
        if (!_fromUnassigned.isSelectionEmpty()) {
            groupsToChoseFrom = new ArrayList<Group>(_unassignedGroups);
        }
        else {
            groupsToChoseFrom = new ArrayList<Group>(Allocator.getDatabaseIO().getGroupsAssigned(_dp, fromTA));
        }
        Collections.shuffle(groupsToChoseFrom);

        Collection<Group> groupsToAssign = new LinkedList<Group>();

        int numStudentsToAssign = (Integer) _numStudentsSpinner.getValue();
        int numGroupsAssignedSoFar = 0;

        //assigning to UNASSIGNED; no need to check blacklist
        if (toTA == null) {
            for (Group group : groupsToChoseFrom) {
                if (numGroupsAssignedSoFar == numStudentsToAssign) {
                    break;
                }

                groupsToAssign.add(group);
                numGroupsAssignedSoFar++;
            }
        }

        //attempting to assing to a new TA; need to check blacklist
        else {
            for (Group group : groupsToChoseFrom) {
                if (numGroupsAssignedSoFar == numStudentsToAssign) {
                    break;
                }

                Map<TA, Collection<String>> blacklistMap = new HashMap<TA, Collection<String>>();
                blacklistMap.put(toTA, Allocator.getDatabaseIO().getTABlacklist(toTA));

                if (toTA != null && !Allocator.getGradingServices().groupMemberOnTAsBlacklist(group, blacklistMap)) {
                    groupsToAssign.add(group);
                    numGroupsAssignedSoFar++;
                }
            }
        }

        //we weren't able to assign as many students as requested; show error and return
        if (numGroupsAssignedSoFar < numStudentsToAssign) {
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
            Iterator<Group> iterator = groupsToAssign.iterator();
            while (iterator.hasNext()) {
                Group group = iterator.next();

                try {
                    //update distribution
                    Allocator.getDatabaseIO().assignGroupToGrader(group, _dp, toTA);
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "Reassigning failed because the student"
                                + " was still in another TA's distribution.");
                }

                if (fromTA != null) {
                    Allocator.getDatabaseIO().unassignGroupFromGrader(group, _dp, fromTA);
                }
            }
            
            //create rubrics for any DPs for which the Group does not already
            //have a rubric; do not overwrite existing rubrics
            Allocator.getRubricManager().distributeRubrics(_dp.getHandin(), groupsToAssign,
                                                           Allocator.getConfigurationInfo().getMinutesOfLeniency(),
                                                           DistributionRequester.DO_NOTHING_REQUESTER,
                                                           OverwriteMode.KEEP_EXISTING);
        }

        //assigning to UNASSIGNED from a TA
        else if (fromTA != null) {
            for (Group group : groupsToAssign) {
                Allocator.getDatabaseIO().unassignGroupFromGrader(group, _dp, fromTA);
            }
        }

        this.updateFromList();
        this.updateToList();
    }

    private void filterStudentLogins(KeyEvent evt) throws SQLException, CakeHatDBIOException {
        //term to filter against
        String filterTerm = _studentFilterBox.getText();

        List<Group> matchingLogins = new LinkedList<Group>();

        //if "UNASSIGNED" is selected, filter from unassigned students
        if (!_fromUnassigned.isSelectionEmpty()) {
            for (Group group : _unassignedGroups) {
                if (group.getName().startsWith(filterTerm)) {
                    matchingLogins.add(group);
                }
            }
        }

        //otherwise, filter from the selected TA's assigned students
        else {
            TA selectedTA = _fromTAList.getSelectedValue();
            for (Group group : Allocator.getDatabaseIO().getGroupsAssigned(_dp, selectedTA)) {
                if (group.getName().startsWith(filterTerm)) {
                    matchingLogins.add(group);
                }
            }
        }

        _fromGroupList.setListData(matchingLogins);

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (matchingLogins.size() > 0) {
                _studentFilterBox.setText(matchingLogins.get(0).getName());
                _fromGroupList.setSelectedIndex(0);
                _assignButton.requestFocus();
            }
        }
    }

    private void enableAll() {
        _fromUnassigned.setEnabled(true);
        _fromTAList.setEnabled(true);
        _fromRandom.setEnabled(true);
        _fromGroupList.setEnabled(true);
        
        _toUnassigned.setEnabled(true);
        _toTAList.setEnabled(true);
        _assignButton.setEnabled(true);
    }

    private void disableAll() {
        _fromUnassigned.clearSelection();
        _fromUnassigned.setEnabled(false);
        _fromTAList.setEnabled(false);
        _fromRandom.clearSelection();
        _fromRandom.setEnabled(false);
        _fromGroupList.clearList();
        _fromGroupList.setEnabled(false);

        _toUnassigned.clearSelection();
        _toUnassigned.setEnabled(false);
        _toTAList.setEnabled(false);
        _toGroupList.clearList();
        _assignButton.setEnabled(false);

        _numStudentsSpinner.setVisible(false);
        _assignTypeLabel.setText("");
        _numUnassignedLabel.setText("");
    }

    public static void main(String[] argv) {
        Assignment asgn = Allocator.getConfigurationInfo().getHandinAssignments().iterator().next();
        new ReassignView(asgn, null);
    }
}