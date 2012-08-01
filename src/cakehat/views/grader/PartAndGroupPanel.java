package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.database.TA;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import support.ui.DescriptionProvider;
import support.ui.FormattedLabel;
import support.ui.GenericJComboBox;
import support.ui.GenericJList;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
class PartAndGroupPanel extends JPanel
{   
    private final FormattedLabel _studentOrGroupLabel;
    private final GenericJComboBox<Part> _partComboBox;
    private final GenericJList<Group> _groupList;
    
    private final Map<Part, Map<Group, GroupStatus>> _assignedGroups = new HashMap<Part, Map<Group, GroupStatus>>();
    
    private final List<PartAndGroupSelectionListener> _listeners =
            new CopyOnWriteArrayList<PartAndGroupSelectionListener>();
    
    PartAndGroupPanel()
    {   
        this.setLayout(new BorderLayout(0, 0));

        //Commands
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        this.add(northPanel, BorderLayout.NORTH);

        //Parts
        northPanel.add(FormattedLabel.asHeader("Assignment Parts"));
        
        northPanel.add(Box.createVerticalStrut(5));
        
        _partComboBox = new GenericJComboBox<Part>(new PartDescriptionProvider());
        _partComboBox.setAlignmentX(LEFT_ALIGNMENT);
        _partComboBox.addSelectionListener(new SelectionListener<Part>()
        {
            @Override
            public void selectionPerformed(Part currValue, Part newValue, SelectionAction action)
            {
                notifyPartSelectionChanged(newValue);
            }
        });
        northPanel.add(_partComboBox);

        northPanel.add(Box.createVerticalStrut(10));
        
        //Groups
        _studentOrGroupLabel = FormattedLabel.asHeader("Students");
        northPanel.add(_studentOrGroupLabel);
        
        northPanel.add(Box.createVerticalStrut(5));
        
        _groupList = new GenericJList<Group>();
        _groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _groupList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    notifyGroupSelectionChanged(new HashSet<Group>(_groupList.getGenericSelectedValues()));
                }
            }
        });
        JScrollPane groupListPane = new JScrollPane(_groupList);
        groupListPane.setBackground(Color.WHITE);
        this.add(groupListPane, BorderLayout.CENTER);
        
        //Load data from database into the UI elements
        loadAssignedGrading();
    }
    
    private void notifyPartSelectionChanged(Part part)
    {
        if(part != null && part.getAssignment().hasGroups())
        {
            _studentOrGroupLabel.setText("Groups");
        }
        else
        {
            _studentOrGroupLabel.setText("Students");
        }

        //Get the groups assigned for this part
        this.populateGroupsList(part);
        
        //Notify listeners
        for(PartAndGroupSelectionListener listener : _listeners)
        {
            listener.selectionChanged(part, getSelectedGroups());
        }
    }
    
    private void notifyGroupSelectionChanged(Set<Group> groups)
    {
        //Notify listeners
        for(PartAndGroupSelectionListener listener : _listeners)
        {
            listener.selectionChanged(getSelectedPart(), groups);
        }
    }
    
    Part getSelectedPart()
    {
        return _partComboBox.getSelectedItem();
    }
    
    Set<Group> getSelectedGroups()
    {
        return new HashSet<Group>(_groupList.getGenericSelectedValues());
    }
    
    static interface PartAndGroupSelectionListener
    {
        public void selectionChanged(Part part, Set<Group> groups);
    }

    void addSelectionListener(PartAndGroupSelectionListener listener)
    {
        _listeners.add(listener);
    }

    void removeSelectionListener(PartAndGroupSelectionListener listener)
    {
        _listeners.remove(listener);
    }
    
    /**
     * Populates the group list with the Groups that the TA has been assigned to grade (as recorded in the database) for
     * the selected Part.
     */
    private void populateGroupsList(Part part)
    {
        List<GroupStatus> statuses = new ArrayList<GroupStatus>();
        if(part != null)
        {
            statuses.addAll(_assignedGroups.get(part).values());
        }
        Collections.sort(statuses);
        List<Group> groups = new ArrayList<Group>();
        for(GroupStatus status : statuses)
        {
            groups.add(status.getGroup());
        }
        _groupList.setDescriptionProvider(new GroupDescriptionProvider(part));
        _groupList.setListData(groups, true);
        if(_groupList.isSelectionEmpty())
        {
            _groupList.selectFirst();
        }
    }
    
    final void loadAssignedGrading()
    {
        //Retrieve new data
        _assignedGroups.clear();
        try
        {
            TA user = Allocator.getUserServices().getUser();
            
            Set<Part> parts = Allocator.getDataServices().getPartsWithAssignedGroups(user);
            for(Part part : parts)
            {
                Set<Group> groups = new HashSet<Group>(Allocator.getDataServices().getAssignedGroups(part, user));
                Map<Group, PartGrade> grades = Allocator.getDataServices().getEarned(groups, part);
                
                _assignedGroups.put(part, new HashMap<Group, GroupStatus>());
                for(Group group : groups)
                {
                    _assignedGroups.get(part).put(group, new GroupStatus(group, grades.get(group)));
                }
            }
        }
        catch(ServicesException e)
        {
            _assignedGroups.clear();
            ErrorReporter.report("Unable to load assigned grading", e);
        }
        
        //Load new data into parts combo box
        Part initiallySelectedPart = _partComboBox.getSelectedItem();
        List<Part> sortedParts = new ArrayList<Part>(_assignedGroups.keySet());
        Collections.sort(sortedParts);
        _partComboBox.setItems(sortedParts);
        
        //If there has no been no change in part selection, update group list to reflect any changes in assigned groups
        if(initiallySelectedPart == _partComboBox.getSelectedItem())
        {
            //Update the group list to reflect any changes in assigned groups
            this.populateGroupsList(initiallySelectedPart);
        }
        //If the part selected before this update still exists in the list, maintain that selection
        else if(initiallySelectedPart != null && sortedParts.contains(initiallySelectedPart))
        {
            _partComboBox.setGenericSelectedItem(initiallySelectedPart);
        }
        //Otherwise if there are any parts, select the last one as it is likely the part being graded by the TA
        else if(!sortedParts.isEmpty())
        {
            _partComboBox.setGenericSelectedItem(sortedParts.get(sortedParts.size() - 1));
        }
    }
    
    /**
     * The status of a group, which is made up whether it has been submitted or modified.
     */
    private static class GroupStatus implements Comparable<GroupStatus>
    {
        private final Group _group;
        private boolean _submitted;
        private boolean _modified;

        public GroupStatus(Group group, PartGrade partGrade)
        {
            _group = group;
            _submitted = (partGrade != null && partGrade.isSubmitted());
            _modified = (partGrade != null);
        }

        public Group getGroup()
        {
            return _group;
        }

        public boolean isSubmitted()
        {
            return _submitted;
        }

        public boolean isModified()
        {
            return _modified;
        }
        
        public void setSubmitted(boolean submitted)
        {
            _submitted = submitted;
        }
        
        public void setModified(boolean modified)
        {
            _modified = modified;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean equals = false;
            if(obj instanceof GroupStatus)
            {
                GroupStatus other = (GroupStatus) obj;
                equals = other._group.equals(_group) && other._modified == _modified && other._submitted == _submitted;
            }
            
            return equals;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 61 * hash + (this._group != null ? this._group.hashCode() : 0);
            hash = 61 * hash + (this._submitted ? 1 : 0);
            hash = 61 * hash + (this._modified ? 1 : 0);
            
            return hash;
        }

        @Override
        public int compareTo(GroupStatus other)
        {
            //Comparison on group names breaks ties
            int groupComp = this.getGroup().getName().compareTo(other.getGroup().getName());

            //Heirarchy
            // - submitted
            // - modified
            // - nothing
            if(this.isSubmitted() && other.isSubmitted())
            {
                return groupComp;
            }
            else if(this.isSubmitted())
            {
                return -1;
            }
            else if(other.isSubmitted())
            {
                return 1;
            }
            else if(this.isModified() && other.isModified())
            {
                return groupComp;
            }
            else if(this.isModified())
            {
                return -1;
            }
            else if(other.isModified())
            {
                return 1;
            }
            else
            {
                return groupComp;
            }
        }
    }

    private final class GroupDescriptionProvider implements DescriptionProvider<Group>
    {
        private final Part _part;

        public GroupDescriptionProvider(Part part)
        {
            _part = part;
        }

        @Override
        public String getDisplayText(Group group)
        {
            boolean modified = false;
            boolean submitted = false;
            
            GroupStatus groupStatus = _part == null ? null : _assignedGroups.get(_part).get(group);  
            if(groupStatus != null)
            {
                modified = groupStatus.isModified();
                submitted = groupStatus.isSubmitted();
            }

            //Build representation
            String pre;
            String post;
            if(submitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else if(modified)
            {
                pre = "<strong><font color=#686868>";
                post = "</font><strong>";
            }
            else
            {
                pre = "<strong>";
                post = "<strong>";
            }

            String representation = "<html>" + pre + group.getName() + post + "</html>";

            return representation;
        }

        @Override
        public String getToolTipText(Group group)
        {
            String toolTip = "";
            List<Student> students = new ArrayList<Student>(group.getMembers());
            Collections.sort(students);
            for(int i = 0; i < students.size(); i++)
            {
                toolTip += students.get(i).getName();
                if(i != students.size() - 1)
                {
                    toolTip += ", ";
                }
            }
            
            return toolTip;
        }
    }
    
    private class PartDescriptionProvider implements DescriptionProvider<Part>
    {
        @Override
        public String getDisplayText(Part part)
        {
            return getRepresentation(part);
        }

        @Override
        public String getToolTipText(Part part)
        {
            return getRepresentation(part);
        }
        
        private String getRepresentation(Part part)
        {
            String representation;
            if(part == null)
            {
                representation = null;
            }
            else
            {
                int numParts = 0;
                for(GradableEvent ge : part.getAssignment())
                {
                    numParts += ge.getParts().size();
                }
                if(numParts == 1)
                {
                    representation = part.getAssignment().getName() + " - "  + part.getName();
                }
                else
                {
                    representation = part.getFullDisplayName();
                }
            }
                
            return representation;
        }
    }
}