package gradesystem.services;

import gradesystem.views.shared.EmailView;
import gradesystem.views.shared.ErrorView;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.LabPart;
import gradesystem.GradeSystemApp;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import gradesystem.printing.PrintRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import gradesystem.Allocator;
import gradesystem.config.TA;
import utils.system.NativeException;

public class GradingServicesImpl implements GradingServices
{
    public void importLabGrades(LabPart part)
    {
        //Get logins
        Collection<String> logins = Allocator.getDatabaseIO().getAllStudents().keySet();
        //Get scores
        Map<String, Double> scores = part.getLabScores();

        //We don't want to just input all the keys in scores, because if people
        //were checked off with the wrong login we would submit that to the database

        boolean asgnExists = Allocator.getDatabaseIO().assignmentExists(part.getAssignment());
        if (asgnExists)
        {
            //Input scores for those logins
            for(String login : logins)
            {
                if(scores.containsKey(login))
                {
                    Allocator.getDatabaseIO().enterGrade(login, part, scores.get(login));
                }
            }
        }
        else
        {
            new ErrorView(new Exception(), "The assignment: "
                    + part.getAssignment().getName() +
                    " does not exist in the Database. Therefore we cannot add grades for that assignment.");
        }
    }

    public void makeUserGradingDirectory()
    {
        File gradingDir = new File(this.getUserGradingDirectory());
        try
        {
            Allocator.getFileSystemServices().makeDirectory(gradingDir);
        }
        catch(NativeException e)
        {
            new ErrorView(e, "Unable to create grading directory: " + gradingDir.getAbsolutePath());
        }
    }

    public boolean removeUserGradingDirectory()
    {
        return Allocator.getFileSystemUtilities().removeDirectory(this.getUserGradingDirectory());
    }

    public String getUserGradingDirectory()
    {
        String dirPath = Allocator.getCourseInfo().getGradingDir()
                          +"."
                          + Allocator.getUserUtilities().getUserLogin();

        if (GradeSystemApp.isBackend())
        {
            dirPath += "-admin";
        }
        else if (!GradeSystemApp.isFrontend())
        {
            //should always be in frontend or backend; if not, show an error
            //message (but don't throw error, as that seems that it would cause
            //problems without being helpful)
            new ErrorView(String.format("UNEXPECTED STATE:\n" +
                    "User directory requested before launch of Frontend or Backend.  " +
                    "The directory \"%s\" will be used for the current session.", dirPath));
            new Exception().printStackTrace();
        }

        return dirPath + "/";
    }

    public String getStudentGRDPath(HandinPart part, String studentLogin)
    {
        return this.getUserGradingDirectory() + part.getAssignment().getName() + "/" + studentLogin + ".txt";
    }

    public void notifyStudents(HandinPart part, Vector<String> students, boolean emailRubrics)
    {

        Map<String,String> attachments = null;
        if (emailRubrics)
        {
            attachments = new HashMap<String,String>();
            for (String student : students)
            {
                attachments.put(student, Allocator.getGradingServices().getStudentGRDPath(part, student));
            }
        }

        for (int i = 0; i < students.size(); i++)
        {
            students.setElementAt(students.get(i)+"@"+Allocator.getCourseInfo().getEmailDomain(), i);  //login -> email
        }

        new EmailView(students, Allocator.getCourseInfo().getNotifyAddresses(),
                      "[" + Allocator.getCourseInfo().getCourse() + "] " + part.getAssignment().getName() + " Graded",
                      part.getAssignment().getName() + " has been graded.", attachments);

    }

