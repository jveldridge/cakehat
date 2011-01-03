package gradesystem.views.backend.assignmentdist;

import gradesystem.components.IntegerField;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.TA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import gradesystem.Allocator;
import gradesystem.views.shared.ErrorView;
import java.io.File;
import utils.system.NativeException;

/**
 * Provides an interface for creating a distribution for an assignment.
 * 
 * @author jeldridg
 */
public class AssignmentDistView extends JFrame implements DistributionRequester {

    private static int GRADER_PANEL_WIDTH = 200;
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

        _remainingBadLogins = Allocator.getGradingServices().resolveMissingStudents(_asgn);

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
                generateDistribution();
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

    private void generateDistribution() {
        /** setup variables **/

        //check to make sure that there are graders to distribute to
        if (_gradingTAs.size() == 0) {
            JOptionPane.showMessageDialog(this, "There are no grading TAs.  Students cannot be distributed.",
                                          "Distribution Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //check to make sure that there is not a dist already
        if (!Allocator.getDatabaseIO().isDistEmpty(_asgn.getHandinPart())) {
            int n = JOptionPane.showConfirmDialog(new JFrame(), "A distribution already exists for " + _asgn.getName() + ".\nAre you sure you want to overwrite the existing distribution?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            }
        }

        //get handin logins, shuffle logins, add to deque
        ArrayList<String> handinLoginsRaw = new ArrayList<String>(_asgn.getHandinPart().getHandinLogins());
        Collections.shuffle(handinLoginsRaw);
        ArrayDeque<String> handinLogins = new ArrayDeque<String>(handinLoginsRaw);

        //remove disabled students and students that are not in the DB
        handinLogins.removeAll(_remainingBadLogins);

        //get all grader logins
        ArrayList<TA> tas = new ArrayList<TA>();
        for (TA ta : _gradingTAs) {
            tas.add(ta);
        }

        //build distribution hashmap
        Map<TA, Collection<String>> distribution = new HashMap<TA, Collection<String>>();
        for (TA grader : tas) {
            distribution.put(grader, new ArrayList<String>());
        }

        /** make distribution **/

        //determine how many students to give to each TA
        Map<TA, Integer> numStudsNeeded = this.calculateNumberOfHandinsPerTA(handinLogins, tas.size());

        //distribute all the blacklisted handins to TAs first
        boolean distBlackListSuccessful = this.assignBlackListedHandinsToTAs(distribution, numStudsNeeded, handinLogins, _asgn.getHandinPart(), tas);
        if (!distBlackListSuccessful) {
            JOptionPane.showMessageDialog(this, "There was an error "
                        + "distributing blacklisted "
                        + "students. Please try running "
                        + "the distribution again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //distribute the rest if the handins to TAs
        this.assignRemainingHandinsToTAs(distribution, numStudsNeeded, handinLogins, tas);

        //put the distribution into the DB
        Allocator.getDatabaseIO().setAsgnDist(_asgn.getHandinPart(), distribution);

        JOptionPane.showMessageDialog(this, "Assignments have been successfully distributed to the grading TAs.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * calculate how many handins each TA should get based on the modifiers entered
     *
     * @param handinLogins
     * @param numberOfTAs
     * @return - map between TA and number to grade
     */
    private Map<TA, Integer> calculateNumberOfHandinsPerTA(Collection<String> handinLogins, int numberOfTAs) {

        //total number of handins used in calculating the average
        int calculatedTotalStudents = handinLogins.size();

        //update total number of students based modifiers from table (only used for calc of avg)
        for (TA grader : _gradingTAs) {
            int diffFromAvg = _graderPanels.get(grader).getNumDiff();

            //the extra (if TA gets less than average) students need to be go to the other TAs therefore the average and thus the total must be higher
            calculatedTotalStudents -= diffFromAvg;
        }

        //average number of students for each ta
        int avg = (int) Math.floor((double) calculatedTotalStudents / (double) numberOfTAs);

        //build hashmap of how many students each TA must grade
        HashMap<TA, Integer> numStudsNeeded = new HashMap<TA, Integer>();
        for (TA grader : _gradingTAs) {
            numStudsNeeded.put(grader, _graderPanels.get(grader).getNumDiff() + avg);
        }

        return numStudsNeeded;
    }

    /**
     * assign all the blacklisted handins to TAs first so there the most possible TAs to give them to
     *
     * @param distribution
     * @param numStudsNeeded
     * @param handinLogins
     * @return - did this step work?
     */
    private boolean assignBlackListedHandinsToTAs(Map<TA, Collection<String>> distribution,
                        Map<TA, Integer> numStudsNeeded, Collection<String> handinLogins,
                        HandinPart handinPart, ArrayList<TA> tas) {

        //get all the groups for this project (maps student login to students in their group)
        Map<String, Collection<String>> groups = Allocator.getDatabaseIO().getGroups(handinPart);

        //make a list of all blacklisted students and hashmap of all ta blacklists
        Set<String> blacklistedStudents = new HashSet<String>();
        Map<TA, Collection<String>> taBlacklists = new HashMap<TA, Collection<String>>();

        for (TA ta : tas) {
            Collection<String> tasBlackList = Allocator.getDatabaseIO().getTABlacklist(ta);
            blacklistedStudents.addAll(tasBlackList);
            taBlacklists.put(ta, tasBlackList);
        }

        //get all handins to pick which are the blacklisted handins
        Collection<String> blacklistedHandins = new ArrayList();
        blacklistedHandins.addAll(handinLogins);

        //remove handins which aren't blacklisted
        Iterator<String> iterator = blacklistedHandins.iterator();
        while (iterator.hasNext()) {
            if (!Allocator.getGeneralUtilities().containsAny(blacklistedStudents, groups.get(iterator.next()))) {
                iterator.remove();
            }
        }

        //add all blacklisted handins to a TA first
        for (String blStudent : blacklistedHandins) {
            Collections.shuffle(tas);
            boolean distributed = false;
            for (TA ta : tas) {
                //if ta's blacklist does not contain students from the handin group (individuals will have a group of size 1) and ta's dist is not full
                if (!Allocator.getGeneralUtilities().containsAny(taBlacklists.get(ta), groups.get(blStudent))
                        && numStudsNeeded.get(ta) > 0) {

                    distribution.get(ta).add(blStudent); //add student to ta's dist
                    numStudsNeeded.put(ta, numStudsNeeded.get(ta) - 1); //reduce num ta needs
                    distributed = true;
                    break;
                }
            }
            if (!distributed) {
                return false;
            }
        }

        //remove all blacklisted students from student list since they are all distributed already
        handinLogins.removeAll(blacklistedHandins);
        return true;
    }

    /**
     * take the handins which are not on any blacklist and distributes them to TAs at random until the TA is at their limit
     *
     * @param distribution - current distribution
     * @param numStudsNeeded
     * @param handinLogins
     * @return - did this step work?
     */
    private boolean assignRemainingHandinsToTAs(Map<TA, Collection<String>> distribution,
                        Map<TA, Integer> numStudsNeeded, ArrayDeque<String> handinLogins,
                        ArrayList<TA> tas) {

        Collections.shuffle(tas);
        //fill TAs to limit
        for (TA ta : tas) {
            for (int i = 0; i < numStudsNeeded.get(ta); i++) {
                distribution.get(ta).add(handinLogins.removeFirst());
            }
        }

        //distribute remaining students (< # TAs of them) to random TAs
        //There will be < # TAs of them because we floored when we took the average so only at most n-1 could be left.
        Collections.shuffle(tas);
        for (TA ta : tas) {
            if (!handinLogins.isEmpty()) {
                distribution.get(ta).add(handinLogins.removeFirst());
            } else {
                break;
            }
        }
        return true;
    }

    private void setUpGrading() {
        //create rubric directory if it does not exist
        File directory = new File(Allocator.getCourseInfo().getRubricDir() + _asgn.getName() + "/");
        try
        {
            Allocator.getFileSystemServices().makeDirectory(directory);
        }
        catch(NativeException e)
        {
            new ErrorView(e, "Unable to create rubric directory: " + directory.getAbsolutePath());
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

        final int finalMinsLeniency = minsLeniency;

        Thread distributionThread = new Thread() {

            @Override
            public void run() {
                //get dist from DB
                Map<TA, Collection<String>> distribution = Allocator.getDatabaseIO().getDistribution(_asgn.getHandinPart());

                //actually distribute rubrics
                Allocator.getRubricManager().distributeRubrics(_asgn.getHandinPart(),
                        distribution, finalMinsLeniency,
                        AssignmentDistView.this);

                //get rid of progress dialog
                _progressDialog.dispose();
            }
        };

        distributionThread.start();

        _progressDialog.pack();
        _progressDialog.setLocationRelativeTo(this);
        _progressDialog.setVisible(true);

        JOptionPane.showMessageDialog(this, "Grading setup complete.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updatePercentDone(int newPercentDone) {
        _progressBar.setValue(newPercentDone);
    }

    private class GraderPanel extends JPanel {

        private TA _grader;
        private IntegerField _numDiffField;

        public GraderPanel(TA grader) {
            _grader = grader;

            this.setLayout(new GridLayout(1, 0));
            this.setPreferredSize(new Dimension(GRADER_PANEL_WIDTH, GRADER_PANEL_HEIGHT));

            JLabel loginLabel = new JLabel(_grader.getLogin());
            JPanel loginPanel = new JPanel();
            loginPanel.add(loginLabel);

            _numDiffField = new IntegerField(0);
            _numDiffField.setPreferredSize(new Dimension(200, 25));

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
            this.add(removePanel);
        }

        public int getNumDiff() {
            return _numDiffField.getIntValue();
        }

    }

    public static void main(String[] argv) {
        new AssignmentDistView(Allocator.getCourseInfo().getHandinAssignments().toArray(new Assignment[0])[0]);
    }

}
