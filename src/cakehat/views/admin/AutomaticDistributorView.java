package cakehat.views.admin;

import support.ui.IntegerField;

import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import support.ui.GenericJComboBox;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.TA;
import cakehat.logging.ErrorReporter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import support.ui.FormattedLabel;
import support.ui.ModalDialog;

/**
 * Provides an interface for automatically creating a distribution for a gradable event.
 * 
 * @author jeldridg
 */
class AutomaticDistributorView extends JDialog {

    private static int GRADER_PANEL_WIDTH = 600;
    private static int GRADER_PANEL_HEIGHT = 25;

    private GradableEvent _gradableEvent;

    private List<TA> _gradingTAs;
    private List<TA> _nonGradingTAs;
    private Set<String> _remainingBadHandins;

    private Map<TA, GraderPanel> _graderPanels;
    private JPanel _graderPanelsPanel;
    private GenericJComboBox<TA> _selectGraderToAddBox;
    private JButton _addGraderButton;

    private JButton _setUpGradingButton;

    public AutomaticDistributorView(GradableEvent ge, Window owner) {
        super(owner, "Automatic Distributor : " + ge.getFullDisplayName(), ModalityType.MODELESS);
        
        if (!ge.hasDigitalHandins()) {
            throw new RuntimeException("The automatic distributor can only be used for gradable events with digital "
                    + "handins.");
        }
        
        _gradableEvent = ge;

        try {
            _remainingBadHandins = Allocator.getGradingServices().resolveUnexpectedHandins(_gradableEvent);
        } catch (ServicesException ex) {
            ErrorReporter.report(ex);
            this.dispose();
            return;
        }

        //null means that cancel was clicked
        if (_remainingBadHandins == null) {
            this.dispose();
            return;
        }

        _gradingTAs = new ArrayList<TA>(Allocator.getDataServices().getDefaultGraders());
        _nonGradingTAs = new ArrayList<TA>(Allocator.getDataServices().getTAs());
        _nonGradingTAs.removeAll(_gradingTAs);

        this.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 1));

        JPanel instructionsPanel = new JPanel();

        instructionsPanel.add(FormattedLabel.asContent("Enter the number of handins above or below the " +
                "average for each TA to grade" +
                "<br/>" +
                "<font color=gray>Examples: -2 = two fewer to grade, 3 = three more to grade</font>"));
        topPanel.add(instructionsPanel);

        JPanel addGraderPanel = new JPanel();
        addGraderPanel.add(FormattedLabel.asContent("Add Grader: "));

        _selectGraderToAddBox = new GenericJComboBox<TA>(_nonGradingTAs);
        _selectGraderToAddBox.setPreferredSize(new Dimension(100, 20));
        addGraderPanel.add(_selectGraderToAddBox);

        _addGraderButton = new JButton("Add");
        _addGraderButton.setPreferredSize(new Dimension(60, 20));
        _addGraderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TA toAdd = _selectGraderToAddBox.getSelectedItem();
                _gradingTAs.add(toAdd);
                _nonGradingTAs.remove(toAdd);
                updateInterface();
            }
        });
        addGraderPanel.add(_addGraderButton);

        topPanel.add(addGraderPanel);

        _graderPanels = new HashMap<TA, GraderPanel>();

        _graderPanelsPanel = new JPanel();
        _graderPanelsPanel.setLayout(new GridLayout(0, 1));
        this.updateInterface();

        JPanel buttonPanelPanel = new JPanel(new BorderLayout());
        JPanel topButtonPanel = new JPanel();
        JPanel bottomButtonPanel = new JPanel();

        Dimension buttonSize = new Dimension(220, 25);
        
        _setUpGradingButton = new JButton("Set Up Grading");
        _setUpGradingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (oneClickGradingSetup()) {
                        ModalDialog.showMessage(AutomaticDistributorView.this, "Success", "Grading setup succeeded");
                    }
                    else {
                        ModalDialog.showMessage(AutomaticDistributorView.this, "Failed", "Grading setup failed");
                    }
                } catch (ServicesException ex) {
                    ErrorReporter.report("Grading setup failed because a ServicesException was thrown.", ex);
                } catch (IOException ex) {
                    ErrorReporter.report("Grading setup failed because an IOException was thrown.", ex);
                }
            }
        });
        _setUpGradingButton.setPreferredSize(buttonSize);
        topButtonPanel.add(_setUpGradingButton);

        JPanel separationPanel = new JPanel(new BorderLayout());
        separationPanel.add(Box.createRigidArea(new Dimension(200, 10)), BorderLayout.NORTH);
        separationPanel.add(new JSeparator(), BorderLayout.CENTER);
        separationPanel.add(Box.createRigidArea(new Dimension(200, 10)), BorderLayout.SOUTH);

        buttonPanelPanel.add(topButtonPanel, BorderLayout.NORTH);
        buttonPanelPanel.add(separationPanel, BorderLayout.CENTER);
        buttonPanelPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(_graderPanelsPanel, BorderLayout.CENTER);
        this.add(buttonPanelPanel, BorderLayout.SOUTH);

        try {
            this.setIconImage(IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.ACCESSORIES_TEXT_EDITOR));
        } catch (Exception e) {}

        this.pack();
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(owner);
        this.setVisible(true);
    }

    private void updateInterface() {
        Collections.sort(_gradingTAs);
        Collections.sort(_nonGradingTAs);
        
        _selectGraderToAddBox.setItems(_nonGradingTAs);
        _addGraderButton.setEnabled(_nonGradingTAs.size() > 0);

        _graderPanelsPanel.removeAll();

        for (TA grader : _gradingTAs) {
            if (!_graderPanels.containsKey(grader)) {
                GraderPanel gp = new GraderPanel(grader);
                _graderPanels.put(grader, gp);
            }
            _graderPanelsPanel.add(_graderPanels.get(grader));
        }

        _graderPanelsPanel.revalidate();
        this.pack();
    }

    /**
     * Returns true if distribution was completed successfully, and false otherwise.  A return value of false indicates
     * that either that the user canceled the distribution or that creating a complete distribution without violating
     * the blacklist was not successful.
     * 
     * @return
     * @throws ServicesException
     * @throws IOException
     */
    private boolean oneClickGradingSetup() throws ServicesException, IOException {
        
        if (!Allocator.getDataServices().isDistEmpty(_gradableEvent)) {
            if (!ModalDialog.showConfirmation(this, "Confirm Overwrite", "Any existing distribution will be  "
                    + "overwritten, though no existing student grading sheets will be deleted or modified. "
                    + "Do you wish to continue?", "Yes", "No")) {
                return false;
            }
        }

        return this.generateDistribution();
    }

    /**
     * Distributes Groups to TAs, respecting blacklists and storing the result in
     * the database.  Returns true if distribution was completed successfully, and
     * false otherwise.  A return value of false means that either that the user
     * canceled the operation or that attempting to make a complete distribution
     * without violating the blacklist was unsuccessful and that the user chose not to
     * use the incomplete distribution.  If false is returned, no changes will have been
     * made to the database.
     *
     * @return
     * @throws ServicesException
     * @throws SQLException
     */
    private boolean generateDistribution() throws ServicesException, IOException {
        //figure out which parts are being graded by which TAs
        Map<Part, Collection<TA>> graderMap = new HashMap<Part, Collection<TA>>();
        for (TA grader : _gradingTAs) {
            Part part = _graderPanels.get(grader).getPartToGrade();

            if (!graderMap.containsKey(part)) {
                graderMap.put(part, new ArrayList<TA>());
            }
            graderMap.get(part).add(grader);
        }

        //check to make sure that there are graders to distribute to for each part
        Collection<Part> partsWithoutGraders = new ArrayList<Part>();
        for (Part dp : _gradableEvent.getParts()) {
            if (!graderMap.containsKey(dp)) {
                partsWithoutGraders.add(dp);
            }
        }

        if (!partsWithoutGraders.isEmpty()) {
            ModalDialog.showMessage(this, "Distribution Error", "The following parts do not have TAs assigned to " +
                    "grade them: " + partsWithoutGraders + ". Students cannot be distributed.");
            return false;
        }

        Set<String> handinNames = _gradableEvent.getDigitalHandinNames();
        handinNames.removeAll(_remainingBadHandins);
        Map<String, Group> groups = Allocator.getGradingServices().getGroupsForHandins(_gradableEvent, _remainingBadHandins);

        //construct a Map that will be used to store the result of distribution for each Part
        Map<Part, DistributionResponse> distribution = new HashMap<Part, DistributionResponse>();

        String message = "Not all students were successfully distributed for all parts.\n " +
                         "For each part, the result of distribution is shown below.  Please\n" +
                         "choose whether to use the incomplete distribution (in which case the undistributed\n" +
                         "students must be distributed manually) or to discard the distribution.\n\n";
        boolean someStudentsUndistributed = false;
        for (Part part : _gradableEvent.getParts()) {
            DistributionResponse resp = generateDistForPart(graderMap.get(part), handinNames, groups);

            message += "Part " + part + ": ";
            if (resp.getProblemStudents().isEmpty()) {
                message += "all students were distributed successfully\n";
            }
            else {
                message += "the following students could not be distributed without\n" +
                           "violating a TA's blacklist: " + resp.getProblemStudents() + "\n";
                someStudentsUndistributed = true;
            }

            distribution.put(part, resp);
        }

        if (someStudentsUndistributed) {
            if (!ModalDialog.showConfirmation(this, "Use distribution?", message,
                    "Use Distribution", "Discard Distribution")) {
                return false;
            }
        }

        Map<Part, SetMultimap<TA, Group>> distForDB = new HashMap<Part, SetMultimap<TA, Group>>();
        for (Part part : _gradableEvent) {
            distForDB.put(part, distribution.get(part).getDistribution());
        }
        Allocator.getDataServices().setDistribution(distForDB);

        return true;
    }

    private DistributionResponse generateDistForPart(Collection<TA> graders,
                                                     Collection<String> handinNames,
                                                     Map<String, Group> groups) throws ServicesException {
        //get TA blacklists
        Map<TA, Collection<Student>> taBlacklists = new HashMap<TA, Collection<Student>>();
        for (TA grader : graders) {
            Collection<Student> taBlacklist = Allocator.getDataServices().getBlacklist(grader);
            taBlacklists.put(grader, taBlacklist);
        }

        //figure out how many groups each TA should grade
        Map<TA, Integer> numGroupsNeeded = this.calculateNumberOfGroupsPerTA(graders, handinNames.size());

        //find groups for whom some member is one one of the grading TAs' blacklist
        Collection<Group> blacklistedGroups = new ArrayList<Group>();
        for (String handinName : handinNames) {
            Group group = groups.get(handinName);

            if (Allocator.getGradingServices().isSomeGroupMemberBlacklisted(group, taBlacklists)) {
                blacklistedGroups.add(group);
            }
        }

        //assign blacklisted students first
        DistributionResponse blacklistedResponse = this.assignBlacklistedGroups(blacklistedGroups,
                                                                                numGroupsNeeded,
                                                                                taBlacklists);

        List<Group> remainingGroups = new ArrayList<Group>(groups.values());
        remainingGroups.removeAll(blacklistedGroups);

        //then assign all other students
        Map<TA, Collection<Group>> remainingDist = this.assignRemainingGroups(remainingGroups, numGroupsNeeded);

        //overallDist represents distribution of both Groups that have members blacklisted by
        //some TA and Groups in which no member is blacklisted
        SetMultimap<TA, Group> overallDist = HashMultimap.create(blacklistedResponse.getDistribution());
        for (TA ta : remainingDist.keySet()) {
            overallDist.putAll(ta, remainingDist.get(ta));
        }

        //students who could not be distributed without violating some TA's blacklisted
        Set<Group> overallUndistributed = new HashSet<Group>(blacklistedResponse.getProblemStudents());

        return new DistributionResponse(overallDist, overallUndistributed);
    }

    /**
     * calculate how many groups each TA should get to grade based on the modifiers entered
     *
     * @param graders
     * @param numGroups
     * @return - map between TA and number to grade
     */
    private Map<TA, Integer> calculateNumberOfGroupsPerTA(Collection<TA> graders, int numGroups) {

        //update total number of students based modifiers from table (only used for calc of avg)
        for (TA grader : graders) {
            int diffFromAvg = _graderPanels.get(grader).getNumDiff();

            //the extra (if TA gets less than average) students need to be go to the other TAs
            //therefore the average and thus the total must be higher
            numGroups -= diffFromAvg;
        }

        //average number of students for each ta
        int avg = (int) Math.floor((double) numGroups / (double) graders.size());

        //build hashmap of how many students each TA must grade
        HashMap<TA, Integer> numStudsNeeded = new HashMap<TA, Integer>();
        for (TA grader : graders) {
            numStudsNeeded.put(grader, _graderPanels.get(grader).getNumDiff() + avg);
        }

        return numStudsNeeded;
    }

    private DistributionResponse assignBlacklistedGroups(Collection<Group> groups,
                                         Map<TA, Integer> numStudsNeeded,
                                         Map<TA, Collection<Student>> graderBlacklists) {
        List<TA> graders = new ArrayList<TA>(numStudsNeeded.keySet());

        SetMultimap<TA, Group> distribution = HashMultimap.create();

        Set<Group> unDistributed = new HashSet<Group>();

        for (Group blGroup : groups) {
            Collections.shuffle(graders);
            boolean distributed = false;
            
            for (TA ta : graders) {
                //if ta's blacklist does not contain students from the group and ta's dist is not full
                if (Collections.disjoint(graderBlacklists.get(ta), blGroup.getMembers())
                        && numStudsNeeded.get(ta) > 0) {

                    distribution.put(ta, blGroup); //add student to ta's dist
                    numStudsNeeded.put(ta, numStudsNeeded.get(ta) - 1); //reduce num ta needs
                    distributed = true;
                    break;
                }
            }

            if (!distributed) {
                unDistributed.add(blGroup);
            }
        }

        return new DistributionResponse(distribution, unDistributed);
    }

    private Map<TA, Collection<Group>> assignRemainingGroups(List<Group> groups,
                                                       Map<TA, Integer> numStudsNeeded) {
        List<TA> graders = new ArrayList<TA>(numStudsNeeded.keySet());

        Map<TA, Collection<Group>> distribution = new HashMap<TA, Collection<Group>>();
        for (TA grader : graders) {
            distribution.put(grader, new ArrayList<Group>());
        }
        
        Collections.shuffle(graders);
        //fill TAs to limit
        for (TA ta : graders) {
            for (int i = 0; i < numStudsNeeded.get(ta); i++) {
                if (!groups.isEmpty()) {
                    distribution.get(ta).add(groups.remove(0));
                }
            }
        }

        //distribute remaining students (< # TAs of them) to random TAs
        //There will be < # TAs of them because we floored when we took the average so only at most n-1 could be left.
        Collections.shuffle(graders);
        for (TA ta : graders) {
            if (!groups.isEmpty()) {
                distribution.get(ta).add(groups.remove(0));
            } else {
                break;
            }
        }

        return distribution;
    }

    private class GraderPanel extends JPanel {

        private TA _grader;
        private IntegerField _numDiffField;
        private GenericJComboBox<Part> _partBox;

        public GraderPanel(TA grader) {
            _grader = grader;

            this.setLayout(new GridLayout(1, 0));
            this.setPreferredSize(new Dimension(GRADER_PANEL_WIDTH, GRADER_PANEL_HEIGHT));

            JLabel loginLabel = FormattedLabel.asContent(_grader.getLogin());
            loginLabel.setVerticalTextPosition(JLabel.CENTER);

            JPanel loginPanel = new JPanel(new BorderLayout(0, 0));
            loginPanel.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
            loginPanel.add(loginLabel, BorderLayout.CENTER);
            loginPanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

            _numDiffField = new IntegerField(0);

            Collection<Part> parts = AutomaticDistributorView.this._gradableEvent.getParts();
            _partBox = new GenericJComboBox<Part>(parts);

            JButton removeGraderButton = new JButton("Remove Grader");
            removeGraderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _gradingTAs.remove(_grader);
                    _nonGradingTAs.add(_grader);
                    updateInterface();
                }
            });

            JPanel removePanel = new JPanel(new BorderLayout(0, 0));
            removePanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
            removePanel.add(removeGraderButton, BorderLayout.CENTER);
            removePanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

            this.add(loginPanel);
            this.add(_numDiffField);
            this.add(_partBox);
            this.add(removePanel);
        }

        public int getNumDiff() {
            return _numDiffField.getIntValue();
        }

        public Part getPartToGrade() {
            return _partBox.getSelectedItem();
        }

    }

    private class DistributionResponse {

        private SetMultimap<TA, Group> _distribution;
        private Set<Group> _problemGroups;

        public DistributionResponse(SetMultimap<TA, Group> distribution, Set<Group> problemGroup) {
            _distribution = distribution;
            _problemGroups = problemGroup;
        }

        public SetMultimap<TA, Group> getDistribution() {
            return _distribution;
        }

        public Set<Group> getProblemStudents() {
            return _problemGroups;
        }

    }
    
    public static void main(String[] argv) throws CakehatException {
        CakehatMain.initializeForTesting();
        
        GradableEvent ge = null;
        for (Assignment asgn : Allocator.getDataServices().getAssignments()) {
            if (!asgn.getGradableEvents().isEmpty()) {
                ge = asgn.getGradableEvents().get(0);
                break;
            }
        }
        
        if (ge != null) {
            new AutomaticDistributorView(ge, null).setVisible(true);
        }
        else {
            System.err.println("Cannot test view because the configuration contains no assignments with gradable events "
                    + "with parts.");
        }
    }
}