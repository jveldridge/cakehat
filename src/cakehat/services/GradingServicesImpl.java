package cakehat.services;

import cakehat.views.shared.ErrorView;
import cakehat.config.Assignment;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import cakehat.Allocator;
import cakehat.CakehatMain;
import cakehat.config.LatePolicy;
import cakehat.config.TA;
import cakehat.database.Group;
import cakehat.database.HandinStatus;
import cakehat.config.handin.Handin;
import cakehat.database.DataServices.ValidityCheck;
import cakehat.database.Student;
import cakehat.printing.CITPrinter;
import cakehat.printing.PrintRequest;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.rubric.TimeStatus;
import cakehat.views.shared.EmailView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

public class GradingServicesImpl implements GradingServices
{
    @Override
    public void makeUserWorkspace() throws ServicesException
    {
        File workspace = Allocator.getPathServices().getUserWorkspaceDir();

        //If the workspace already exists, attempt to delete it
        if(workspace.exists())
        {
            try
            {
                Allocator.getFileSystemUtilities().deleteFiles(Arrays.asList(workspace));
            }
            //Do not do anything if this fails, because it will almost certainly
            //be due to NFS (networked file system) issues about which nothing
            //can be done
            catch(IOException e) { }
        }

        //Create the workspace
        try
        {
            Allocator.getFileSystemServices().makeDirectory(workspace);
        }
        catch(ServicesException e)
        {
            throw new ServicesException("Unable to create user's workspace: " +
                    workspace.getAbsolutePath(), e);
        }
        
        //Due to NFS (networked file system) behavior, the workspace might not
        //always be succesfully deleted - there is NOTHING that can be done
        //about this, even 'rm -rf' will fail in these situations
        Allocator.getFileSystemUtilities().deleteFileOnExit(workspace);
    }
    
    @Override
    public void makeDatabaseBackup() throws ServicesException {
        String backupFileName = Allocator.getCourseInfo().getCourse() +
                            "db_bk_" +
                            Allocator.getCalendarUtilities()
                            .getCalendarAsString(Calendar.getInstance())
                            .replaceAll("(\\s|:)", "_");
                    File backupFile = new File(Allocator.getPathServices().getDatabaseBackupDir(),
                            backupFileName);
        try
        {
            Allocator.getFileSystemServices()
                .copy(Allocator.getPathServices().getDatabaseFile(),
                backupFile, OverwriteMode.FAIL_ON_EXISTING,
                false, FileCopyPermissions.READ_WRITE);
        }
        catch(FileCopyingException ex)
        {
            throw new ServicesException("Unable to make database backup.", ex);
        }
    }

    private static final List<CITPrinter> NORMALLY_ALLOWED_PRINTERS =
            Arrays.asList(CITPrinter.bw3, CITPrinter.bw4, CITPrinter.bw5);
    private static final List<CITPrinter> DEVELOPER_ALLOWED_PRINTERS =
            Arrays.asList(CITPrinter.bw1, CITPrinter.bw2, CITPrinter.bw3, CITPrinter.bw4, CITPrinter.bw5);
    private static final CITPrinter DEFAULT_PRINTER = CITPrinter.bw3;

    @Override
    public CITPrinter getPrinter()
    {
        return this.getPrinter("Please select a printer.");
    }

    @Override
    public CITPrinter getDefaultPrinter()
    {
        return CITPrinter.bw3;
    }

    @Override
    public List<CITPrinter> getAllowedPrinters()
    {
        List<CITPrinter> allowed;
        if(CakehatMain.isDeveloperMode())
        {
            allowed = DEVELOPER_ALLOWED_PRINTERS;
        }
        else
        {
            allowed = NORMALLY_ALLOWED_PRINTERS;
        }

        return allowed;
    }

    @Override
    public CITPrinter getPrinter(String message)
    {
        CITPrinter[] printerChoices = getAllowedPrinters().toArray(new CITPrinter[0]);

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.PRINTER);

