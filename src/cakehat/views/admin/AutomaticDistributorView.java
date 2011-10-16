package cakehat.views.admin;

import cakehat.CakehatException;
import support.ui.IntegerField;
import cakehat.config.Assignment;
import cakehat.config.TA;
import cakehat.database.CakeHatDBIOException;
import cakehat.rubric.RubricException;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import cakehat.Allocator;
import cakehat.CakehatMain;
import support.ui.GenericJComboBox;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.database.Student;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.rubric.DistributionRequester;
import cakehat.views.shared.ErrorView;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JSeparator;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 * Provides an interface for automatically creating a distribution for an
 * assignment.
 * <br/><br/>
 * Also provides for automatically recalculating all handin statuses for an
 * assignment, and for creating new rubrics.
 * 
 * @author jeldridg
 */
class AutomaticDistributorView extends JFrame implements DistributionRequester {

    private static int GRADER_PANEL_WIDTH = 600;
    private static int GRADER_PANEL_HEIGHT = 25;

    private Assignment _asgn;

    private Vector<TA> _gradingTAs;
    private Vector<TA> _nonGradingTAs;
    private Collection<String> _remainingBadLogins;

    private Map<TA, GraderPanel> _graderPanels;
    private JPanel _graderPanelsPanel;
    private JComboBox _selectGraderToAddBox;
    private JButton _addGraderButton;

    private JDialog _progressDialog;
    private JProgressBar _progressBar;

    private JButton _setUpGradingButton, _makeNewRubricsButton, _recalculateHandinStatusesButton;

