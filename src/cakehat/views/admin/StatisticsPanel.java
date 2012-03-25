package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
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

/**
 * A panel shown when multiple groups are selected and a part, gradable event, or assignment is selected. This view is
 * intended to show relevant statistics on the selected elements.
 * <br/><br/>
 * For now it just shows grading status, but this is intended to be expanded in the future.
 *
 * @author jak2
 */
class StatisticsPanel extends JPanel
{   
    private final JPanel _contentPanel;
    
    StatisticsPanel()
    {
        this.setBackground(Color.WHITE);
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        this.add(centerPanel, BorderLayout.CENTER);
        
        centerPanel.add(FormattedLabel.asHeader("Grading Status"));
        
        _contentPanel = new JPanel();
        _contentPanel.setBackground(Color.WHITE);
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        _contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(_contentPanel);
    }
    
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
            Map<Part, Map<Group, PartGrade>> grades = new HashMap<Part, Map<Group, PartGrade>>();
            for(Part part : parts)
            {
                grades.put(part, Allocator.getDataServices().getEarned(groupSelection, part));
            }

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
                    PartGrade grade = grades.get(part).get(group);
                    if(grade != null)
                    {
                        anySubmittedOrInProgress = true;
                    }
                    else if(grade == null || !grade.isSubmitted())
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
            new ErrorView(e, "Unable to determine grading status");
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