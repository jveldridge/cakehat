package utils;

import config.Assignment;
import config.HandinPart;
import config.LabPart;
import config.TA;
import gradesystem.GradeSystemApp;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import utils.printing.PrintRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

/**
 * This class provides grading specific utility functions.
 *
 * @author jeldridg
 * @author jak2
 */
public class GradingUtilities {

    /**
     * Import grades for a lab part into the database.
     *
     * @param part
     */
    public void importLabGrades(LabPart part) {
        //Get logins
        Collection<String> logins = Allocator.getDatabaseIO().getAllStudents().keySet();
        //Get scores
        Map<String, Double> scores = part.getLabScores();

        //We don't want to just input all the keys in scores, because if people
        //were checked off with the wrong login we would submit that to the database

        boolean asgnExists = Allocator.getDatabaseIO().assignmentExists(part.getAssignment());
        if (asgnExists) {
            //Input scores for those logins
            for(String login : logins){
                if(scores.containsKey(login)){
                    Allocator.getDatabaseIO().enterGrade(login, part, scores.get(login));
                }
            }
        }
        else {
            new ErrorView(new Exception(), "The assignment: "
                    + part.getAssignment().getName() +
                    " does not exist in the Database. Therefore we cannot add grades for that assignment.");
        }
    }

    /**
     * Makes the user's grading directory as specified by {@link #getUserGradingDirectory()}.
     *
     * @return success of making directory
     */
    public boolean makeUserGradingDirectory()
    {
        return Allocator.getGeneralUtilities().makeDirectory(this.getUserGradingDirectory());
    }

    /**
     * Removes the user's grading directory as specified by {@link #getUserGradingDirectory()}.
     *
     * @return success of removing directory
     */
    public boolean removeUserGradingDirectory()
    {
        return Allocator.getGeneralUtilities().removeDirectory(this.getUserGradingDirectory());
    }

    /**
     * Gets the path to the temporary directory that the user uses while running
     * cakehat.
     * <br><br>
     * Path is: /course/<course>/.cakehat/.<talogin>/
     * <br><br>
     * This directory <b>should</b> be deleted when cakehat is closed.
     *
     * @return path to a TA's temporary grading directory
     */
    public String getUserGradingDirectory()
    {
        return Allocator.getCourseInfo().getGradingDir() + "." + Allocator.getUserUtilities().getUserLogin() + "/";
    }

    /**
     * The absolute path to a student's GRD file for a given handin part.
     *
     * @param part
     * @param studentLogin
     * @return
     */
    public String getStudentGRDPath(HandinPart part, String studentLogin)
    {
        return this.getUserGradingDirectory() + part.getAssignment().getName() + "/" + studentLogin + ".txt";
    }

    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settings:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      grades TA & grades HTA
     *  BCC:     students the user TA is assigned to grade for this assignment, as selected
     *  SUBJECT: "[<course code>] <project> Graded"
     *  MESSAGE: "<project> has been graded and is available for pickup in the handback bin."
     *
     * @param project
     * @param students
     */
    public void notifyStudents(HandinPart part, Vector<String> students, boolean emailRubrics) {
        
        Map<String,String> attachments = null;
        if (emailRubrics) {
            attachments = new HashMap<String,String>();
            for (String student : students) {
                attachments.put(student, Allocator.getGradingUtilities().getStudentGRDPath(part, student));
            }
        }
        
        for (int i = 0; i < students.size(); i++) {
            students.setElementAt(students.get(i)+"@"+Allocator.getCourseInfo().getEmailDomain(), i);  //login -> email
        }

        new EmailView(students, Allocator.getCourseInfo().getNotifyAddresses(), 
                      "[" + Allocator.getCourseInfo().getCourse() + "] " + part.getAssignment().getName() + " Graded",
                      part.getAssignment().getName() + " has been graded.", attachments);
                
    }