    public AutomaticDistributorView(Assignment asgn) {
        _asgn = asgn;
        this.setTitle("Automatic Distributor - " + _asgn.getName());

        try {
            _remainingBadLogins = Allocator.getGradingServices().resolveUnexpectedHandins(_asgn);
        } catch (ServicesException ex) {
            new ErrorView(ex);
            this.dispose();
            return;
        }

        //null means that cancel was clicked
        if (_remainingBadLogins == null) {
            this.dispose();
            return;
        }

        _gradingTAs = new Vector<TA>(Allocator.getConfigurationInfo().getDefaultGraders());
        _nonGradingTAs = new Vector<TA>(Allocator.getConfigurationInfo().getNonDefaultGraders());

        _progressDialog = new JDialog(this, "Distribution In Progress", true);
        _progressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        _progressBar = new JProgressBar(0, 100);
        _progressBar.setStringPainted(true);

        JPanel progressPanel = new JPanel();
        progressPanel.add(new JLabel("Distributing rubrics..."));
        progressPanel.add(_progressBar);

        _progressDialog.add(progressPanel);

        this.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 1));

        JPanel instructionsPanel = new JPanel();

        instructionsPanel.add(new JLabel("<html>Enter the number " +
                "of handins above or below the average for each TA to grade" +
                "<br/>" +
                "<font color=gray>Examples: -2 = two fewer to grade, " +
                "3 = three more to grade</font></html>"));
        topPanel.add(instructionsPanel);

        JPanel addGraderPanel = new JPanel();
        addGraderPanel.add(new JLabel("Add Grader: "));

        _selectGraderToAddBox = new JComboBox();
        _selectGraderToAddBox.setPreferredSize(new Dimension(100, 20));
        addGraderPanel.add(_selectGraderToAddBox);

        _addGraderButton = new JButton("Add");
        _addGraderButton.setPreferredSize(new Dimension(60, 20));
        _addGraderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TA toAdd = (TA) _selectGraderToAddBox.getSelectedItem();
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
                        JOptionPane.showMessageDialog(AutomaticDistributorView.this, "Success!");
                    }
                    else {
                        JOptionPane.showMessageDialog(AutomaticDistributorView.this, "Grading setup failed.");
                    }
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "Grading setup failed because a CakeHatDBIOException was thrown.");
                } catch (SQLException ex) {
                    new ErrorView(ex, "Grading setup failed because a SQLException was thrown.");
                } catch (ServicesException ex) {
                    new ErrorView(ex, "Grading setup failed because a ServicesException was thrown.");
                } catch (IOException ex) {
                    new ErrorView(ex, "Grading setup failed because an IOException was thrown.");
                } catch (RubricException ex) {
                    new ErrorView(ex, "Grading setup failed because a RubricException was thrown.");
                }
            }
        });
        _setUpGradingButton.setPreferredSize(buttonSize);
        topButtonPanel.add(_setUpGradingButton);

        _makeNewRubricsButton = new JButton("Make New Rubrics");
        _makeNewRubricsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Set<Group> distributedGroups = new HashSet<Group>();
                    for (DistributablePart dp : _asgn.getDistributableParts()) {
                        distributedGroups.addAll(Allocator.getDataServices().getAssignedGroups(dp));
                    }

                    if (makeNewRubrics(distributedGroups, true)) {
                        JOptionPane.showMessageDialog(AutomaticDistributorView.this, "Success!");
                    }
                } catch (ServicesException ex) {
                    new ErrorView(ex, "Could not make new rubrics.");
                } catch (RubricException ex) {
                    new ErrorView(ex, "Could not make new rubrics.");
                }
            }
        });
        _makeNewRubricsButton.setPreferredSize(buttonSize);
        bottomButtonPanel.add(_makeNewRubricsButton);

        _recalculateHandinStatusesButton = new JButton("Recalculate Handin Statuses");
        _recalculateHandinStatusesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Set<Group> distributedGroups = new HashSet<Group>();
                    for (DistributablePart dp : _asgn.getDistributableParts()) {
                        distributedGroups.addAll(Allocator.getDataServices().getAssignedGroups(dp));
                    }

                    if (recalculateHandinStatuses(distributedGroups, true)) {
                        JOptionPane.showMessageDialog(AutomaticDistributorView.this, "Success!");
                    }
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not recalculate handin statuses.");
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "Could not recalculate handin statuses.");
                } catch (ServicesException ex) {
                    new ErrorView(ex, "Could not recalculate handin statuses.");
                }
            }
        });
        _recalculateHandinStatusesButton.setPreferredSize(buttonSize);
        bottomButtonPanel.add(_recalculateHandinStatusesButton);

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

        //if no distribution has been made, disable all buttons except set up grading
        try {
            // get data services, not get database
            if (Allocator.getDatabase().isDistEmpty(_asgn.getPartIDs())) {
                _makeNewRubricsButton.setEnabled(false);
                _recalculateHandinStatusesButton.setEnabled(false);
            }
        } catch (SQLException ex) {
            new ErrorView(ex, "Could note determine whether or not a distribution exists " +
                              "for assignment " + _asgn + ".  Clicking the \"Make New Rubrics\"" +
                              "or \"Recalculate Handin Statuses\" buttons could result in " +
                              "undesired behavior.");
        }

        try {
            this.setIconImage(IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.ACCESSORIES_TEXT_EDITOR));
        } catch (Exception e) {}

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setResizable(false);
    }

    private void updateInterface() {
        Collections.sort(_gradingTAs);
        Collections.sort(_nonGradingTAs);
        
        _selectGraderToAddBox.setModel(new DefaultComboBoxModel(_nonGradingTAs));
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
     * Performs all grading setup: creating a distribution, calculating and storing handin statuses,
     * and making rubrics.  Returns true if the entire grading setup was completed successfully, and
     * false otherwise.  A return value of false indicates either that the user cancelled an operation
     * or that creating a complete distribution without violating the blacklist was not successful.
     * 
     * @return
     * @throws ServicesException
     * @throws SQLException
     * @throws CakeHatDBIOException
     * @throws IOException
     * @throws RubricException
     */
    private boolean oneClickGradingSetup() throws ServicesException, SQLException, CakeHatDBIOException, IOException, RubricException {
        boolean needConfirmation = false;
        if (!Allocator.getDatabase().isDistEmpty(_asgn.getPartIDs())) {
            needConfirmation = true;
        }
        if (!needConfirmation && Allocator.getDataServices().areHandinStatusesSet(_asgn.getHandin())) {
            needConfirmation = true;
        }
        if (!needConfirmation && Allocator.getRubricManager().areRubricsDistributed(_asgn.getHandin())) {
            needConfirmation = true;
        }

        if (needConfirmation) {
            int n = JOptionPane.showConfirmDialog(this, "Any existing distribution, handin statuses, " +
                                                        "and rubrics will be overwritten.  Do you wish to " +
                                                        "continue?",
                                                  "Confirm Overwrite",
                                                  JOptionPane.YES_NO_OPTION);
            if (n != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        boolean success = generateDistribution();

        Set<Group> distributedGroups = new HashSet<Group>();
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            distributedGroups.addAll(Allocator.getDataServices().getAssignedGroups(dp));
        }

        if (success) {
            success &= recalculateHandinStatuses(distributedGroups, false);
        }

        if (success) {
            success &= makeNewRubrics(distributedGroups, false);
        }

        if (success) {
            _makeNewRubricsButton.setEnabled(true);
            _recalculateHandinStatusesButton.setEnabled(true);
        }

        return success;
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
     * @throws CakeHatDBIOException
     * @throws IOException
     */
    private boolean generateDistribution() throws ServicesException, SQLException, CakeHatDBIOException, IOException {
        //figure out which DistributableParts are being graded by which TAs
        Map<DistributablePart, Collection<TA>> graderMap = new HashMap<DistributablePart, Collection<TA>>();
        for (TA grader : _gradingTAs) {
            DistributablePart part = _graderPanels.get(grader).getPartToGrade();

            if (!graderMap.containsKey(part)) {
                graderMap.put(part, new ArrayList<TA>());
            }
            graderMap.get(part).add(grader);
        }

        //check to make sure that there are graders to distribute to for each distributable part
        Collection<DistributablePart> partsWithoutGraders = new LinkedList<DistributablePart>();
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            if (!graderMap.containsKey(dp)) {
                partsWithoutGraders.add(dp);
            }
        }

        if (!partsWithoutGraders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The following distributable parts do not have " +
                                                "TAs assigned to grade them: " + partsWithoutGraders + ".  " +
                                                "Students cannot be distributed.",
                                                "Distribution Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        List<String> handinNames = _asgn.getHandin().getHandinNames();
        handinNames.removeAll(_remainingBadLogins);
        Map<String, Group> groups = Allocator.getGradingServices().getGroupsForHandins(_asgn, _remainingBadLogins);

        //construct a Map that will be used to store the result of distribution
        //for each DistributablePart
        Map<DistributablePart, DistributionResponse> distribution = new HashMap<DistributablePart, DistributionResponse>();

        String message = "Not all students were successfully distributed for all distributable parts.\n " +
                         "For each distributable part, the result of distribution is shown below.  Please\n" +
                         "choose whether to use the incomplete distribution (in which case the undistributed\n" +
                         "students must be distributed manually) or to discard the distribution.\n\n";
        boolean someStudentsUndistributed = false;
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            DistributionResponse resp = generateDistForPart(graderMap.get(dp), handinNames, groups);

            message += "Distributable Part " + dp + ": ";
            if (resp.getProblemStudents().isEmpty()) {
                message += "all students were distributed successfully\n";
            }
            else {
                message += "the following students could not be distributed without\n" +
                           "violating a TA's blacklist: " + resp.getProblemStudents() + "\n";
                someStudentsUndistributed = true;
            }

            distribution.put(dp, resp);
        }

        if (someStudentsUndistributed) {
            Object[] options = {"Use Distribution", "Discard Distribution"};
            int proceed = JOptionPane.showOptionDialog(this, message,
                                                      "Use distribution?",
                                                      JOptionPane.YES_NO_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null, options, options[0]);
            if (proceed != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        Map<DistributablePart, Map<TA, Collection<Group>>> distForDB = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            distForDB.put(dp, distribution.get(dp).getDistribution());
        }

        Allocator.getDataServices().setDistribution(distForDB);

        return true;
    }

    private DistributionResponse generateDistForPart(Collection<TA> graders,
                                                     Collection<String> handinNames,
                                                     Map<String, Group> groups) throws ServicesException, SQLException {
        //get TA blacklists
        Map<TA, Collection<Student>> taBlacklists = new HashMap<TA, Collection<Student>>();
        for (TA grader : graders) {
            Collection<Student> taBlacklist = Allocator.getDataServices().getBlacklist(grader);
            taBlacklists.put(grader, taBlacklist);
        }

        //figure out how many students each TA should grade
        Map<TA, Integer> numStudsNeeded = this.calculateNumberOfHandinsPerTA(graders, handinNames.size());

        //find groups for whom some member is one one of the grading TAs' blacklist
        Collection<Group> blacklistedGroups = new LinkedList<Group>();
        for (String handinName : handinNames) {
            Group group = groups.get(handinName);

            if (Allocator.getGradingServices().isSomeGroupMemberBlacklisted(group, taBlacklists)) {
                blacklistedGroups.add(group);
            }
        }

        //assign blacklisted students first
        DistributionResponse blacklistedResponse = this.assignBlacklistedGroups(blacklistedGroups,
                                                                                numStudsNeeded,
                                                                                taBlacklists);

        List<Group> remainingGroups = new LinkedList<Group>(groups.values());
        remainingGroups.removeAll(blacklistedGroups);

        //then assign all other students
        Map<TA, Collection<Group>> remainingDist = this.assignRemainingGroups(remainingGroups, numStudsNeeded);

        //overallDist represents distribution of both Groups that have members blacklisted by
        //some TA and Groups in which no member is blacklisted
        Map<TA, Collection<Group>> overallDist = new HashMap<TA, Collection<Group>>(blacklistedResponse.getDistribution());
        for (TA ta : remainingDist.keySet()) {
            if (!overallDist.containsKey(ta)) {
                overallDist.put(ta, new ArrayList<Group>());
            }
            overallDist.get(ta).addAll(remainingDist.get(ta));
        }

        //students who could not be distributed without violating some TA's blacklisted
        Collection<Group> overallUndistributed = new ArrayList<Group>(blacklistedResponse.getProblemStudents());

        return new DistributionResponse(overallDist, overallUndistributed);
    }

    /**
     * calculate how many handins each TA should get based on the modifiers entered
     *
     * @param graders
     * @param numGroups
     * @return - map between TA and number to grade
     */
    private Map<TA, Integer> calculateNumberOfHandinsPerTA(Collection<TA> graders, int numGroups) {

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

        Map<TA, Collection<Group>> distribution = new HashMap<TA, Collection<Group>>();
        for (TA grader : graders) {
            distribution.put(grader, new LinkedList<Group>());
        }

        Collection<Group> unDistributed = new LinkedList<Group>();

        for (Group blGroup : groups) {
            Collections.shuffle(graders);
            boolean distributed = false;
            
            for (TA ta : graders) {
                //if ta's blacklist does not contain students from the handin group and ta's dist is not full
                if (!Allocator.getGeneralUtilities().containsAny(graderBlacklists.get(ta), blGroup.getMembers())
                        && numStudsNeeded.get(ta) > 0) {

                    distribution.get(ta).add(blGroup); //add student to ta's dist
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
            distribution.put(grader, new LinkedList<Group>());
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

    /**
     * For each Group in the given Collection, calculates and stores in the database
     * the Group's handin status.  Returns true if handin status storage was completed successfully, and
     * false otherwise.  A return value of false does not mean that an error occurred; rather, it
     * can indicate that the user cancelled an operation.
     *
     * @param groups
     * @param requireConfirmation
     * @return
     * @throws ServicesException
     * @throws SQLException
     * @throws CakeHatDBIOException
     */
    private boolean recalculateHandinStatuses(Collection<Group> groups, boolean requireConfirmation) throws ServicesException, SQLException, CakeHatDBIOException {
        if (requireConfirmation) {
            if (Allocator.getDataServices().areHandinStatusesSet(_asgn.getHandin())) {
                int proceed = JOptionPane.showConfirmDialog(this, "<html>All existing handin statuses will be overwritten.<br/>" +
                                                                  "Are you sure you wish to continue?</html>",
                                                            "Continue?",
                                                            JOptionPane.YES_NO_OPTION);
                if (proceed != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        }

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.ACCESSORIES_TEXT_EDITOR);
        String input = (String) JOptionPane.showInputDialog(this, "Enter minutes of leniency:",
                "Set Grace Period", JOptionPane.PLAIN_MESSAGE, icon, null, "");

        //return value will be null if cancel is clicked; should halt setup
        if (input == null) {
            return false;
        }

        int minsLeniency = Allocator.getConfigurationInfo().getMinutesOfLeniency();
        if (!input.isEmpty()) {
            try {
                minsLeniency = Integer.parseInt(input);

                if (minsLeniency < 0) {
                    throw new NumberFormatException ("Minutes of leniency must be positive.");
                }
            } catch (NumberFormatException e) {
                int shouldContinue = JOptionPane.showConfirmDialog(this, "Invalid minutes of leniency." +
                        "  The course default will be used.",
                        "Invalid Entry", JOptionPane.WARNING_MESSAGE);

                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return false;
                }
            }
        }
        
        Allocator.getGradingServices().storeHandinStatuses(_asgn.getHandin(), groups, minsLeniency, true);
        return true;
    }

    /**
     * Creates a rubric for each Group in the given Collection for each DistributablePart
     * of the selected Assignment. Returns true if rubric creation was completed successfully, and
     * false otherwise.  A return value of false does not mean that an error occurred; rather, it
     * can indicate that the user canceled an operation.
     * 
     * @param groups
     * @param requireConfirmation
     * @return
     * @throws RubricException
     */
    private boolean makeNewRubrics(final Collection<Group> groups, boolean requireConfirmation) throws RubricException {
        if (requireConfirmation) {
            if (Allocator.getRubricManager().areRubricsDistributed(_asgn.getHandin())) {
                int proceed = JOptionPane.showConfirmDialog(this, "<html>All existing rubrics will be overwritten.<br/>" +
                                                                  "Are you sure you wish to continue?</html>",
                                                            "Continue?",
                                                            JOptionPane.YES_NO_OPTION);
                if (proceed != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        }

        Thread distributionThread = new Thread() {
            @Override
            public void run() {
                try {
                    Allocator.getRubricManager().distributeRubrics(_asgn.getHandin(),
                                                                   groups,
                                                                   AutomaticDistributorView.this,
                                                                   OverwriteMode.REPLACE_EXISTING);
                    
                    _progressDialog.dispose();
                } catch (RubricException ex) {
                    new ErrorView(ex, "Distributing rubrics for asignment " + _asgn + " failed.");
                } finally {
                    //get rid of progress dialog
                    _progressDialog.dispose();
                }
            }
        };

        distributionThread.start();

        _progressDialog.pack();
        _progressDialog.setLocationRelativeTo(this);
        _progressDialog.setVisible(true);

        return true;
    }

    public void updatePercentDone(int newPercentDone) {
        _progressBar.setValue(newPercentDone);
    }

    private class GraderPanel extends JPanel {

        private TA _grader;
        private IntegerField _numDiffField;
        private GenericJComboBox<DistributablePart> _partBox;

        public GraderPanel(TA grader) {
            _grader = grader;

            this.setLayout(new GridLayout(1, 0));
            this.setPreferredSize(new Dimension(GRADER_PANEL_WIDTH, GRADER_PANEL_HEIGHT));

            JLabel loginLabel = new JLabel(_grader.getLogin());
            loginLabel.setVerticalTextPosition(JLabel.CENTER);

            JPanel loginPanel = new JPanel(new BorderLayout(0, 0));
            loginPanel.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
            loginPanel.add(loginLabel, BorderLayout.CENTER);
            loginPanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

            _numDiffField = new IntegerField(0);

            Collection<DistributablePart> parts = AutomaticDistributorView.this._asgn.getDistributableParts();
            _partBox = new GenericJComboBox<DistributablePart>(parts);

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

        public DistributablePart getPartToGrade() {
            return _partBox.getSelectedItem();
        }

    }

    private class DistributionResponse {

        private Map<TA, Collection<Group>> _distribution;
        private Collection<Group> _problemGroups;

        public DistributionResponse(Map<TA, Collection<Group>> distribution,
                                    Collection<Group> problemGroup) {
            _distribution = distribution;
            _problemGroups = problemGroup;
        }

        public Map<TA, Collection<Group>> getDistribution() {
            return _distribution;
        }

        public Collection<Group> getProblemStudents() {
            return _problemGroups;
        }

    }

    public static void main(String[] argv) throws CakehatException {
        CakehatMain.initializeForTesting();

        AutomaticDistributorView view = new AutomaticDistributorView(Allocator.getConfigurationInfo().getHandinAssignments().get(0));
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }
}