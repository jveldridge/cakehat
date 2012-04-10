package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.TA;
import support.resources.icons.IconLoader;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import support.ui.DescriptionProvider;
import support.ui.GenericJList;
import support.ui.FormattedLabel;
import support.ui.ModalDialog;
import support.ui.PartialDescriptionProvider;

/**
 * Provides an interface to allow administrators to manually distribute students to TAs and reassign already distributed
 * students.
 *
 * @author jeldridg
 */
class ManualDistributorView extends JDialog {
    
    private static final int LIST_HEIGHT = 300;
    private static final int LIST_WIDTH = 130;
    private static final int BUTTON_WIDTH = 200;
    private static final int TEXT_HEIGHT = 17;
    
    private final Part _part;
    
    private List<TA> _tas;
    private GenericJList<String> _fromUnassigned;
    private GenericJList<TA> _fromTAList;
    private GenericJList<String> _fromRandom;
    private GenericJList<Group> _fromGroupList;
    private GenericJList<String> _toUnassigned;
    private GenericJList<TA> _toTAList;
    private GenericJList<Group> _toGroupList;
    
    private final TADescriptionProvider _taDescriptionProvider;
    private final UnassignedDescriptionProvider _unassignedDescriptionProvider;

    private JButton _assignButton;
    private JTextField _studentFilterBox;
    private JSpinner _randomStudentsSpinner;
    private JLabel _randomStudentLabel;
    private Set<Group> _groupsForAsgn;
    private Set<String> _unresolvedHandins;
    private Set<Group> _groupsWithHandins;
    private Set<Group> _unassignedGroups;
    
    public ManualDistributorView(Part part, Window owner) {
        super(owner, "Manual Distributor : " + part.getFullDisplayName(), ModalityType.MODELESS);
        
        _part = part;

        _taDescriptionProvider = new TADescriptionProvider();
        _unassignedDescriptionProvider = new UnassignedDescriptionProvider();
        try {
            _tas = new ArrayList<TA>(Allocator.getDataServices().getTAs());
            Collections.sort(_tas);
            
            _groupsForAsgn = Allocator.getDataServices().getGroups(_part.getAssignment());
            
            _unresolvedHandins = Allocator.getGradingServices().resolveUnexpectedHandins(_part.getGradableEvent());
            if (_unresolvedHandins == null) {
                this.dispose();
                return;
            }
            
            _groupsWithHandins = ImmutableSet.copyOf(Allocator.getGradingServices()
                    .getGroupsForHandins(_part.getGradableEvent(), _unresolvedHandins).values());
            
            _unassignedGroups = new HashSet<Group>(_groupsForAsgn);            
            _unassignedGroups.removeAll(Allocator.getDataServices().getAssignedGroups(_part));

            this.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
            contentPanel.add(this.getLeftPanel(), BorderLayout.WEST);
            contentPanel.add(this.getCenterPanel(), BorderLayout.CENTER);
            contentPanel.add(this.getRightPanel(), BorderLayout.EAST);

            // Pad everything so it is not directly touching the frame border
            int padding = 5;
            this.add(Box.createVerticalStrut(padding), BorderLayout.NORTH);
            this.add(Box.createVerticalStrut(padding), BorderLayout.SOUTH);
            this.add(Box.createHorizontalStrut(padding), BorderLayout.EAST);
            this.add(Box.createHorizontalStrut(padding), BorderLayout.WEST);
            this.add(contentPanel, BorderLayout.CENTER);
            
            //default selections
            _fromUnassigned.selectFirst();
            _toTAList.selectFirst();

            this.updateFromList();
            this.updateToList();
            this.refreshTALists();
            
            this.pack();
            this.setResizable(false);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.setLocationRelativeTo(owner);
            this.setVisible(true);

            _studentFilterBox.requestFocus();
        } catch (ServicesException ex) {
            new ErrorView(ex, "An error occurred while initializing the interface. "
                    + "This view will now close.  If this problem "
                    + "persists, please send an error report.");
            ManualDistributorView.this.dispose();
        }
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
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("From"));