        return (CITPrinter) JOptionPane.showInputDialog(new JFrame(), message,
                "Select Printer", JOptionPane.PLAIN_MESSAGE, icon,
                printerChoices, DEFAULT_PRINTER);
    }

    @Override
    public boolean isOkToDistribute(Group group, TA ta) throws ServicesException {
        Collection<Student> blacklist = Allocator.getDataServices().getBlacklist(ta);

        if (Allocator.getGeneralUtilities().containsAny(blacklist, group.getMembers())) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + group + " is on TA "
                                                    + ta.getLogin() + "'s blacklist.  Continue?",
                                                    "Distribute Blacklisted Student?",
                                                    JOptionPane.YES_NO_OPTION);
            return (shouldContinue == JOptionPane.YES_OPTION);
        }

        return true;
    }

    @Override
    public boolean isSomeGroupMemberBlacklisted(Group group, Map<TA, Collection<Student>> blacklists) throws ServicesException {
        for (TA ta : blacklists.keySet()) {
            Collection<Student> blackList = blacklists.get(ta);
            if (Allocator.getGeneralUtilities().containsAny(blackList, group.getMembers())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<String> resolveUnexpectedHandins(Assignment asgn) throws ServicesException {
        Collection<String> handinNames;
        try {
            handinNames = asgn.getHandin().getHandinNames();
        } catch (IOException e) {
            throw new ServicesException("Unable to retrieve handin names for " + asgn.getName(), e);
        }

        //group project- check that that the name of each handin is either
        //the name of some group or the login of a member of some group
        if (asgn.hasGroups()) {
            Set<String> validNames = new HashSet<String>();

            Collection<Group> groups = Allocator.getDataServices().getGroups(asgn);
            for (Group group : groups) {
                validNames.add(group.getName());
                validNames.addAll(group.getMemberLogins());
            }

            Collection<String> badHandins = new LinkedList<String>();
            for (String handinName : handinNames) {
                if (!validNames.contains(handinName)) {
                    badHandins.add(handinName);
                }
            }

            if (!badHandins.isEmpty()) {
                String errMsg = "The following handins do not correspond to a group name " +
                                "or a group member's login:\n";
                for (String handin : badHandins) {
                    errMsg += handin + "\n";
                }
                errMsg += "They will not be available for distribution.";

                JOptionPane.showMessageDialog(null, errMsg, "Unexpected Handins", JOptionPane.WARNING_MESSAGE);
            }

            return badHandins;
        }

        //not a group project- every handin's name will be a student's login,
        //so check that the login corresponding to the name of each handin is
        //in the database and enabled
        else {
            Collection<String> allStudentLogins = new ArrayList<String>();
            Collection<String> enabledStudentLogins = new ArrayList<String>();
            Collection<Student> students = Allocator.getDataServices().getAllStudents();
            for (Student student : students) {
                allStudentLogins.add(student.getLogin());
                if (student.isEnabled()) {
                    enabledStudentLogins.add(student.getLogin());
                }
            }

            Set<String> handinsNotInDB = new HashSet<String>();
            Set<String> handinsDisabled = new HashSet<String>();

            for (String handinLogin : handinNames) {
                if (!allStudentLogins.contains(handinLogin)) {
                    handinsNotInDB.add(handinLogin);
                }
                else if (!enabledStudentLogins.contains(handinLogin)) {
                    handinsDisabled.add(handinLogin);
                }
            }

            // if there are no issues then return an list of no logins
            if (handinsNotInDB.isEmpty() && handinsDisabled.isEmpty()) {
                return Collections.emptyList();
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
                        Allocator.getDataServices().addStudent(studentLogin, ValidityCheck.CHECK);
                        handinsNotInDB.remove(studentLogin);
                    }
                }

                for (IssueResolutionPanel disabledPanel : disabledPanels) {
                    if (disabledPanel.isChangeSelected()) {
                        String studentLogin = disabledPanel.getStudentLogin();
                        try {
                            Allocator.getDataServices().setStudentEnabled(Allocator.getDataServices().getStudentFromLogin(studentLogin), true);
                            handinsDisabled.remove(studentLogin);
                        } catch (ServicesException e) {
                            new ErrorView(e, "Student " + studentLogin + " could not be enabled.");
                        }
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
    }

    @Override
    public Map<String, Group> getGroupsForHandins(Assignment asgn, Collection<String> handinsToIgnore) throws ServicesException {
        Collection<String> handinNames;
        try {
            handinNames = asgn.getHandin().getHandinNames();
        } catch (IOException e) {
            throw new ServicesException("Unable to retrieve handin names", e);
        }

        handinNames.removeAll(handinsToIgnore);

        Collection<Group> groups = Allocator.getDataServices().getGroups(asgn);
        Map<String, Group> nameToGroup = new HashMap<String, Group>();
        Map<String, Group> loginToGroup = new HashMap<String, Group>();
        for (Group group : groups) {
            nameToGroup.put(group.getName(), group);

            for (Student member : group.getMembers()) {
                loginToGroup.put(member.getLogin(), group);
            }
        }

        Map<String, Group> toReturn = new HashMap<String, Group>();

        for (String handinName : handinNames) {
            if (nameToGroup.containsKey(handinName)) {
                toReturn.put(handinName, nameToGroup.get(handinName));
            }
            else if (loginToGroup.containsKey(handinName)) {
                toReturn.put(handinName, loginToGroup.get(handinName));
            }
            else {
                throw new ServicesException("There is no group corresponding to the handin named " + handinName + ".");
            }
        }

        return toReturn;
    }

    @Override
    public Map<Student, Group> getGroupsForStudents(Assignment asgn) throws ServicesException {
        Collection<Group> groups = Allocator.getDataServices().getGroups(asgn);

        Map<Student, Group> studentToGroup = new HashMap<Student, Group>();
        for (Group group : groups) {
            for (Student member : group.getMembers()) {
                studentToGroup.put(member, group);
            }
        }
        
        return studentToGroup;
    }

    @Override
    public void notifyStudents(Handin handin, Collection<Group> groups, boolean emailRubrics) {
        Map<Student, File> attachments = new HashMap<Student, File>();

        List<Student> students = new ArrayList<Student>(groups.size());
        for (Group group : groups) {
            for (Student student : group.getMembers()) {
                students.add(student);

                if (emailRubrics) {
                    attachments.put(student, Allocator.getPathServices().getGroupGRDFile(handin, group));
                }
            }
        }

        if (!emailRubrics) {
            attachments = null;
        }

        new EmailView(students, Allocator.getConfigurationInfo().getNotifyAddresses(),
                      "[" + Allocator.getCourseInfo().getCourse() + "] " + handin.getAssignment().getName() + " Graded",
                      handin.getAssignment().getName() + " has been graded.", "submitted grading", attachments);
    }

    @Override
    public void printGRDFiles(Handin handin, Iterable<Group> groups, CITPrinter printer) throws ServicesException
    {
        TA ta = Allocator.getUserServices().getUser();
        List<PrintRequest> requests = new ArrayList<PrintRequest>();

        for(Group group : groups)
        {
            File file = Allocator.getPathServices().getGroupGRDFile(handin, group);
            for(Student student : group.getMembers())
            {
                try
                {
                    requests.add(new PrintRequest(file, ta, student));
                }
                catch (FileNotFoundException ex) {
                    throw new ServicesException("Could not print GRD files because a requested" +
                                                "file was not found.\nFile: " + file.getAbsolutePath(), ex);
                }
            }
        }

        try
        {
            Allocator.getPortraitPrintingService().print(requests, printer);
        }
        catch(IOException e)
        {
            new ErrorView(e, "Unable to issue print command for " + handin.getAssignment() + ".\n" +
                              "For the following students: " + groups);
        }
    }

    private HandinStatus calculateHandinStatus(Handin handin, Group group, Calendar extension, int minutesOfLeniency) throws ServicesException {
        File groupHandin;
        try {
            groupHandin = handin.getHandin(group);
        } catch(IOException e) {
            throw new ServicesException(e);
        }

        if (groupHandin == null) {
            throw new ServicesException("Cannot get handin status for group " + group + ". " +
                                        "Handin file does not exist.");
        }
        Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(groupHandin);

        TimeStatus timeStatus = null;

        //If the policy is N0_LATE, or MULTIPLE_DEADLINES with an extension
        if ((handin.getTimeInformation().getLatePolicy() == LatePolicy.NO_LATE)
                || (extension != null && handin.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES)) {
            Calendar onTime = handin.getTimeInformation().getOntimeDate();
            if (extension != null) {
                onTime = extension;
            }

            //If before deadline
            if (Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency)) {
                timeStatus = TimeStatus.ON_TIME;
            } else {
                timeStatus = TimeStatus.NC_LATE;
            }
        } else if (handin.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION) {
            Calendar onTime = handin.getTimeInformation().getOntimeDate();
            if (extension != null) {
                onTime = extension;
            }

            //If before deadline
            if (Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency)) {
                timeStatus = TimeStatus.ON_TIME;
            } else {
                timeStatus = TimeStatus.LATE;
            }
        } else if (handin.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES) {
            Calendar earlyTime = handin.getTimeInformation().getEarlyDate();
            Calendar onTime = handin.getTimeInformation().getOntimeDate();
            Calendar lateTime = handin.getTimeInformation().getLateDate();

            // If before early deadline
            if (Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, earlyTime, minutesOfLeniency)) {
                timeStatus = TimeStatus.EARLY;
            } // If before ontime deadline
            else if (Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency)) {
                timeStatus = TimeStatus.ON_TIME;
            } // If before late deadline
            else if (Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, lateTime, minutesOfLeniency)) {
                timeStatus = TimeStatus.LATE;
            } // If after late deadline
            else {
                timeStatus = TimeStatus.NC_LATE;
            }
        }

        if (timeStatus != null) {
            return new HandinStatus(timeStatus, this.getDaysLate(handin, group, extension, minutesOfLeniency));
        }

        throw new ServicesException("Could not determine time status for group " +
                                    group + " on assignment " + handin.getAssignment() + ".");
    }

    @Override
    public void storeHandinStatuses(Handin handin, Collection<Group> groups,
                                    int minutesOfLeniency, boolean overwrite) throws ServicesException {
        Map<Group, Calendar> extensions = Allocator.getDataServices().getExtensions(handin);

        Map<Group, HandinStatus> handinStatuses = new HashMap<Group, HandinStatus>();
        for (Group group : groups) {
            //if overwrite is true or if group does not already have a handin status, calculate new handin status
            if (overwrite || Allocator.getDataServices().getHandinStatus(group) == null) {
                handinStatuses.put(group, calculateHandinStatus(handin, group, extensions.get(group), minutesOfLeniency));
            }
        }

        Allocator.getDataServices().setHandinStatuses(handinStatuses);
    }

    private int getDaysLate(Handin handin, Group group, Calendar extension, int minutesOfLeniency) throws ServicesException {
        File groupHandin;
        try {
            groupHandin = handin.getHandin(group);
        } catch(IOException e) {
            throw new ServicesException(e);
        }

        Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(groupHandin);
        Calendar onTime = handin.getTimeInformation().getOntimeDate();

        //if there is an extension, use that date
        if(extension != null){
            onTime = extension;
        }

        return Allocator.getCalendarUtilities().daysAfterDeadline(handinTime, onTime, minutesOfLeniency);
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
    
    @Override
    public Map<Assignment, Map<Student, Double>> getScores(Collection<Assignment> asgns,
            Collection<Student> studentsToInclude) throws ServicesException
    {
        HashSet<Student> studentsToIncludeHashed = new HashSet<Student>(studentsToInclude);
        
        Map<Assignment, Map<Student, Double>> allScores = new HashMap<Assignment, Map<Student, Double>>();
        for(Assignment asgn : asgns)
        {
            //Pull from database
            Collection<Group> groups = Allocator.getDataServices().getGroups(asgn);
            Map<Group, Double> groupScores = Allocator.getDataServices().getScores(asgn, groups);

            //Build a mapping from included students to their scores for the assignment
            Map<Student, Double> studentScores = new HashMap<Student, Double>();
            allScores.put(asgn, studentScores);
            for(Group group : groups)
            {
                for(Student student : group.getMembers())
                {
                    if(studentsToIncludeHashed.contains(student))
                    {
                        Double score = groupScores.get(group);
                        studentScores.put(student, (score == null ? 0 : score));
                    }
                }
            }
            for(Student student : studentsToInclude)
            {
                if(!studentScores.containsKey(student))
                {
                    studentScores.put(student, 0D);
                }
            }
        }
        
        return allScores;
    }
}