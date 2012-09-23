package cakehat.services;

import cakehat.assignment.Assignment;
import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.database.GradableEventOccurrence;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.DeadlineInfo;
import cakehat.database.DeadlineInfo.DeadlineResolution;
import cakehat.database.DeadlineInfo.Type;
import cakehat.database.Extension;
import cakehat.database.TA;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.GroupGradingSheet.GroupSectionComments;
import cakehat.database.GroupGradingSheet.GroupSubsectionEarned;
import cakehat.database.Student;
import cakehat.gradingsheet.GradingSheetDetail;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import cakehat.printing.CITPrinter;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import org.joda.time.DateTime;
import support.ui.ModalDialog;

public class GradingServicesImpl implements GradingServices
{
    @Override
    public Map<Group, DateTime> getOccurrenceDates(GradableEvent ge, Set<Group> groups) throws ServicesException
    {
        ImmutableMap.Builder<Group, DateTime> occurrenceDates = ImmutableMap.builder();
        
        Map<Group, GradableEventOccurrence> occurrences = Allocator.getDataServices()
                .getGradableEventOccurrences(ge, groups);
        
        //Groups which have an occurence recorded in the database but now have a digital handin will have their
        //occurence date deleted from the database
        Set<Group> occurencesToDelete = new HashSet<Group>();
        
        for(Group group : groups)
        {
            GradableEventOccurrence occurrence = occurrences.get(group);
            
            File digitalHandin = null;
            if(ge.hasDigitalHandins())
            {
                try
                {
                    digitalHandin = ge.getDigitalHandin(group);
                }
                catch(IOException e)
                {
                    throw new ServicesException("Unable to retrieve digital handin", e);
                }
            }
            
            if(occurrence != null)
            {
                if(digitalHandin != null)
                {
                    occurencesToDelete.add(group);
                    occurrenceDates.put(group, new DateTime(digitalHandin.lastModified()));
                }
                else
                {
                    occurrenceDates.put(group, occurrence.getOccurrenceDate());
                }
            }
            else if(digitalHandin != null)
            {
                occurrenceDates.put(group, new DateTime(digitalHandin.lastModified()));
            }
        }
        
        if(!occurencesToDelete.isEmpty())
        {
            Allocator.getDataServices().deleteGradableEventOccurrences(ge, occurencesToDelete);
        }
        
        return occurrenceDates.build();
    }

    private static final List<CITPrinter> NORMALLY_ALLOWED_PRINTERS =
            Arrays.asList(CITPrinter.bw3, CITPrinter.bw4, CITPrinter.bw5);
    private static final List<CITPrinter> DEVELOPER_ALLOWED_PRINTERS =
            Arrays.asList(CITPrinter.bw1, CITPrinter.bw2, CITPrinter.bw3, CITPrinter.bw4, CITPrinter.bw5);
    private static final CITPrinter DEFAULT_PRINTER = CITPrinter.bw3;

    @Override
    public CITPrinter getDefaultPrinter()
    {
        return DEFAULT_PRINTER;
    }

    @Override
    public List<CITPrinter> getAllowedPrinters()
    {
        List<CITPrinter> allowed;
        if(CakehatSession.isDeveloperMode())
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
    public CITPrinter getPrinter()
    {
        return this.getPrinter("Please select a printer.");
    }

    @Override
    public CITPrinter getPrinter(String message)
    {
        CITPrinter[] printerChoices = getAllowedPrinters().toArray(new CITPrinter[0]);

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.PRINTER);

        return (CITPrinter) JOptionPane.showInputDialog(null, message, "Select Printer", JOptionPane.PLAIN_MESSAGE,
                icon, printerChoices, DEFAULT_PRINTER);
    }

    @Override
    public boolean isOkToDistribute(Group group, TA ta) throws ServicesException
    {
        boolean distribute = true;
        if(ta != null)
        {
            Collection<Student> blacklist = Allocator.getDataServices().getBlacklist(ta);
            if(!Collections.disjoint(blacklist, group.getMembers()))
            {
                distribute = ModalDialog.showConfirmation(null, "Distribute Blacklisted Student?",
                    "A member of group " + group + " is on TA " + ta.getLogin() + "'s blacklist.",
                    "Distribute", "Cancel");
            }
        }

        return distribute;
    }
    
