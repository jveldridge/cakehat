package gradesystem.views.backend.assignmentdist;

import gradesystem.components.IntegerField;
import gradesystem.config.Assignment;
import gradesystem.config.TA;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.rubric.RubricException;
import gradesystem.services.ServicesException;
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
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import gradesystem.Allocator;
import gradesystem.components.GenericJComboBox;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.views.shared.ErrorView;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;

/**
 * Provides an interface for creating a distribution for an assignment.
 * 
 * @author jeldridg
 */
public class AssignmentDistView extends JFrame implements DistributionRequester {

    private static int GRADER_PANEL_WIDTH = 600;
    private static int GRADER_PANEL_HEIGHT = 30;

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

    public AssignmentDistView(Assignment asgn) {
        _asgn = asgn;
        try {
            _remainingBadLogins = Allocator.getGradingServices().resolveMissingStudents(_asgn);
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

        _gradingTAs = new Vector<TA>(Allocator.getCourseInfo().getDefaultGraders());
        _nonGradingTAs = new Vector<TA>(Allocator.getCourseInfo().getNonDefaultGraders());

        this.setTitle(String.format("Create Distribution for Assignment: %s", _asgn.getName()));

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
        instructionsPanel.add(new JLabel("<html>Enter the number of handins above or " +
                "below the average each TA should grade. " +
                "<br/>(-2 = two less to grade; don't use a + symbol for positive numbers)</html>"));
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

        JPanel buttonPanel = new JPanel();
        
        JButton createDistributionButton = new JButton("1. Distribute Students");
        createDistributionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    generateDistribution();
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "Generating the distribution for assignment " +
                                       _asgn + " failed." );
                } catch (SQLException ex) {
                    new ErrorView(ex, "Generating the distribution for assignment " +
                                       _asgn + " failed." );
                } catch (ServicesException ex) {
                    new ErrorView(ex, "Generating the distribution for assignment " +
                                       _asgn + " failed." );
                }
            }
        });
        buttonPanel.add(createDistributionButton);

        JButton setupGradingButton = new JButton("2. Set Up Grading");
        setupGradingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUpGrading();
            }
        });
        buttonPanel.add(setupGradingButton);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(_graderPanelsPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/accessories-text-editor.png")));
        } catch (Exception e) {}

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
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

    private void generateDistribution() throws ServicesException, SQLException, CakeHatDBIOException {
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
            return;
        }

        try {
            if (!Allocator.getDatabaseIO().isDistEmpty(_asgn)) {
                int n = JOptionPane.showConfirmDialog(this, "A distribution already exists for " + _asgn.getName() +
                                                             ".\nAre you sure you want to overwrite the existing distribution?",
                                                             "Confirm Overwrite",
                                                             JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        } catch (SQLException ex) {
            int n = JOptionPane.showConfirmDialog(this, "Could not determine whether a distribution already exists " +
                                                        "for assignment " + _asgn.getName() + ".\nDo you wish to proceed?  " +
                                                        "Doing so will overwrite any existing distribution.",
                                                        "Proceed?",
                                                        JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.NO_OPTION) {
                    return;
                }
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
                         "students must be distributed manually via the ReassignView) or to discard the distribution.\n\n";
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
            if (proceed == JOptionPane.NO_OPTION) {
                return;
            }
        }

        Map<DistributablePart, Map<TA, Collection<Group>>> distForDB = new HashMap<DistributablePart, Map<TA, Collection<Group>>>();
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            distForDB.put(dp, distribution.get(dp).getDistribution());
        }

        Allocator.getDatabaseIO().setDistributablePartDist(distForDB);

        JOptionPane.showMessageDialog(this, "Success!");

        
    }

    private DistributionResponse generateDistForPart(Collection<TA> graders,
                                                     Collection<String> handinNames,
                                                     Map<String, Group> groups) throws ServicesException, SQLException {
        //get TA blacklists
        Map<TA, Collection<String>> taBlacklists = new HashMap<TA, Collection<String>>();
        for (TA grader : graders) {
            Collection<String> taBlacklist = Allocator.getDatabaseIO().getTABlacklist(grader);
            taBlacklists.put(grader, taBlacklist);
        }

        //figure out how many students each TA should grade
        Map<TA, Integer> numStudsNeeded = this.calculateNumberOfHandinsPerTA(graders, handinNames.size());

        //find groups for whom some member is one one of the grading TAs' blacklist
        Collection<Group> blacklistedGroups = new LinkedList<Group>();
        for (String handinName : handinNames) {
            Group group = groups.get(handinName);

            if (Allocator.getGradingServices().groupMemberOnTAsBlacklist(group, taBlacklists)) {
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
        overallDist.putAll(remainingDist);

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
                                         Map<TA, Collection<String>> graderBlacklists) {
        List<TA> graders = new ArrayList<TA>(numStudsNeeded.keySet());

        Map<TA, Collection<Group>> distribution = new HashMap<TA, Collection<Group>>();
        for (TA grader : graders) {
            distribution.put(grader, new LinkedList<Group>());
        }

        Collection<Group> unDistributed = new LinkedList<Group>();

        Collections.shuffle(graders);
        for (Group blGroup : groups) {
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

    private void setUpGrading() {
        int proceed = JOptionPane.showConfirmDialog(this, "<html>All handin statuses will be recalculated<br/>" +
                                                          "and any existing rubrics will be overwritten.<br/>" +
                                                          "Are you sure you wish to continue?</html>",
                                                    "Continue?",
                                                    JOptionPane.YES_NO_OPTION);
        if (proceed != JOptionPane.YES_OPTION) {
            return;
        }


        //create any necessary rubric directories that do not alreayd exist
        for (DistributablePart dp : _asgn.getDistributableParts()) {
            File directory = new File(Allocator.getCourseInfo().getRubricDir() + _asgn.getName() + "/" + dp.getName() + "/");
            if (!directory.exists()) {
                try {
                    Allocator.getFileSystemServices().makeDirectory(directory);
                } catch (ServicesException e) {
                    new ErrorView(e, "Unable to create rubric directory: " + directory.getAbsolutePath() + "\n"
                            + "Rubrics cannot be distributed.");
                    return;
                }
            }
        }

        ImageIcon icon = new javax.swing.ImageIcon("/gradesystem/resources/icons/32x32/accessories-text-editor.png"); // NOI18N
        String input = (String) JOptionPane.showInputDialog(this, "Enter minutes of leniency:",
                "Set Grace Period", JOptionPane.PLAIN_MESSAGE, icon, null, "");
        
        //return value will be null if cancel is clicked; should halt setup
        if (input == null) {
            return;
        }

        int minsLeniency = Allocator.getCourseInfo().getMinutesOfLeniency();
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
                    return;
                }
            }
        }
        final int minutesOfLeniency = minsLeniency;

        Thread distributionThread = new Thread() {

            @Override
            public void run() {
                try {
                    //figure out which Groups have been distributed for any DistributablePart
                    //and make rubrics for them for all DistributableParts
                    Set<Group> groups = new HashSet<Group>();
                    for (DistributablePart dp : _asgn.getDistributableParts()) {
                        groups.addAll(Allocator.getDatabaseIO().getAllAssignedGroups(dp));
                    }
                    
                    Allocator.getRubricManager().distributeRubrics(_asgn.getHandin(),
                                                                   groups,
                                                                   minutesOfLeniency,
                                                                   AssignmentDistView.this,
                                                                   true);
                    
                    _progressDialog.dispose();
                    JOptionPane.showMessageDialog(AssignmentDistView.this, "Grading setup complete.",
                                                  "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    new ErrorView(ex, "The distribution for assignment " + _asgn + " " +
                                      "could not be read from the database.");
                } catch (CakeHatDBIOException ex) {
                    new ErrorView(ex, "The distribution for assignment " + _asgn + " " +
                                      "could not be read from the database.");
                } catch (RubricException ex) {
                    new ErrorView(ex, "Disributing rubrics for asignment " + _asgn + " failed.");
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
            JPanel loginPanel = new JPanel();
            loginPanel.add(loginLabel);

            _numDiffField = new IntegerField(0);
            _numDiffField.setPreferredSize(new Dimension(100, 25));

            Collection<DistributablePart> parts = AssignmentDistView.this._asgn.getDistributableParts();
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

            JPanel removePanel = new JPanel();
            removePanel.add(removeGraderButton);

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

    public static void main(String[] argv) {
        new AssignmentDistView(Allocator.getCourseInfo().getAssignments().iterator().next());
    }

}