        _fromUnassigned = new GenericJList<String>(Arrays.asList("UNASSIGNED"), _unassignedDescriptionProvider);
        _fromUnassigned.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _fromUnassigned.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _fromTAList.clearSelection();
            }
        });

        _fromTAList = new GenericJList<TA>(_tas, _taDescriptionProvider);
        _fromTAList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _fromTAList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    updateFromList();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. "
                            + "This view will now close.  If this problem "
                            + "persists, please send an error report.");
                    ManualDistributorView.this.dispose();
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
        _studentFilterBox.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));

        _studentFilterBox.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent de) { filterStudentLogins(); }
            public void removeUpdate(DocumentEvent de) { filterStudentLogins(); }
            public void changedUpdate(DocumentEvent de){ filterStudentLogins(); }
        });

        //If enter key is released and the from group list has at least one
        //entry, select the first entry in the from group list and switch focus
        //to the assign button.
        //It is VERY important that this happen on key release (as opposed to
        //key pressed).
        //When this code executes, it will end with focus being set on the
        //assign button. The assign button, aside from responding to click, also
        //responds to the enter key being released.
        //If this responded to the enter key being pressed, then when the enter
        //key is released the assign button will respond because it will have focus.
        _studentFilterBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER &&
                   _fromGroupList.hasListData()) {
                    _studentFilterBox.setText(_fromGroupList.getListData().get(0).getName());
                    _fromGroupList.selectFirst();
                    _assignButton.requestFocus();
                }
            }
        });

        _fromRandom = new GenericJList<String>("RANDOM");
        _fromRandom.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _fromRandom.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableAssigningRandom();
            }
        });

        _fromGroupList = new GenericJList<Group>(_groupDescriptionProvider);
        _fromGroupList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enableAssigningSelected();
            }
        });

        JScrollPane fromStudentSP = new JScrollPane();
        fromStudentSP.setViewportView(_fromGroupList);

        //height of fromStudentList needs to be shrunk by the height of the RANDOM list
        //plus the buffer space to align with the bottoms of the fromTAList and the screen
        int fromStudentListHeight = LIST_HEIGHT - (TEXT_HEIGHT + 10);
        fromStudentSP.setPreferredSize(new Dimension(LIST_WIDTH, fromStudentListHeight));

        JPanel leftPanel_upper = new JPanel(new BorderLayout(5, 5));
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
        centerPanel.setPreferredSize(new Dimension(2 * LIST_WIDTH, LIST_HEIGHT));
        centerPanel.setLayout(new GridLayout(0, 1));

        JPanel assignControlPanel = new JPanel();
        assignControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        //center asgnControlPanel
        centerPanel.add(Box.createVerticalBox());
        centerPanel.add(assignControlPanel);
        centerPanel.add(Box.createVerticalBox());

        _randomStudentLabel = FormattedLabel.asContent("Random Student(s):");
        //Initially do not show the text, instead of making invisible via
        //setVisible(false) which would affect layout, just hide the text
        _randomStudentLabel.setForeground(new Color(0, 0, 0, Color.TRANSLUCENT));
        _randomStudentLabel.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        assignControlPanel.add(_randomStudentLabel);

        _randomStudentsSpinner = new JSpinner(new SpinnerNumberModel());
        _randomStudentsSpinner.setPreferredSize(new Dimension(LIST_WIDTH / 2, TEXT_HEIGHT));
        assignControlPanel.add(_randomStudentsSpinner);
        _randomStudentsSpinner.setVisible(false);

        _assignButton = Allocator.getGeneralUtilities()
                .createTextCenteredButton("Assign",
                IconLoader.loadIcon(IconLoader.IconSize.s16x16, IconLoader.IconImage.GO_NEXT),
                BUTTON_WIDTH, false);
        _assignButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 25));
        assignControlPanel.add(_assignButton);

        _assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    handleAssignButtonClick();
                } catch (SQLException ex) {
                    new ErrorView(ex, "An error occurred during assignment. "
                            + "No changes have been made.");
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred during assignment. "
                            + "No changes have been made.");
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "An error occurred during assignment. "
                            + "No changes have been made.");
                }
            }
        });
        
        _assignButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    _assignButton.doClick();
                    _studentFilterBox.setText(null);
                    _studentFilterBox.requestFocus();
                }
            }
        });

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
        rightPanel.setBorder(BorderFactory.createTitledBorder("To"));

        _toUnassigned = new GenericJList<String>(Arrays.asList("UNASSIGNED"), _unassignedDescriptionProvider);
        _toUnassigned.setPreferredSize(new Dimension(LIST_WIDTH, TEXT_HEIGHT));
        _toUnassigned.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                _toTAList.clearSelection();
            }
        });

        _toTAList = new GenericJList<TA>(_tas, _taDescriptionProvider);
        _toTAList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _toTAList.selectFirst();
        _toTAList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    updateToList();
                } catch (ServicesException ex) {
                    new ErrorView(ex, "An error occurred while updating the interface. "
                            + "This view will now close.  If this problem "
                            + "persists, please send an error report.");
                    ManualDistributorView.this.dispose();
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
        toTASP.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT - 5));

        _toGroupList = new GenericJList<Group>();
        _toGroupList.setEnabled(false);
        JScrollPane toStudentSP = new JScrollPane();
        toStudentSP.setViewportView(_toGroupList);
        toStudentSP.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));

        JPanel rightPanel_upper = new JPanel();
        rightPanel_upper.setLayout(new BorderLayout(5, 5));
        rightPanel.add(rightPanel_upper, BorderLayout.NORTH);

        JPanel toTAPanel = new JPanel();
        toTAPanel.setLayout(new BorderLayout());
        toTAPanel.add(_toUnassigned, BorderLayout.NORTH);
        toTAPanel.add(toTASP, BorderLayout.SOUTH);
        rightPanel.add(toTAPanel, BorderLayout.WEST);
        rightPanel.add(toStudentSP, BorderLayout.EAST);

        return rightPanel;
    }

    private void enableAssigningRandom() {
        _fromGroupList.clearSelection();

        //Make the text visible
        _randomStudentLabel.setForeground(Color.BLACK);
        _randomStudentsSpinner.setVisible(true);

        _assignButton.setEnabled(_fromGroupList.hasListData());
    }

    private void enableAssigningSelected() {
        _fromRandom.clearSelection();

        //Make the text invisible
        _randomStudentLabel.setForeground(new Color(0, 0, 0, Color.TRANSLUCENT));
        _randomStudentsSpinner.setVisible(false);

        _assignButton.setEnabled(_fromGroupList.getSelectedValue() != null);
    }

    private void updateFromList() throws ServicesException {
        List<Group> groupsToDisplay;

        //if UNASSIGNED is selected
        if (!_fromUnassigned.isSelectionEmpty()) {
            _unassignedGroups = new HashSet<Group>(Allocator.getDataServices().getGroups(_part.getAssignment()));
            _unassignedGroups.removeAll(Allocator.getDataServices().getAssignedGroups(_part));
            groupsToDisplay = new ArrayList<Group>(_unassignedGroups);

            _assignButton.setEnabled(_unassignedGroups.size() > 0);
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setMaximum(_unassignedGroups.size());
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setValue(_unassignedGroups.isEmpty()? 0 : 1);
        }
        else {
            TA fromTA = _fromTAList.getSelectedValue();
            Collection<Group> studentsAssigned = Allocator.getDataServices().getAssignedGroups(_part, fromTA);
            groupsToDisplay = new ArrayList<Group>(studentsAssigned);

            _assignButton.setEnabled(studentsAssigned.size() > 0);
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setMinimum(1);
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setMaximum(studentsAssigned.size());
            ((SpinnerNumberModel) _randomStudentsSpinner.getModel()).setValue(studentsAssigned.isEmpty() ? 0 : 1);
        }

        Collections.sort(groupsToDisplay);
        _fromGroupList.setListData(groupsToDisplay);
        filterStudentLogins();

        if (_fromRandom.isSelectionEmpty()) {
            _fromGroupList.selectFirst();
        }
    }

    private void updateToList() throws ServicesException {
        List<Group> groupsToDisplay;

        if (!_toUnassigned.isSelectionEmpty()) {
            _unassignedGroups = new HashSet<Group>(Allocator.getDataServices().getGroups(_part.getAssignment()));
            _unassignedGroups.removeAll(Allocator.getDataServices().getAssignedGroups(_part));
            groupsToDisplay = new ArrayList<Group>(_unassignedGroups);
        } else if (!_toTAList.isSelectionEmpty()) {
            TA toTA = _toTAList.getSelectedValue();
            groupsToDisplay = new ArrayList<Group>(Allocator.getDataServices().getAssignedGroups(_part, toTA));
        } else {
            groupsToDisplay = new ArrayList<Group>();
        }

        Collections.sort(groupsToDisplay);
        _toGroupList.setListData(groupsToDisplay);
    }

    private void handleAssignButtonClick() throws SQLException, ServicesException, CakeHatDBIOException {
        if (!_fromRandom.isSelectionEmpty()) {
            this.handleRandomAssignButtonClick();
        } else {
            this.handleSelectedAssignButtonClick();
        }
        this.refreshTALists();
    }

    private void handleSelectedAssignButtonClick() throws ServicesException, CakeHatDBIOException {
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

                    Allocator.getDataServices().setGrader(_part, group, ta);
                }
            }
        }
        //reassigning a student from one TA to another
        else {
            //"reassigning" to UNASSIGNED (i.e., unassigning)
            if (!_toUnassigned.isSelectionEmpty()) {
                for (Group group : groups) {
                    //modify the distribution
                    Allocator.getDataServices().setGrader(_part, group, null);
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
                    Allocator.getDataServices().setGrader(_part, group, newTA);
                }
            }
        }

        this.updateFromList();
        this.updateToList();
        _studentFilterBox.requestFocus();
    }

    private void handleRandomAssignButtonClick() throws ServicesException, CakeHatDBIOException {
        TA toTA = _toTAList.getSelectedValue();
        TA fromTA = _fromTAList.getSelectedValue();

        List<Group> groupsToChoseFrom;

        //assigning students who were previously assigned to UNASSIGNED
        if (!_fromUnassigned.isSelectionEmpty()) {
            groupsToChoseFrom = new ArrayList<Group>(_unassignedGroups);
        } else {
            groupsToChoseFrom = new ArrayList<Group>(Allocator.getDataServices().getAssignedGroups(_part, fromTA));
        }
        Collections.shuffle(groupsToChoseFrom);

        Collection<Group> groupsToAssign = new ArrayList<Group>();

        int numStudentsToAssign = (Integer) _randomStudentsSpinner.getValue();
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

        //attempting to assign to a new TA; need to check blacklist
        else {
            for (Group group : groupsToChoseFrom) {
                if (numGroupsAssignedSoFar == numStudentsToAssign) {
                    break;
                }
                
                Collection<Student> blacklisted = Allocator.getDataServices().getBlacklist(toTA);
                if (!Allocator.getGeneralUtilities().containsAny(blacklisted, group.getMembers())) {
                    groupsToAssign.add(group);
                    numGroupsAssignedSoFar++;
                }
            }
        }

        //we weren't able to assign as many students as requested; show error and return
        if (numGroupsAssignedSoFar < numStudentsToAssign) {
            String errMsg = "Cannot assign this many students "
                    + "without violating the blacklist.\nIf you would like to "
                    + "override the blacklist, please manually select students "
                    + "to be distributed.\n";
            ModalDialog.showMessage(this, "Distribution Error", errMsg);
            return;
        }

        //assigning to a new TA
        if (toTA != null) {
            Iterator<Group> iterator = groupsToAssign.iterator();
            while (iterator.hasNext()) {
                Group group = iterator.next();
                Allocator.getDataServices().setGrader(_part, group, toTA);

            }
        } //assigning to UNASSIGNED from a TA
        else if (fromTA != null) {
            for (Group group : groupsToAssign) {
                Allocator.getDataServices().setGrader(_part, group, null);
            }
        }

        this.updateFromList();
        this.updateToList();
    }

    private void filterStudentLogins() {
        //term to filter against
        String filterTerm = _studentFilterBox.getText();

        List<Group> matchingLogins = new ArrayList<Group>();

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
            Collection<Group> groups;
            try {
                groups = Allocator.getDataServices().getAssignedGroups(_part, selectedTA);
            } catch (ServicesException ex) {
                new ErrorView(ex, "Unable to filter student/group list");
                return;
            }

            for (Group group : groups) {
                if (group.getName().startsWith(filterTerm)) {
                    matchingLogins.add(group);
                }
            }
        }

        Collections.sort(matchingLogins);
        _fromGroupList.setListData(matchingLogins);
    }

    private void refreshTALists() {
        _taDescriptionProvider.updateData();
        _fromTAList.refreshList();
        _toTAList.refreshList();
        
        _fromUnassigned.refreshList();
        _toUnassigned.refreshList();
    }

    private class UnassignedDescriptionProvider extends PartialDescriptionProvider<String> {
        @Override
        public String getDisplayText(String item) {
            String numStudents;

            if(_unassignedGroups == null) {
                numStudents = "?";
            } else {
                numStudents = Integer.toString(_unassignedGroups.size());
            }

            return "<html>UNASSIGNED<font color=gray> (" + numStudents +
                    ")</font></html>";
        }
    }
    
    private final DescriptionProvider<Group> _groupDescriptionProvider = new DescriptionProvider<Group>() {
        @Override
        public String getDisplayText(Group group) {
            if (_part.getGradableEvent().hasDigitalHandins()) {
                if (!_groupsWithHandins.contains(group)) {
                    return "<html><font color=#B40404>" + group.toString() + "</font></html>";
                }
            }
            
            return group.toString();
        }

        @Override
        public String getToolTipText(Group group) {
            if (_part.getGradableEvent().hasDigitalHandins()) {
                if (!_groupsWithHandins.contains(group)) {
                    return "Group does not have a handin for gradable event [" + _part.getGradableEvent() + "].";
                }
            }
            
            return null;
        }
    };

    private class TADescriptionProvider extends PartialDescriptionProvider<TA> {
        private Map<TA, Set<Group>> _distribution;

        public void updateData() {
            _distribution = null;
            try {
                _distribution = Allocator.getDataServices().getDistribution(_part);
            } catch (ServicesException ex) {
                new ErrorView(ex, "Unable to distribution data. The user " +
                        "interface will be unable to display the number of" +
                        "students/groups assigned to each TA.");
            }
        }

        @Override
        public String getDisplayText(TA ta) {
            String numStudents;

            if(_distribution == null) {
                numStudents = "?";
            }
            else if(!_distribution.containsKey(ta)) {
                numStudents = "0";
            }
            else {
                numStudents = Integer.toString(_distribution.get(ta).size());
            }

            String representation = "<html>" + ta.getLogin() +
                    "<font color=gray> (" + numStudents + ")</font></html>";

            return representation;
        }
    }

    public static void main(String[] argv) throws CakehatException {
        CakehatMain.initializeForTesting();
        
        Part part = null;
        for (Assignment asgn : Allocator.getDataServices().getAssignments()) {
            for (GradableEvent ge : asgn.getGradableEvents()) {
                if (!ge.getParts().isEmpty()) {
                    part = ge.getParts().get(0);
                    break;
                }
            }
        }
        
        if(part != null) {
            new ManualDistributorView(part, null).setVisible(true);
        } else {
            System.err.println("Cannot test view because the configuration contains no assignments with gradable events "
                    + "with parts.");
        }
    }
}