    @Override
    public boolean isSomeGroupMemberBlacklisted(Group group, Map<TA, Collection<Student>> blacklists) throws ServicesException {
        for (TA ta : blacklists.keySet()) {
            Collection<Student> blackList = blacklists.get(ta);
            if (!Collections.disjoint(blackList, group.getMembers())) {
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
            
            boolean proceed  = ModalDialog.showConfirmation(null, "Non-distributable groups", warnMsg.toString(),
                    "Proceed", "Cancel");
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
            
            boolean proceed = ModalDialog.showConfirmation(null, "Unexpected Handins", warnMsg.toString(),
                    "Proceed", "Cancel");
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

        JLabel explainationText = new JLabel("<html><p>The following students are <font color=red>not</font> in the " +
                "database or are disabled and they have handins for: <font color=blue><i>" + ge.getFullDisplayName() +
                "</i></font>. You should consider adding them to the database or enabling them. If you do not their " +
                "handins will <font color=red>not</font> be distributed to a TA for grading.</p></html>");
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
            
            @Override
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

            Map<Student, Boolean> studentsToEnable = new HashMap<Student, Boolean>();
            for (IssueResolutionPanel disabledPanel : disabledPanels) {
                if (disabledPanel.isChangeSelected()) {
                    String studentLogin = disabledPanel.getStudentLogin();
                    studentsToEnable.put(Allocator.getDataServices().getStudentFromLogin(studentLogin), true);
                }
            }
            
            Allocator.getDataServices().setStudentsAreEnabled(studentsToEnable);
            handinsDisabled.removeAll(studentsToEnable.keySet());

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
    public Map<Student, String> generateGRD(Assignment asgn, Set<Student> students) throws ServicesException {
        Map<Student, Group> groupsForStudents = new HashMap<Student, Group>();
        for (Student student : students) {
            groupsForStudents.put(student, Allocator.getDataServices().getGroup(asgn, student));
        }

        SetMultimap<Part, Group> toRetrieve = HashMultimap.create();
        for (GradableEvent ge : asgn) {
            for (Part part : ge) {
                toRetrieve.putAll(part, groupsForStudents.values());
            }
        }
        
        Map<Part, Map<Group, GroupGradingSheet>> gradingSheets =
                Allocator.getDataServices().getGroupGradingSheets(toRetrieve);
        
        Map<Student, String> toReturn = new HashMap<Student, String>();
        for (Student student : students) {
            Map<Part, GroupGradingSheet> gradingSheetsForStudent = new HashMap<Part, GroupGradingSheet>();
            for (Part part : toRetrieve.keySet()) {
                gradingSheetsForStudent.put(part, gradingSheets.get(part).get(groupsForStudents.get(student)));
            }
            
            toReturn.put(student, this.generateGrd(asgn, student, gradingSheetsForStudent));
        }
        
        return toReturn;
    }
    
    private String generateGrd(Assignment asgn, Student student, Map<Part, GroupGradingSheet> gradingSheets) throws ServicesException {
        Group group = Allocator.getDataServices().getGroup(asgn, student) ;

        StringBuilder grdBuilder = new StringBuilder("<table width='600px'><tr><td style='text-align:center'>");
        grdBuilder.append(group.getAssignment().getName()).append(" Grading Sheet");
        grdBuilder.append("</td></tr></table>");
        
        grdBuilder.append("<p><b>Student:</b> ").append(student.getName());
        grdBuilder.append(" (").append(student.getLogin()).append(')');
        if (group.size() > 1) {
            Iterator<Student> members = group.getMembers().iterator();
            grdBuilder.append("<br/><b>Group:</b> ").append(group.getName()).append(" (").append(members.next().getLogin());
            while (members.hasNext()) {
                grdBuilder.append(", ").append(members.next().getLogin());
            }
            grdBuilder.append(')');
        }
        grdBuilder.append("</p>");
        
        double asgnEarned = 0;
        double asgnOutOf = 0;
        
        for (GradableEvent ge : group.getAssignment()){
            Score geScore = this.generateGradableEventGRD(ge, group, gradingSheets, grdBuilder);
            
            asgnEarned += geScore._earned;
            asgnOutOf += geScore._outOf;
            
            grdBuilder.append("<br/>");
        }

        grdBuilder.append("<table width='600px' style='border: 1px solid; border-spacing: 0px'>");
        this.writeLineWithEarnedAndOutOf("<b>Total Grade</b>", "Earned", "Out of", grdBuilder, TopLineStyle.NONE);
        this.writeLineWithEarnedAndOutOf("Total Score:", doubleToString(asgnEarned), doubleToString(asgnOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        grdBuilder.append("</table>");
        
        return grdBuilder.toString();
    }
    
    private Score generateGradableEventGRD(GradableEvent ge, Group group, Map<Part, GroupGradingSheet> gradingSheets,
                                           StringBuilder grdBuilder) throws ServicesException {
        double geEarned = 0;
        double geOutOf = 0;
        
        grdBuilder.append("<table width='600px' style='border: 1px solid; border-spacing: 0px'>");
        
        grdBuilder.append("<tr><td colspan='3' style='border-bottom: 1px solid'><b>").append(ge.getName()).append("</b></td></tr>");
        for (Part part : ge) {
            Score partScore = this.generatePartGRD(gradingSheets.get(part), grdBuilder);
            
            geEarned += partScore._earned;
            geOutOf += partScore._outOf;
        }
        
        this.writeLineWithEarnedAndOutOf("Parts total:", doubleToString(geEarned), doubleToString(geOutOf), grdBuilder,
                                         TopLineStyle.FULL);

        DeadlineInfo info = Allocator.getDataServices().getDeadlineInfo(ge);
        if (info.getType() != Type.NONE) {
            DeadlineResolution res;
            DateTime occurrenceDate = Allocator.getGradingServices().getOccurrenceDates(ge, ImmutableSet.of(group)).get(group);
            Extension extension = Allocator.getDataServices().getExtensions(ge, ImmutableSet.of(group)).get(group);
            res = info.apply(occurrenceDate, extension); 
            double penalty = res.getPenaltyOrBonus(geEarned);

            this.writeLineWithEarnedAndOutOf("Deadline resolution: " + res.getTimeStatus().toString(),
                                             doubleToString(penalty), "", grdBuilder, TopLineStyle.FULL);

            geEarned += penalty;
        }
        
        this.writeLineWithEarnedAndOutOf(ge.getName() + " Score", doubleToString(geEarned), doubleToString(geOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        grdBuilder.append("</table>");
        
        return new Score(geEarned, geOutOf);
    }
    
    private Score generatePartGRD(GroupGradingSheet groupGradingSheet, StringBuilder grdBuilder) {
        double partEarned = 0;
        double partOutOf = 0;
        
        grdBuilder.append("<tr><td colspan='3' style='border-top: 1px solid; border-bottom: 1px solid'>Part: ").append(groupGradingSheet.getGradingSheet().getPart().getName());
        String grader = groupGradingSheet.getAssignedTo() == null
                ? "Not specified"
                : groupGradingSheet.getAssignedTo().getName() + " (" + groupGradingSheet.getAssignedTo().getLogin() + ")";
        grdBuilder.append("<br/>Grader: ").append(grader).append("</td></tr>");
        
        if (groupGradingSheet.isSubmitted()) {
            for (GradingSheetSection section : groupGradingSheet.getGradingSheet().getSections()) {
                Score sectionScore = this.generateSectionGRD(section, groupGradingSheet, grdBuilder);

                partEarned += sectionScore._earned;
                partOutOf += sectionScore._outOf;
            }
        }
        else {
            grdBuilder.append("<tr><td colspan='3'>");
            grdBuilder.append("<i>Your grade has not been submitted for this part. Please contact the TAs.</i>");
            grdBuilder.append("</td></tr>");
        }
        
        this.writeLineWithEarnedAndOutOf("Part total:", doubleToString(partEarned), doubleToString(partOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        
        return new Score(partEarned, partOutOf);
    }
    
    private Score generateSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                             StringBuilder grdBuilder) {        
        if (section.getOutOf() == null) { //additive grading
            return this.generateAdditiveSectionGRD(section, gradingSheet, grdBuilder);
        }
        else {                            //subtractive grading
            return this.generateSubtractiveSectionGRD(section, gradingSheet, grdBuilder);
        }
    }
    
    private Score generateAdditiveSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                             StringBuilder grdBuilder) {            
        double sectionEarned = 0;
        double sectionOutOf = 0;

        this.writeLineWithEarnedAndOutOf(section.getName(), "Earned", "Out of", grdBuilder, TopLineStyle.POINTS_ONLY);

        for (GradingSheetSubsection subsection : section.getSubsections()) {
            StringBuilder subsectionBuilder = new StringBuilder(this.generateSpaces(5));
            subsectionBuilder.append(subsection.getText());

            if (!subsection.getDetails().isEmpty()) {
                subsectionBuilder.append("<ul style='margin: 0px 5px 10px;'>");
                for (GradingSheetDetail detail : subsection.getDetails()) {
                    subsectionBuilder.append("<li>").append(detail.getText()).append("</li>");
                }
                subsectionBuilder.append("</ul>");
            }

            Double earned = null;
            GroupSubsectionEarned earnedRecord = gradingSheet.getEarnedPoints().get(subsection);
            if (earnedRecord != null) {
                earned = earnedRecord.getEarned();
            }

            String earnedString = earned == null ? "--" : doubleToString(earned);
            this.writeLineWithEarnedAndOutOf(subsectionBuilder.toString(), earnedString,
                                             doubleToString(subsection.getOutOf()), grdBuilder, TopLineStyle.NONE);

            sectionEarned += earned == null ? 0 : earned;
            sectionOutOf += subsection.getOutOf();
        }

        this.generateCommentsGRD(gradingSheet.getComments().get(section), grdBuilder);

        this.writeLineWithEarnedAndOutOf("Total&nbsp;&nbsp;", TextAlignment.RIGHT, doubleToString(sectionEarned),
                                         doubleToString(sectionOutOf), grdBuilder, TopLineStyle.NONE);

        return new Score(sectionEarned, sectionOutOf);
    }
    
    private Score generateSubtractiveSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                              StringBuilder grdBuilder) {
        throw new UnsupportedOperationException("Subtractive grading is not yet supported.");
    }
    
    private void generateCommentsGRD(GroupSectionComments commentsRecord, StringBuilder grdBuilder) {
        if (commentsRecord != null && commentsRecord.getComments() != null && !commentsRecord.getComments().isEmpty()) {
            StringBuilder comments = new StringBuilder("<br/>Comments:<blockquote>");
            comments.append(commentsRecord.getComments());
            comments.append("</blockquote>");

            this.writeLineWithEarnedAndOutOf(comments.toString(), "", "", grdBuilder, TopLineStyle.NONE);
        }
    }
    
    private void writeLineWithEarnedAndOutOf(String text, String earned, String outOf, StringBuilder grdBuilder,
                                             TopLineStyle topLineStyle) {
        this.writeLineWithEarnedAndOutOf(text, TextAlignment.LEFT, earned, outOf, grdBuilder, topLineStyle);
    }
    
    private void writeLineWithEarnedAndOutOf(String text, TextAlignment alignment, String earned, String outOf,
                                             StringBuilder grdBuilder, TopLineStyle topLineStyle) {
        String textTd = "", pointsTd = "";
        String alignStyle = alignment == TextAlignment.RIGHT ? "text-align: right" : "";
        
        if (topLineStyle == TopLineStyle.NONE) {
            textTd = String.format("<td style='%s'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid;'>";
        }
        else if (topLineStyle == topLineStyle.POINTS_ONLY) {
            textTd = String.format("<td style='%s'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid; border-top: 1px solid'>";
        }
        else if (topLineStyle == topLineStyle.FULL) {
            textTd = String.format("<td style='%s; border-top: 1px solid'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid; border-top: 1px solid'>";
        }
        else {
            throw new IllegalArgumentException("Invalid TopLineStyle given.");
        }

        grdBuilder.append("<tr>").append(textTd).append(text).append("</td>");
        grdBuilder.append(pointsTd).append(earned).append("</td>");
        grdBuilder.append(pointsTd).append(outOf).append("</td></tr>");
    }
    
    private static enum TextAlignment {
        LEFT, RIGHT;
    }
    
    private static enum TopLineStyle {
        NONE, FULL, POINTS_ONLY;
    }
    
    private String generateSpaces(int numSpaces) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numSpaces; i++) {
            builder.append("&nbsp;");
        }
        
        return builder.toString();
    }

    private static class Score {
        private double _earned;
        private double _outOf;

        public Score(double earned, double outOf) {
            _earned = earned;
            _outOf = outOf;
        }   
    }

    private static String doubleToString(double value) {
        double roundedVal;
        
        if(Double.isNaN(value)) {
            roundedVal = Double.NaN;
        }
        else {
            BigDecimal bd = new BigDecimal(Double.toString(value));
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            roundedVal = bd.doubleValue();
        }

        return Double.toString(roundedVal);
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