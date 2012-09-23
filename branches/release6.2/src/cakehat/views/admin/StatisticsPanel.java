package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.Student;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import support.ui.FormattedLabel;
import support.ui.PaddingPanel;

/**
 * A panel showing information for groups and the selected part, gradable event, or assignment. This view is intended to
 * show relevant statistics on the selected elements.
 * <br/><br/>
 * For now it just shows grading status, but this is intended to be expanded in the future.
 *
 * @author jak2
 */
class StatisticsPanel extends PaddingPanel
{   
    private final JPanel _contentPanel;
    
    StatisticsPanel()
    {
        super(PaddingPanel.DEFAULT_PAD, Color.WHITE);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(this.getBackground());
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        this.addContentComponent(centerPanel);
        
        centerPanel.add(FormattedLabel.asHeader("Grading Status"));
        
        _contentPanel = new JPanel();
        _contentPanel.setBackground(centerPanel.getBackground());
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        _contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(_contentPanel);
    }
    
    /**
     * Shows statistics for the provided selection. If {@code groupSelection} is {@code null} then all groups for the
     * assignment are used.
     * 
     * @param assignmentSelection may not be {@code null}
     * @param groupSelection may be {@code null}, if so then all groups for the selected assignment will be used
     */
    void displayFor(AssignmentTreeSelection assignmentSelection, Set<Group> groupSelection)
    {
        _contentPanel.removeAll();
        
        //Parts the statistics are being determined for
        Set<Part> parts = new HashSet<Part>();
        if(assignmentSelection.getPart() != null)
        {
            _contentPanel.add(FormattedLabel.asSubheader(assignmentSelection.getPart().getName()).grayOut());
            parts.add(assignmentSelection.getPart());
        }
        else if(assignmentSelection.getGradableEvent() != null)
        {
            _contentPanel.add(FormattedLabel.asSubheader(assignmentSelection.getGradableEvent().getName()).grayOut());
            parts.addAll(assignmentSelection.getGradableEvent().getParts());
        }
        else
        {
            _contentPanel.add(FormattedLabel.asSubheader(assignmentSelection.getAssignment().getName()).grayOut());
            for(GradableEvent ge : assignmentSelection.getAssignment())
            {
                parts.addAll(ge.getParts());
            }
        }
        
        _contentPanel.add(Box.createVerticalStrut(10));
        
        try
        {
            if(groupSelection == null)
            {
                groupSelection = Allocator.getDataServices().getGroups(assignmentSelection.getAssignment());
            }
            
            SetMultimap<Part, Group> dataToQuery = HashMultimap.create();
            for(Part part : parts)
            {
                dataToQuery.putAll(part, groupSelection);
            }
            Map<Part, Map<Group, GroupGradingSheet>> gradingSheets = Allocator.getDataServices()
                    .getGroupGradingSheets(dataToQuery);
            
            //Categories
            // - Not started (no parts submitted or in progress)
            // - Grading in progress (some, but not all, parts submitted OR
            //                        some, including all, parts with non-submitted grades)
            // - Submitted (all parts submitted)
            List<Group> notStartedGroups = new ArrayList<Group>();
            List<Group> inProgressGroups = new ArrayList<Group>();
            List<Group> allSubmittedGroups = new ArrayList<Group>();
            for(Group group : groupSelection)
            {
                boolean anySubmittedOrInProgress = false;
                boolean allSubmitted = true;
                
                for(Part part : parts)
                {
                    GroupGradingSheet gradingSheet = gradingSheets.get(part).get(group);
                    
                    if(gradingSheet.getId() != null)
                    {
                        anySubmittedOrInProgress = true;
                    }
                    
                    if(gradingSheet.getId() == null || !gradingSheet.isSubmitted())
                    {
                        allSubmitted = false;
                    }
                }
                
                if(allSubmitted)
                {
                    allSubmittedGroups.add(group);
                }
                else if(anySubmittedOrInProgress)
                {
                    inProgressGroups.add(group);
                }
                else
                {
                    notStartedGroups.add(group);
                }
            }
            
            //Display
            showGradingStatusForGroups("Not Started", notStartedGroups);
            showGradingStatusForGroups("In Progress", inProgressGroups);
            showGradingStatusForGroups("Completed", allSubmittedGroups);
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to determine grading status", e);
            _contentPanel.add(FormattedLabel.asSubheader("Unable to load grades").showAsErrorMessage()
                    .centerHorizontally());
        }
        
        this.repaint();
        this.revalidate();
    }
    
    private void showGradingStatusForGroups(String message, List<Group> groups)
    {
        if(!groups.isEmpty())
        {
            _contentPanel.add(FormattedLabel.asSubheader(message));

            Collections.sort(groups);
            for(Group group : groups)
            {
                FormattedLabel groupLabel = FormattedLabel.asContent(" • " + group.getName()).usePlainFont();
                
                String toolTip = "";
                Iterator<Student> students = group.getMembers().iterator();
                while(students.hasNext())
                {
                    toolTip += students.next().getName();
                    if(students.hasNext())
                    {
                        toolTip += ", ";
                    }
                }
                groupLabel.setToolTipText(toolTip);
                
                _contentPanel.add(groupLabel);
            }
            
            _contentPanel.add(Box.createVerticalStrut(10));
        }
    }
}