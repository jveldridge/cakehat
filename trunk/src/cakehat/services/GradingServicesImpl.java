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
import cakehat.printing.PrintRequest;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.rubric.TimeStatus;
import cakehat.views.shared.EmailView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;

public class GradingServicesImpl implements GradingServices
{
    @Override
    public void makeUserWorkspace() throws ServicesException
    {
        File workspace = Allocator.getPathServices().getUserWorkspaceDir();
        try
        {
            Allocator.getFileSystemServices().makeDirectory(workspace);
        }
        catch(ServicesException e)
        {
            throw new ServicesException("Unable to create user's workspace: " +
                    workspace.getAbsolutePath(), e);
        }
    }

    @Override
    public void removeUserWorkspace() throws ServicesException
    {
        File workspace = Allocator.getPathServices().getUserWorkspaceDir();
        try
        {
            Allocator.getFileSystemUtilities().deleteFiles(Arrays.asList(workspace));
        }
        catch(IOException e)
        {
            throw new ServicesException("Unable to remove user's workspace: " +
                    workspace.getAbsolutePath(), e);
        }
    }

    @Override
    public String getPrinter()
    {
        return this.getPrinter("Please select a printer.");
    }

    private static final String[] DEVELOPER_PRINTERS = new String[] { "bw1", "bw2", "bw3", "bw4", "bw5" };
    private static final String[] DEFAULT_PRINTERS = new String[] { "bw3", "bw4", "bw5" };
    private static final String DEFAULT_PRINTER = "bw3";
    @Override
    public String getPrinter(String message)
    {
        String[] printerChoices = null;

        // select printer choices based on if under developer mode
        if (CakehatMain.isDeveloperMode())
        {
            printerChoices = DEVELOPER_PRINTERS;
        }
        else
        {
            printerChoices = DEFAULT_PRINTERS;
        }

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.PRINTER);