    public void printGRDFiles(HandinPart part, Iterable<String> studentLogins)
    {
        String printer = this.getPrinter("Select printer to print .GRD files");

        if (printer == null)
        {
            return;
        }

        String taLogin = Allocator.getUserUtilities().getUserLogin();
        Vector<PrintRequest> requests = new Vector<PrintRequest>();


        for(String studentLogin : studentLogins)
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

        try
        {
            Allocator.getPortraitPrinter().print(requests, printer);
        }
        catch(IOException e)
        {
          String loginsString = "";
            for(String login : studentLogins)
            {
                loginsString += login + " ";
            }
            new ErrorView(e, "Unable to issue print command for " + part.getAssignment().getName() + ".\n" +
                    "For the following students: " + loginsString);
        }
    }

    public void printGRDFiles(Map<HandinPart, Iterable<String>> toPrint)
    {
        String printer = this.getPrinter("Select printer to print .GRD files");

        if (printer == null)
        {
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

        try
        {
            Allocator.getPortraitPrinter().print(requests, printer);
        }
        catch(IOException e)
        {
            new ErrorView(e, "Unable to issue print command for GRD files.");
        }
    }

    public String getPrinter()
    {
        return this.getPrinter("Please select a printer.");
    }

    public String getPrinter(String message)
    {
        Object[] printerChoices = null;
        Object[] printerChoices_testing = {"bw1", "bw2", "bw3", "bw4", "bw5"};
        Object[] printerChoices_main = {"bw3", "bw4", "bw5"};

        // select printer choices based on testing mode
        if (GradeSystemApp.inTestMode())
        {
            printerChoices = printerChoices_testing;
        }
        else
        {
            printerChoices = printerChoices_main;
        }

        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png");

        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }

    public boolean isOkToDistribute(Assignment asgn, String student, TA ta) {
        if (groupMemberOnTAsBlacklist(student, asgn.getHandinPart(), ta)) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + student + " is on TA "
                                                    + ta.getLogin() + "'s blacklist.  Continue?",
                                                    "Distribute Blacklisted Student?",
                                                    JOptionPane.YES_NO_OPTION);
            return (shouldContinue == JOptionPane.YES_OPTION);
        }