    /**
     * Prints .GRD files for each student the user TA has graded
     * Calls getPrinter(...) and then prints using lpr
     *
     * @param assignment assignment for which .GRD files should be printed
     */
    public void printGRDFiles(HandinPart part, Iterable<String> studentLogins) {
        String printer = this.getPrinter("Select printer to print .GRD files");

        if (printer == null) {
            return;
        }

        String taLogin = Allocator.getUserUtilities().getUserLogin();
        Vector<PrintRequest> requests = new Vector<PrintRequest>();


        for(String studentLogin : studentLogins){
           String filePath = this.getStudentGRDPath(part, studentLogin);
           File file = new File(filePath);
            try{
                requests.add(new PrintRequest(file, taLogin, studentLogin));
            }
            catch (FileNotFoundException ex) {
                new ErrorView(ex);
            }
        }

        Allocator.getPortraitPrinter().print(requests, printer);
    }

    /**
     * Prints GRD files for the handin parts and student logins specified. The
     * GRD files must already exist in order for this to work.
     * 
     * @param toPrint
     */
    public void printGRDFiles(Map<HandinPart, Iterable<String>> toPrint)
    {
        String printer = this.getPrinter("Select printer to print .GRD files");

        if (printer == null) {
            return;
        }

        String taLogin = Allocator.getUserUtilities().getUserLogin();
        Vector<PrintRequest> requests = new Vector<PrintRequest>();

        for(HandinPart part : toPrint.keySet())
        {
            for(String studentLogin : toPrint.get(part))
            {
                String filePath = this.getStudentGRDPath(part, studentLogin);
                File file = new File(filePath);
                try
                {
                    requests.add(new PrintRequest(file, taLogin, studentLogin));
                }
                catch (FileNotFoundException ex)
                {
                    new ErrorView(ex);
                }
            }
        }

        Allocator.getPortraitPrinter().print(requests, printer);
    }

    /**
     * Prompts the user to a select a printer.
     *
     * @return printer selected
     */
    public String getPrinter() {
        return this.getPrinter("Please select a printer.");
    }

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return the name of the printer selected
     */
    public String getPrinter(String message) {

        Object[] printerChoices = null;
        Object[] printerChoices_testing = {"bw1", "bw2", "bw3", "bw4", "bw5"};
        Object[] printerChoices_main = {"bw3", "bw4", "bw5"};

        if (GradeSystemApp.inTestMode()) { // select printer choices based on testing mode
            printerChoices = printerChoices_testing;
        }
        else {
            printerChoices = printerChoices_main;
        }

        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png");

        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }

    /**
     * Returns whether or not some member of the given student's group for the given
     * project is on the given TA's blacklist.
     *
     * @param studentLogin
     * @param ta
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean groupMemberOnTAsBlacklist(String studentLogin, HandinPart part, TA ta) {
        Collection<String> blackList = Allocator.getDatabaseIO().getTABlacklist(ta.getLogin());
        Collection<String> group = Allocator.getDatabaseIO().getGroup(part, studentLogin);
            if (Allocator.getGeneralUtilities().containsAny(blackList, group)) {
                return true;
            }
        return false;
    }

    /**
     * present the user with a dialog warning them that some of the handins are for students
     * who are not in the database or who are not enabled. allow the user to choose a method
     * for resolution. either add/enable them or ignore the handin.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Collection<String> resolveMissingStudents(Assignment asgn) {
        Collection<String> handinLogins = asgn.getHandinPart().getHandinLogins();
        Collection<String> allStudents = Allocator.getDatabaseIO().getAllStudents().keySet();
        Collection<String> enabledStudents = Allocator.getDatabaseIO().getEnabledStudents().keySet();

        Set<String> handinsNotInDB = new HashSet<String>();
        Set<String> handinsDisabled = new HashSet<String>();

        for (String handinLogin : handinLogins) {
            if (!allStudents.contains(handinLogin)) {
                handinsNotInDB.add(handinLogin);
            }
            if (!enabledStudents.contains(handinLogin) && allStudents.contains(handinLogin)) {
                handinsDisabled.add(handinLogin);
            }
        }

        // if there are no issues then return an list of no logins
        if (handinsNotInDB.isEmpty() && handinsDisabled.isEmpty()) {
            return new ArrayList();
        }

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel explainationText = new JLabel("<html><p>The following students are"
                + " <font color=red>not</font> in the database or are disabled and they"
                + " have handins for: <font color=blue><i>" + asgn.getName() + "</i></font>."
                + " You should consider adding them to the database or enabling them."
                + " If you do not their handins will <font color=red>not</font> be distributed to"
                + " a TA for grading.</p></html>");
        explainationText.setPreferredSize(new Dimension(175, 100));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        warningPanel.add(explainationText, c);

        final Collection<IssueResolutionPanel> notInDBPanels = new ArrayList<IssueResolutionPanel>();

        if (!handinsNotInDB.isEmpty()) {
            JPanel notInDBChoicePanel = new JPanel();
            notInDBChoicePanel.setLayout(new GridLayout(0, 1));

            c.gridy = 1;
            c.insets = new Insets(20, 0, 0, 0);
            warningPanel.add(new JLabel("<html><u>Select which students to add to the database:</u></html>"), c);

            for (String handinNotInDB : handinsNotInDB) {
                IssueResolutionPanel IRPanel = new IssueResolutionPanel(handinNotInDB, "Add");
                notInDBChoicePanel.add(IRPanel);
                notInDBPanels.add(IRPanel);
            }

            int scrollHeight = notInDBChoicePanel.getPreferredSize().height > 100 ? 100 : notInDBChoicePanel.getPreferredSize().height;

            JScrollPane notInDBScrollPane = new JScrollPane(notInDBChoicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            notInDBScrollPane.setPreferredSize(new Dimension(notInDBChoicePanel.getPreferredSize().width, scrollHeight));
            notInDBScrollPane.setBorder(BorderFactory.createEmptyBorder());
            c.gridy = 2;
            c.insets = new Insets(0, 0, 0, 0);
            warningPanel.add(notInDBScrollPane, c);
        }

        final Collection<IssueResolutionPanel> disabledPanels = new ArrayList<IssueResolutionPanel>();

        if (!handinsDisabled.isEmpty()) {
            JPanel disabledChoicePanel = new JPanel();
            disabledChoicePanel.setLayout(new GridLayout(0, 1));

            c.gridy = 3;
            c.insets = new Insets(20, 0, 0, 0);
            warningPanel.add(new JLabel("<html><u>Select which students to enable in the database:</u></html>"), c);

            for (String handinDisabled : handinsDisabled) {
                IssueResolutionPanel DPanel = new IssueResolutionPanel(handinDisabled, "Enable");
                disabledChoicePanel.add(DPanel);
                disabledPanels.add(DPanel);
            }

            int scrollHeight = disabledChoicePanel.getPreferredSize().height > 100 ? 100 : disabledChoicePanel.getPreferredSize().height;

            JScrollPane disabledScrollPane = new JScrollPane(disabledChoicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            disabledScrollPane.setPreferredSize(new Dimension(disabledChoicePanel.getPreferredSize().width, scrollHeight));
            disabledScrollPane.setBorder(BorderFactory.createEmptyBorder());
            c.gridy = 4;
            c.insets = new Insets(0, 0, 0, 0);
            warningPanel.add(disabledScrollPane, c);
        }

        JPanel allButtonsPanel = new JPanel();
        allButtonsPanel.setLayout(new FlowLayout());

        JButton changeAllButton = new JButton("Change All");
        changeAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (IssueResolutionPanel notInDBPanel : notInDBPanels) {
                    notInDBPanel.setAction2Change();
                }
                for (IssueResolutionPanel disabledPanel : disabledPanels) {
                    disabledPanel.setAction2Change();
                }
            }
        });
        allButtonsPanel.add(changeAllButton);
        
        JButton ignoreAllButton = new JButton("Ignore All");
        ignoreAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (IssueResolutionPanel notInDBPanel : notInDBPanels) {
                    notInDBPanel.setAction2Ignore();
                }
                for (IssueResolutionPanel disabledPanel : disabledPanels) {
                    disabledPanel.setAction2Ignore();
                }
            }
        });
        allButtonsPanel.add(ignoreAllButton);

        c.gridwidth = 1;
        c.insets = new Insets(20, 0, 25, 0);
        c.gridx = 1;
        c.gridy = 5;
        warningPanel.add(allButtonsPanel, c);

        Object[] options = {"Proceed", "Cancel"};
        int doProceed = JOptionPane.showOptionDialog(null, warningPanel, "Resolve Handin Issues", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if (doProceed == JOptionPane.YES_OPTION) {
            for (IssueResolutionPanel notInDBPanel : notInDBPanels) {
                if (notInDBPanel.isChangeSelected()) {
                    String studentLogin = notInDBPanel.getStudentLogin();
                    String studentName = Allocator.getUserUtilities().getUserName(studentLogin);
                    String[] studentSplitName = studentName.split(" ");
                    Allocator.getDatabaseIO().addStudent(studentLogin, studentSplitName[0], studentSplitName[studentSplitName.length - 1]);
                    handinsNotInDB.remove(studentLogin);
                }
            }

            for (IssueResolutionPanel disabledPanel : disabledPanels) {
                if (disabledPanel.isChangeSelected()) {
                    String studentLogin = disabledPanel.getStudentLogin();
                    Allocator.getDatabaseIO().enableStudent(studentLogin);
                    handinsDisabled.remove(studentLogin);
                }
            }

            //create a list of the remaining badlogins
            Collection badLogins = new ArrayList();
            badLogins.addAll(handinsNotInDB);
            badLogins.addAll(handinsDisabled);
            return badLogins;
        }
        return null;
    }

    /**
     * panel containing student login, resolution radio buttons, and radio button group.
     * used in the warning dialog for issue handins
     * @author aunger
     */
    private class IssueResolutionPanel extends JPanel {