        return (String) JOptionPane.showInputDialog(new JFrame(), message,
                "Select Printer", JOptionPane.PLAIN_MESSAGE, icon,
                printerChoices, DEFAULT_PRINTER);
    }

    @Override
    public boolean isOkToDistribute(Group group, TA ta) throws ServicesException {
        Map<TA, Collection<String>> blacklistMap = new HashMap<TA, Collection<String>>();
        try {
            blacklistMap.put(ta, Allocator.getDatabaseIO().getTABlacklist(ta));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklist for TA " + ta + " " +
                                        "from the database.", ex);
        }

        if (groupMemberOnTAsBlacklist(group, blacklistMap)) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + group + " is on TA "
                                                    + ta.getLogin() + "'s blacklist.  Continue?",
                                                    "Distribute Blacklisted Student?",
                                                    JOptionPane.YES_NO_OPTION);
            return (shouldContinue == JOptionPane.YES_OPTION);
        }

        return true;
    }

    @Override
    public boolean groupMemberOnTAsBlacklist(Group group, Map<TA, Collection<String>> blacklists) throws ServicesException {
        for (TA ta : blacklists.keySet()) {
            Collection<String> blackList = blacklists.get(ta);
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
            Collection<String> badHandins = new LinkedList<String>();

            Collection<Group> groups;
            try {
                groups = Allocator.getDatabaseIO().getGroupsForAssignment(asgn);
            } catch (SQLException ex) {
                throw new ServicesException("Could not resolve missing students for " +
                                            "assignment " + asgn + " because groups could " +
                                            "not be retrieved from the database.", ex);
            }

            for (Group group : groups) {
                validNames.add(group.getName());

                for (String studentLogin : group.getMembers()) {
                    validNames.add(studentLogin);
                }
            }

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
            Collection<String> allStudents;
            Collection<String> enabledStudents;
            try {
                allStudents = Allocator.getDatabaseIO().getAllStudents().keySet();
                enabledStudents = Allocator.getDatabaseIO().getEnabledStudents().keySet();
            } catch (SQLException e) {
                throw new ServicesException("Students could not be retrieved from the database.", e);
            }

            Set<String> handinsNotInDB = new HashSet<String>();
            Set<String> handinsDisabled = new HashSet<String>();

            for (String handinLogin : handinNames) {
                if (!allStudents.contains(handinLogin)) {
                    handinsNotInDB.add(handinLogin);
                }
                else if (!enabledStudents.contains(handinLogin)) {
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
                Collection<Group> groupsToAdd = new LinkedList<Group>();

                for (IssueResolutionPanel notInDBPanel : notInDBPanels) {
                    if (notInDBPanel.isChangeSelected()) {
                        String studentLogin = notInDBPanel.getStudentLogin();
                        Allocator.getUserServices().addStudent(studentLogin, UserServices.ValidityCheck.CHECK);
                        groupsToAdd.add(new Group(studentLogin, studentLogin));
                        handinsNotInDB.remove(studentLogin);
                    }
                }

                for (IssueResolutionPanel disabledPanel : disabledPanels) {
                    if (disabledPanel.isChangeSelected()) {
                        String studentLogin = disabledPanel.getStudentLogin();
                        try {
                            Allocator.getDatabaseIO().enableStudent(studentLogin);
                            groupsToAdd.add(new Group(studentLogin, studentLogin));
                            handinsDisabled.remove(studentLogin);
                        } catch (SQLException e) {
                            new ErrorView(e, "Student " + studentLogin + " could not be enabled.");
                        }
                    }
                }
                try {
                    //create groups of one for newly added or enabled students
                    Allocator.getDatabaseIO().setGroups(asgn, groupsToAdd);
                    
                    //and store their handin statuses
                    this.storeHandinStatuses(asgn.getHandin(), groupsToAdd, Allocator.getConfigurationInfo().getMinutesOfLeniency(), true);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not create internally required groups of one in " +
                                      "the database for the newly added and/or enabled students.");
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

    public Map<String, Group> getGroupsForHandins(Assignment asgn, Collection<String> handinsToIgnore) throws ServicesException {
        Collection<String> handinNames;
        try {
            handinNames = asgn.getHandin().getHandinNames();
        } catch (IOException e) {
            throw new ServicesException("Unable to retrieve handin names", e);
        }

        handinNames.removeAll(handinsToIgnore);

        Collection<Group> groups;
        try {
            groups = Allocator.getDatabaseIO().getGroupsForAssignment(asgn);
        } catch (SQLException ex) {
            throw new ServicesException("Could not get groups for assignment " + asgn + " "
                    + "from the database.", ex);
        }

        Map<String, Group> nameToGroup = new HashMap<String, Group>();
        Map<String, Group> loginToGroup = new HashMap<String, Group>();
        for (Group group : groups) {
            nameToGroup.put(group.getName(), group);

            for (String member : group.getMembers()) {
                loginToGroup.put(member, group);
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

    public Map<String, Group> getGroupsForStudents(Assignment asgn) throws ServicesException {
        Collection<Group> groups;
        try {
            groups = Allocator.getDatabaseIO().getGroupsForAssignment(asgn);
        } catch (SQLException ex) {
            throw new ServicesException("Could not get groups for assignment " + asgn + " "
                    + "from the database.", ex);
        }

        Collection<String> students;
        try {
            students = Allocator.getDatabaseIO().getEnabledStudents().keySet();
        } catch (SQLException ex) {
            throw new ServicesException("Could not get list of enabled students from the database.", ex);
        }

        Map<String, Group> loginToGroup = new HashMap<String, Group>();
        for (Group group : groups) {
            for (String member : group.getMembers()) {
                loginToGroup.put(member, group);
            }
        }

        //if not a group assignment, create entries in group table in db and Group
        //object for students who do not already have corresponding groups of one
        if (!asgn.hasGroups()) {
            Collection<Group> groupsToAdd = new LinkedList<Group>();

            for (String student : students) {
                if (!loginToGroup.containsKey(student)) {
                    Group newGroup = new Group(student, student);
                    groupsToAdd.add(newGroup);
                    loginToGroup.put(student, newGroup);
                }
            }
            try {
                Allocator.getDatabaseIO().setGroups(asgn, groupsToAdd);
            } catch (SQLException ex) {
                throw new ServicesException("Could not save internal groups of " +
                                            "one to database for students " + groupsToAdd +
                                            "on assignment " + asgn + ".", ex);
            }
        }

        return loginToGroup;
    }

    @Override
    public void notifyStudents(Handin handin, Collection<Group> groups, boolean emailRubrics) {
        Map<String,String> attachments = new HashMap<String, String>();

        List<String> students = new ArrayList<String>(groups.size());
        for (Group group : groups) {
            for (String student : group.getMembers()) {
                students.add(student + "@" + Allocator.getConstants().getEmailDomain());

                if (emailRubrics) {
                    attachments.put(student,Allocator.getPathServices().getGroupGRDFile(handin, group).getAbsolutePath());
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
    public void printGRDFiles(Handin handin, Iterable<Group> groups) throws ServicesException {
        String printer = this.getPrinter("Select printer to print .GRD files");

        if (printer == null) {
            return;
        }

        String taLogin = Allocator.getUserUtilities().getUserLogin();
        Vector<PrintRequest> requests = new Vector<PrintRequest>();

        for (Group group : groups) {
            File file = Allocator.getPathServices().getGroupGRDFile(handin, group);
            for (String student : group.getMembers()) {
                try {
                    requests.add(new PrintRequest(file, taLogin, student));
                } catch (FileNotFoundException ex) {
                    throw new ServicesException("Could not print GRD files because a requested" +
                                                "file was not found.\nFile: " + file.getAbsolutePath(), ex);
                }
            }
        }

        try {
            Allocator.getPortraitPrinter().print(requests, printer);
        } catch (IOException e) {
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
        Map<Group, Calendar> extensions;
        try {
            extensions = Allocator.getDatabaseIO().getAllExtensions(handin);
        } catch (SQLException ex) {
            throw new ServicesException("Could not read extensions for assignment " + handin.getAssignment() + " " +
                                      "from the database.  Rubrics cannot be distributed because handin " +
                                      "status cannot be determined.", ex);
        }

        Map<Group, HandinStatus> handinStatuses = new HashMap<Group, HandinStatus>();
        for (Group group : groups) {
            try {
                //if overwrite is true or if group does not already have a handin status, calculate new handin status
                if (overwrite || Allocator.getDatabaseIO().getHandinStatus(handin, group) == null) {
                    handinStatuses.put(group, calculateHandinStatus(handin, group, extensions.get(group), minutesOfLeniency));
                }
            } catch (SQLException ex) {
                throw new ServicesException("Could not determine whether group " +
                                            group + " already has a handin status " +
                                            "for assignment " + handin.getAssignment() +
                                            ".  No handin statuses have been changed.", ex);
            }
        }

        try {
            Allocator.getDatabaseIO().setHandinStatuses(handin, handinStatuses);
        } catch (SQLException ex) {
            throw new ServicesException("Could not store handin statuses in the database for " +
                                      "assignment " + handin.getAssignment() + ".  Rubrics cannot be distributed.", ex);
        }
        
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
}