        return true;
    }

    public boolean groupMemberOnTAsBlacklist(String studentLogin, HandinPart part, TA ta)
    {
        Collection<String> blackList = Allocator.getDatabaseIO().getTABlacklist(ta);
        Collection<String> group = Allocator.getDatabaseIO().getGroup(part, studentLogin);
        if (Allocator.getGeneralUtilities().containsAny(blackList, group))
        {
            return true;
        }
        
        return false;
    }

    public Collection<String> resolveMissingStudents(Assignment asgn)
    {
        Collection<String> handinLogins = asgn.getHandinPart().getHandinLogins();
        Collection<String> allStudents = Allocator.getDatabaseIO().getAllStudents().keySet();
        Collection<String> enabledStudents = Allocator.getDatabaseIO().getEnabledStudents().keySet();

        Set<String> handinsNotInDB = new HashSet<String>();
        Set<String> handinsDisabled = new HashSet<String>();

        for (String handinLogin : handinLogins)
        {
            if (!allStudents.contains(handinLogin))
            {
                handinsNotInDB.add(handinLogin);
            }
            if (!enabledStudents.contains(handinLogin) && allStudents.contains(handinLogin))
            {
                handinsDisabled.add(handinLogin);
            }
        }

        // if there are no issues then return an list of no logins
        if (handinsNotInDB.isEmpty() && handinsDisabled.isEmpty())
        {
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

        if (!handinsNotInDB.isEmpty())
        {
            JPanel notInDBChoicePanel = new JPanel();
            notInDBChoicePanel.setLayout(new GridLayout(0, 1));

            c.gridy = 1;
            c.insets = new Insets(20, 0, 0, 0);
            warningPanel.add(new JLabel("<html><u>Select which students to add to the database:</u></html>"), c);

            for (String handinNotInDB : handinsNotInDB)
            {
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

        if (!handinsDisabled.isEmpty())
        {
            JPanel disabledChoicePanel = new JPanel();
            disabledChoicePanel.setLayout(new GridLayout(0, 1));

            c.gridy = 3;
            c.insets = new Insets(20, 0, 0, 0);
            warningPanel.add(new JLabel("<html><u>Select which students to enable in the database:</u></html>"), c);

            for (String handinDisabled : handinsDisabled)
            {
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
        changeAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (IssueResolutionPanel notInDBPanel : notInDBPanels)
                {
                    notInDBPanel.setAction2Change();
                }
                for (IssueResolutionPanel disabledPanel : disabledPanels)
                {
                    disabledPanel.setAction2Change();
                }
            }
        });
        allButtonsPanel.add(changeAllButton);

        JButton ignoreAllButton = new JButton("Ignore All");
        ignoreAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for (IssueResolutionPanel notInDBPanel : notInDBPanels)
                {
                    notInDBPanel.setAction2Ignore();
                }
                for (IssueResolutionPanel disabledPanel : disabledPanels)
                {
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

        if (doProceed == JOptionPane.YES_OPTION)
        {
            for (IssueResolutionPanel notInDBPanel : notInDBPanels)
            {
                if (notInDBPanel.isChangeSelected())
                {
                    String studentLogin = notInDBPanel.getStudentLogin();
                    Allocator.getUserServices().addStudent(studentLogin, UserServices.ValidityCheck.CHECK);
                    handinsNotInDB.remove(studentLogin);
                }
            }

            for (IssueResolutionPanel disabledPanel : disabledPanels)
            {
                if (disabledPanel.isChangeSelected())
                {
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
    private class IssueResolutionPanel extends JPanel
    {
        private JRadioButton _changeButton;
        private JRadioButton _ignoreButton;
        private String _studentLogin;

        /**
         * constructor
         * @param studentLogin
         * @param changeText - text for the button that is not ignore (one the is an action)
         */
        public IssueResolutionPanel(String studentLogin, String changeText)
        {
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

        public boolean isChangeSelected()
        {
            return _changeButton.isSelected();
        }

        public void setAction2Change()
        {
            _changeButton.setSelected(true);
            _ignoreButton.setSelected(false);
        }

        public void setAction2Ignore()
        {
            _changeButton.setSelected(false);
            _ignoreButton.setSelected(true);
        }

        public String getStudentLogin()
        {
            return _studentLogin;
        }
    }

    public void updateLabGradeFile(LabPart labPart, double score, String student)
    {
        //Deletes existing lab grade(s) for this lab and student. There should
        //only be 1, but to be safe, allow for deleting all
        File labDir = new File(Allocator.getCourseInfo().getLabsDir() + labPart.getLabNumber());
        for(File labFile : labDir.listFiles())
        {
            if(labFile.getName().startsWith(student))
            {
                if(!labFile.delete())
                {
                    new ErrorView("Unable to remove previous lab grade: " +
                            labFile.getAbsolutePath() + ". \n" +
                            "The lab grade cannot be updated.");
                    return;
                }
            }
        }

        //Convert score to string and trim (to avoid something like 1.5000000000)
        String scoreText = new Double(score).toString();
        char[] scoreChars = scoreText.toCharArray();
        int endIndex = scoreText.length() - 1;
        for (; endIndex >= 0 ; endIndex--)
        {
            if (scoreChars[endIndex] != '0')
            {
                break;
            }
        }
        scoreText = scoreText.substring(0, endIndex+1);

        //File that will represent the new lab grade
        File scoreFile = new File(labDir, student + "," + scoreText);

        //Create the file
        try
        {
            scoreFile.createNewFile();

            //Change permissions and group ownership
            try
            {
                Allocator.getFileSystemServices().sanitize(scoreFile);
            }
            catch(NativeException e)
            {
                new ErrorView(e, "Unable to change permissions and group for new lab grade.");
            }
        }
        catch (IOException ex)
        {
            new ErrorView(ex, "Previous lab grade was removed, " +
                    "but it was not possible to add the new lab grade: " +
                    scoreFile.getAbsolutePath());
        }
    }
}