        private JRadioButton _changeButton;
        private JRadioButton _ignoreButton;
        private String _studentLogin;

        /**
         * constructor
         * @param studentLogin
         * @param changeText - text for the button that is not ignore (one the is an action)
         */
        public IssueResolutionPanel(String studentLogin, String changeText) {
            super();

            _studentLogin = studentLogin;

            this.setLayout(new GridLayout(1, 3));

            JLabel loginLabel = new JLabel(_studentLogin);
            this.add(loginLabel);

            ButtonGroup actionGroup = new ButtonGroup();

            _changeButton = new JRadioButton(changeText, true);
            _ignoreButton = new JRadioButton("Ignore", false);

            actionGroup.add(_changeButton);
            actionGroup.add(_ignoreButton);

            this.add(_changeButton);
            this.add(_ignoreButton);
        }

        public boolean isChangeSelected() {
            return _changeButton.isSelected();
        }

        public void setAction2Change() {
            _changeButton.setSelected(true);
            _ignoreButton.setSelected(false);
        }

        public void setAction2Ignore() {
            _changeButton.setSelected(false);
            _ignoreButton.setSelected(true);
        }

        public String getStudentLogin() {
            return _studentLogin;
        }
    }

    /**
     * updates the touched file that goes represents the student's lab grade
     *
     * @param labPart
     * @param score
     * @param student
     * @author aunger
     */
    public void updateLabGradeFile(LabPart labPart, double score, String student) {
        BashConsole.write(String.format("rm %s/%d/%s* -f",
                Allocator.getCourseInfo().getLabsDir(), labPart.getLabNumber(), student));

        String scoreText = new Double(score).toString();

        char[] scoreChars = scoreText.toCharArray();
        int endIndex = scoreText.length() - 1;

        for (; endIndex >= 0 ; endIndex--) {
            if (scoreChars[endIndex] != '0') {
                break;
            }
        }

        scoreText = scoreText.substring(0, endIndex+1);

        BashConsole.write(String.format("touch %s/%d/%s,%s",
                Allocator.getCourseInfo().getLabsDir(), labPart.getLabNumber(), student, scoreText));
        BashConsole.write(String.format("chmod 770 %s/%d/%s,%s",
                Allocator.getCourseInfo().getLabsDir(), labPart.getLabNumber(), student, scoreText));
        BashConsole.write(String.format("chgrp %sta %s/%d/%s,%s",
                Allocator.getCourseInfo().getCourse(), Allocator.getCourseInfo().getLabsDir(), labPart.getLabNumber(), student, scoreText));
    }
}
