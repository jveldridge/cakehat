package cakehat.services;

import cakehat.database.assignment.Assignment;
import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import cakehat.Allocator;
import cakehat.CakehatMain;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.TA;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.gml.GMLGRDWriter;
import cakehat.gml.GradingSheetException;
import cakehat.printing.CITPrinter;
import cakehat.printing.PrintRequest;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import support.ui.ModalDialog;
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
    public void makeDatabaseBackup() throws ServicesException
    {
        String backupFileName =  "database_backup_" + System.currentTimeMillis() + ".db";
        File backupFile = new File(Allocator.getPathServices().getDatabaseBackupDir(), backupFileName);
        try
        {
            Allocator.getFileSystemServices().copy(Allocator.getPathServices().getDatabaseFile(), backupFile,
                    OverwriteMode.FAIL_ON_EXISTING, false, FileCopyPermissions.READ_WRITE);
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
    public Set<String> resolveUnexpectedHandins(GradableEvent ge) throws ServicesException {
        Set<String> handinNames;
        try {
            handinNames = ge.getDigitalHandinNames();
        } catch (IOException e) {
            throw new ServicesException("Unable to retrieve handin names for " + ge.getName(), e);
        }
         if (ge.getAssignment().hasGroups()) {
             return this.resolveUnexpectedHandinsForGroupAssignment(ge, handinNames);
         }
         else {
             return this.resolveUnexpectedHandinsForNonGroupAssignment(ge, handinNames);
         }
    }
    
    private Set<String> resolveUnexpectedHandinsForGroupAssignment(GradableEvent ge, Set<String> handinNames) throws ServicesException {
        //check that each group for the assignment has at least one enabled student
        Set<Student> enabledStudents = Allocator.getDataServices().getEnabledStudents();
        Set<Group> distributableGroups = new HashSet<Group>(Allocator.getDataServices().getGroups(ge.getAssignment()));
        Set<Group> nonDistributableGroups = new HashSet<Group>();
        
        for (Group group : distributableGroups) {
            boolean hasEnabledStudent = false;
            
            for (Student student : group) {
                if (enabledStudents.contains(student)) {
                    hasEnabledStudent = true;
                    break;
                }
            }
            
            if (!hasEnabledStudent) {
                nonDistributableGroups.add(group);
            }
        }
        
        if (!nonDistributableGroups.isEmpty()) {
            StringBuilder warnMsg = new StringBuilder("The following groups contain no enabled students.  They will "
                    + "not be available for distribution.");
            for (Group group : nonDistributableGroups) {
                warnMsg.append(" - ").append(group).append('\n');
            }
            
            boolean proceed  = ModalDialog.showConfirmation("Non-distributable groups", warnMsg.toString(), "Proceed",
                                                            "Cancel");
            if (!proceed) {
                return null;
            }
        }
        
        //check that that the name of each handin is either the name of some distributable group or the login of a
        //member of some distributable group
        Set<String> validNames = new HashSet<String>();
        for (Group group : distributableGroups) {
            validNames.add(group.getName());

            for (Student student : group) {
                validNames.add(student.getLogin());
            }
            
        }
        
        Set<String> badHandins = new HashSet<String>();
        for (String handinName : handinNames) {
            if (!validNames.contains(handinName)) {
                badHandins.add(handinName);
            }
        }

        if (!badHandins.isEmpty()) {
            StringBuilder warnMsg = new StringBuilder("The following handins do not correspond to a group name or a "
                    + "group member's login:\n");
            for (String handin : badHandins) {
                warnMsg.append(handin).append('\n');
            }
            warnMsg.append("They will not be available for distribution.");
            
            boolean proceed = ModalDialog.showConfirmation("Unexpected Handins", warnMsg.toString(), "Proceed", "Cancel");
            if (!proceed) {
                return null;
            }
        }

        return badHandins;
    }
    
    private Set<String> resolveUnexpectedHandinsForNonGroupAssignment(GradableEvent ge, Set<String> handinNames)
            throws ServicesException {
        //every handin's name will be a student's login, so check that the login corresponding to the name of each 
        //handin is in the database and enabled
        
        Set<Student> students = Allocator.getDataServices().getStudents();
        Set<Student> enabledStudents = Allocator.getDataServices().getEnabledStudents();
        Set<String> allStudentLogins = new HashSet<String>();
        Set<String> enabledStudentLogins = new HashSet<String>();
        
        for (Student student : students) {
            allStudentLogins.add(student.getLogin());
        }
        
        for (Student enabledStudent : enabledStudents) {
            enabledStudentLogins.add(enabledStudent.getLogin());
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

        // if there are no issues then return a set of no logins
        if (handinsNotInDB.isEmpty() && handinsDisabled.isEmpty()) {
            return Collections.emptySet();
        }

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel explainationText = new JLabel("<html><p>The following students are"
                + " <font color=red>not</font> in the database or are disabled and they"
                + " have handins for: <font color=blue><i>" + ge.getFullDisplayName() + "</i></font>."
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
                IssueResolutionPanel iRPanel = new IssueResolutionPanel(handinNotInDB, "Add");
                notInDBChoicePanel.add(iRPanel);
                notInDBPanels.add(iRPanel);
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
                    notInDBPanel.setActionToChange();
                }
                for (IssueResolutionPanel disabledPanel : disabledPanels) {
                    disabledPanel.setActionToChange();
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
            Set<String> loginsToAdd = new HashSet<String>();
            for (IssueResolutionPanel notInDBPanel : notInDBPanels) {
                if (notInDBPanel.isChangeSelected()) {
                    String studentLogin = notInDBPanel.getStudentLogin();
                    loginsToAdd.add(studentLogin);
                    handinsNotInDB.remove(studentLogin);
                }
            }
            if (!loginsToAdd.isEmpty()) {
                Allocator.getDataServices().addStudentsByLogin(loginsToAdd);
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

            //create a list of the remaining bad handin names
            Set<String> badHandinNames = new HashSet<String>();
            badHandinNames.addAll(handinsNotInDB);
            badHandinNames.addAll(handinsDisabled);
            return badHandinNames;
        }
        return null;
    }

    @Override
    public Map<String, Group> getGroupsForHandins(GradableEvent ge, Set<String> handinsToIgnore) throws ServicesException {
        Set<String> handinNames;
        try {
            handinNames = new HashSet<String>(ge.getDigitalHandinNames());
        } catch (IOException e) {
            throw new ServicesException("Unable to retrieve handin names for " + ge.getName(), e);
        }
        handinNames.removeAll(handinsToIgnore);
        
        Collection<Group> groups = Allocator.getDataServices().getGroups(ge.getAssignment());
        Map<String, Group> validNamesToGroup = new HashMap<String, Group>();
        for (Group group : groups) {
            validNamesToGroup.put(group.getName(), group);

            for (Student member : group) {
                validNamesToGroup.put(member.getLogin(), group);
            }
        }

        Map<String, Group> toReturn = new HashMap<String, Group>();

        for (String handinName : handinNames) {
            if (validNamesToGroup.containsKey(handinName)) {
                toReturn.put(handinName, validNamesToGroup.get(handinName));
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
            for (Student member : group) {
                studentToGroup.put(member, group);
            }
        }
        
        return studentToGroup;
    }

    @Override
    public void printGRDFiles(Assignment asgn, Set<Group> groups, CITPrinter printer) throws ServicesException
    {
        //Create a set of print requests - one for each student
        Map<Student, File> grdFiles = generateGRDFiles(asgn, groups);
        TA ta = Allocator.getUserServices().getUser();
        Set<PrintRequest> requests = new HashSet<PrintRequest>();
        for(Entry<Student, File> entry : grdFiles.entrySet())
        {
            try
            {
                requests.add(new PrintRequest(entry.getValue(), ta, entry.getKey()));
            }
            catch (FileNotFoundException ex)
            {
                throw new ServicesException("Could not print GRD file, because file was not found.\n" +
                        "File: " + entry.getValue().getAbsolutePath() + "\n" +
                        "Student: " + entry.getKey().getLogin() + "\n" +
                        "Assignment: " + asgn, ex);
            }
        }

        //Submit print requests
        try
        {
            Allocator.getPortraitPrintingService().print(requests, printer);
        }
        catch(IOException e)
        {
            throw new ServicesException("Unable to issue print command for GRD files", e);
        }
    }
    
    @Override
    public void emailGRDFiles(Assignment asgn, Set<Group> groups) throws ServicesException
    {
        EmailAccountStatus emailStatus = Allocator.getEmailManager().getEmailAccountStatus();
        if(emailStatus == EmailAccountStatus.INITIALIZATION_ERROR)
        {
            throw new ServicesException("The email account could not initialize properly");
        }
        else if(emailStatus == EmailAccountStatus.NOT_CONFIGURED)
        {
            ModalDialog.showMessage("Email Unavailable", "Email has not been configured by your course");
        }
        else
        {
            Map<Student, File> grdFiles = generateGRDFiles(asgn, groups);
        
            boolean proceed = ModalDialog.showConfirmation("Email Grading Sheets",
                    "Each student will be sent an email with their grading sheet attached, do you wish to proceed?",
                    "Email Students", "Cancel");
            if(proceed)
            {
                InternetAddress from = Allocator.getUserServices().getUser().getEmailAddress();
                String subject = "[" + Allocator.getCourseInfo().getCourse() + "]" + asgn.getName() + " Graded";
                String body = asgn.getName() + " has been graded; your grading sheet is attached as a plain text file.";

                
                for(Entry<Student, File> entry : grdFiles.entrySet())
                {
                    try
                    {
                        Allocator.getEmailManager().send(from,
                                                         ImmutableSet.of(entry.getKey().getEmailAddress()),
                                                         null,
                                                         null,
                                                         subject,
                                                         body,
                                                         ImmutableSet.of(entry.getValue()));
                    }
                    catch(MessagingException e)
                    {
                        throw new ServicesException("Unable to send grading sheet\n" +
                                "Student: " + entry.getKey().getLogin() + "\n" +
                                "Student's email: " + entry.getKey().getEmailAddress() + "\n" +
                                "File: " + entry.getValue().getAbsolutePath() , e);
                    }
                }
            }
        }
    }

    @Override
    public Map<Student, File> generateGRDFiles(Assignment asgn, Set<Group> groups) throws ServicesException
    {
        //Validate all groups belong to the assignment
        for(Group group : groups)
        {
            if(!group.getAssignment().equals(asgn))
            {
                throw new ServicesException(group.getName() + " does not belong to assignment " + asgn.getName());
            }
        }
        
        //Determine which grades have not been submitted
        Multimap<Group, Part> groupsWithNonSubmittedGrades = HashMultimap.create();
        for(GradableEvent ge : asgn)
        {
            for(Part part : ge)
            {
                Map<Group, PartGrade> grades = Allocator.getDataServices().getEarned(groups, part);
                
                for(Group group : groups)
                {
                    PartGrade grade = grades.get(group);
                    if(grade == null || !grade.isSubmitted())
                    {
                        groupsWithNonSubmittedGrades.put(group, part);
                    }
                }
            }
        }
        
        //Determine whether to proceed if not all groups have submitted grades
        boolean proceed = true;
        if(!groupsWithNonSubmittedGrades.isEmpty())
        {
            String groupsOrStudents = asgn.hasGroups() ? "groups" : "students";
            
            StringBuilder builder = new StringBuilder();
            builder.append("The following ");
            builder.append(groupsOrStudents);
            builder.append(" do not have grades submitted for one or more parts:\n\n");
            for(Entry<Group, Collection<Part>> entry : groupsWithNonSubmittedGrades.asMap().entrySet())
            {
                builder.append(entry.getKey().getName());
                builder.append("\n");
                
                for(Part part : entry.getValue())
                {
                    builder.append(" • ");
                    builder.append(part.getFullDisplayName());
                    builder.append("\n");
                }
            }
            builder.append("\n\nThese ");
            builder.append(groupsOrStudents);
            builder.append(" will not be included, do you wish to proceed?");
            
            proceed = ModalDialog.showConfirmation("Some Grades Not Submitted", builder.toString(), "Proceed", "Cancel");
        }
        
        //Generate GRD files
        HashMap<Student, File> generatedGRDs = new HashMap<Student, File>();
        if(proceed)
        {
            HashSet<Group> groupsWithSubmittedGrades = new HashSet<Group>(groups);
            groupsWithSubmittedGrades.removeAll(groupsWithNonSubmittedGrades.keySet());
            
            for(Group group : groupsWithSubmittedGrades)
            {
                for(Student student : group)
                {
                    File grdFile = Allocator.getPathServices().getStudentGRDFile(asgn, student);
                    try
                    {
                        GMLGRDWriter.write(group, grdFile);
                        generatedGRDs.put(student, grdFile);
                    }
                    catch(GradingSheetException e)
                    {
                        throw new ServicesException("Unable to create GRD file\n" +
                                "Group: " + group.getName() + "\n" +
                                "Student: " + student.getName() + "\n" +
                                "Assignment: " + asgn.getName() + "\n" +
                                "File: " + grdFile.getAbsolutePath());
                    }
                }
            }
        }
        
        return generatedGRDs;
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

        public void setActionToChange()
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
}