package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.assignment.GradableEvent;
import cakehat.database.Group;
import cakehat.database.TA;
import cakehat.assignment.Part;
import cakehat.database.GroupGradingSheet;
import cakehat.database.Student;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
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
    private final GenericJList<GroupStatus> _groupList;
    
    private AssignedGradingStatus _assignedStatus = AssignedGradingStatus.NOT_LOADED;
    private final SetMultimap<Part, GroupStatus> _assignedGrading = HashMultimap.create();
    
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
            public void selectionPerformed(Part currValue, final Part newValue, SelectionAction action)
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
        
        _groupList = new GenericJList<GroupStatus>(new GroupStatusDescriptionProvider());
        _groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _groupList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting() && !_partComboBox.getModel().isSelectionInProgress())
                {
                    notifyGroupSelectionChanged(_groupList.getGenericSelectedValues());
                }
            }
        });
        JScrollPane groupListPane = new JScrollPane(_groupList);
        groupListPane.setBackground(Color.WHITE);
        this.add(groupListPane, BorderLayout.CENTER);
        
        //Load data from database into the UI elements
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                loadAssignedGrading();
            }
        });
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
        this.populateGroupsList(part, false);
        
        //Notify listeners
        for(PartAndGroupSelectionListener listener : _listeners)
        {
            listener.selectionChanged(part, getSelectedGroups());
        }
    }
    
    private void notifyGroupSelectionChanged(List<GroupStatus> groupStatuses)
    {
        Set<Group> groups = new HashSet<Group>();
        for(GroupStatus status : groupStatuses)
        {
            groups.add(status.getGroup());
        }
        
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
        Set<Group> selectedGroups = new HashSet<Group>();
        for(GroupStatus status : _groupList.getGenericSelectedValues())
        {
            selectedGroups.add(status.getGroup());
        }
        
        return selectedGroups;
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
    private void populateGroupsList(Part part, boolean maintainSelection)
    {
        List<GroupStatus> statuses = new ArrayList<GroupStatus>();
        if(part != null)
        {
            statuses.addAll(_assignedGrading.get(part));
        }
        Collections.sort(statuses);
        
        _groupList.setListData(statuses, maintainSelection);
        
        if(_groupList.isSelectionEmpty())
        {
            GroupStatus unsubmittedStatus = null;
            for(GroupStatus status : statuses)
            {
                if(!status.isSubmitted())
                {
                    unsubmittedStatus = status;
                    break;
                }
            }
            
            if(unsubmittedStatus != null)
            {
                _groupList.setSelectedValue(unsubmittedStatus);
            }
            else
            {
                _groupList.selectFirst();
            }
        }
    }
        
    AssignedGradingStatus getAssignedGradingStatus()
    {
        return _assignedStatus;
    }
    
    enum AssignedGradingStatus
    {
        NOT_LOADED, LOADED_GROUPS_ASSIGNED, LOADED_NONE_ASSIGNED, ERROR_LOADING;
    }
    
    final void loadAssignedGrading()
    {
        //Retrieve new data
        _assignedStatus = AssignedGradingStatus.NOT_LOADED;
        _assignedGrading.clear();
        try
        {
            TA user = Allocator.getUserServices().getUser();
            SetMultimap<Part, Group> assignedGroups = Allocator.getDataServices().getAssignedGroups(user);
            Map<Part, Map<Group, GroupGradingSheet>> gradingSheets = Allocator.getDataServices()
                        .getGroupGradingSheets(assignedGroups);
            
            for(Part part : gradingSheets.keySet())
            {
                for(GroupGradingSheet gradingSheet : gradingSheets.get(part).values())
                {
                    _assignedGrading.put(part, new GroupStatus(gradingSheet));
                }
            }
            
            _assignedStatus = _assignedGrading.isEmpty() ?
                              AssignedGradingStatus.LOADED_NONE_ASSIGNED : AssignedGradingStatus.LOADED_GROUPS_ASSIGNED;
        }
        catch(ServicesException e)
        {
            _assignedStatus = AssignedGradingStatus.ERROR_LOADING;
            ErrorReporter.report("Unable to load assigned grading", e);
        }
        
        //Load new data into parts combo box
        Part initiallySelectedPart = _partComboBox.getSelectedItem();
        List<Part> sortedParts = new ArrayList<Part>(_assignedGrading.keySet());
        Collections.sort(sortedParts);
        
        Part newSelectedPart = null;
        //If the part selected before this update still exists in the list, maintain that selection
        if(initiallySelectedPart != null && sortedParts.contains(initiallySelectedPart))
        {
            newSelectedPart = initiallySelectedPart;
        }
        //Otherwise if there are any parts, select the last one as it is likely the part being graded by the TA
        else if(!sortedParts.isEmpty())
        {
            newSelectedPart = sortedParts.get(sortedParts.size() - 1);
        }
        _partComboBox.setItems(sortedParts, newSelectedPart);
        
        //If there has no been no change in part selection, update group list to reflect any changes in assigned groups
        if(initiallySelectedPart == _partComboBox.getSelectedItem())
        {
            //Update the group list to reflect any changes in assigned groups
            this.populateGroupsList(initiallySelectedPart, true);
        }
    }
    
    void notifyGradingSheetModified(Part part, Group group)
    {
        boolean refreshList = false;
        
        Set<GroupStatus> statuses = _assignedGrading.get(part);
        if(statuses != null)
        {
            for(GroupStatus status : statuses)
            {
                if(status.getGroup().equals(group))
                {
                    refreshList = !status.isModified();
                    status.markModified();
                }
            }
        }
        
        if(refreshList)
        {
            _groupList.refreshList();
        }
    }
    
    void notifyGradingSheetSubmissionChanged(Part part, Group group, boolean submission)
    {
        boolean refreshList = false;
        
        Set<GroupStatus> statuses = _assignedGrading.get(part);
        if(statuses != null)
        {
            for(GroupStatus status : statuses)
            {
                if(status.getGroup().equals(group))
                {
                    refreshList = (submission != status.isSubmitted());
                    status.setSubmitted(submission);
                }
            }
        }
        
        if(refreshList)
        {
            _groupList.refreshList();
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

        public GroupStatus(GroupGradingSheet gradingSheet)
        {
            _group = gradingSheet.getGroup();
            _submitted = gradingSheet.isSubmitted();
            _modified = _submitted || !gradingSheet.getComments().isEmpty() || !gradingSheet.getEarnedPoints().isEmpty();
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
            _modified = true;
        }
        
        public void markModified()
        {
            _modified = true;
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

    private final class GroupStatusDescriptionProvider implements DescriptionProvider<GroupStatus>
    {
        @Override
        public String getDisplayText(GroupStatus groupStatus)
        {
            //Build representation
            String pre;
            String post;
            if(groupStatus.isSubmitted())
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else if(groupStatus.isModified())
            {
                pre = "<strong><font color=#686868>";
                post = "</font></strong>";
            }
            else
            {
                pre = "<strong>";
                post = "</strong>";
            }
            
            String representation = "<html>" + pre + groupStatus.getGroup().getName() + post + "</html>";

            return representation;
        }

        @Override
        public String getToolTipText(GroupStatus groupStatus)
        {
            StringBuilder toolTip = new StringBuilder();
            List<Student> students = new ArrayList<Student>(groupStatus.getGroup().getMembers());
            Collections.sort(students);
            for(int i = 0; i < students.size(); i++)
            {
                toolTip.append(students.get(i).getName());
                if(i != students.size() - 1)
                {
                    toolTip.append(", ");
                }
            }
            
            return toolTip.toString();
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