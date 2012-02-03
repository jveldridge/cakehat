package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.database.Group;
import cakehat.database.Student;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;

/**
 *
 * @author jak2
 */
class AssignmentPanel extends GradingSheetPanel
{
    private final Assignment _asgn;
    private final Group _group;
    private final boolean _isAdmin;
    private final boolean _submitOnSave;
    
    //Map from panel to if it has unsaved changes (true = unsaved changes)
    private final Map<GradableEventPanel, Boolean> _gradableEventPanelSaveStatus =
            new HashMap<GradableEventPanel, Boolean>();
    
    private double _totalEarned = 0;
    private double _totalOutOf = 0;
    
    AssignmentPanel(Assignment asgn, Group group, boolean isAdmin, boolean submitOnSave)
    {
        super(Color.WHITE);
        
        _asgn = asgn;
        _group = group;
        _isAdmin = isAdmin;
        _submitOnSave = submitOnSave;
        
        initUI();
    }
    
    private void initUI()
    {
        initHeaderUI();
        
        addContent(Box.createVerticalStrut(10));
        
        initGradableEventsUI();
    }
    
    private void initHeaderUI()
    {
        addContent(createHeaderLabel(_asgn.getName(), false));
        
        if(_group == null)
        {
            addContent(createSubheaderLabel("Template", true));
        }
        else
        {
            if(_group.isGroupOfOne())
            {
                Student student = _group.getOnlyMember();
                addContent(createSubheaderLabel("Student: " +  student.getName() + " (" + student.getLogin() + ")",
                        true));
            }
            else
            {
                addContent(createSubheaderLabel("Group: " + _group.getName(), true));
                for(Student member : _group)
                {
                    addContent(createSubheaderLabel("\t" + member.getName() + " (" + member.getLogin() + ")", true));
                }
            }
        }
    }
    
    private void initGradableEventsUI()
    {
        for(int i = 0; i < _asgn.getGradableEvents().size(); i++)
        {
            final GradableEvent gradableEvent = _asgn.getGradableEvents().get(i);
            
            final GradableEventPanel panel = new GradableEventPanel(gradableEvent, _group, _isAdmin, _submitOnSave);
            
            _gradableEventPanelSaveStatus.put(panel, false);
            _totalEarned += panel.getEarned();
            _totalOutOf += panel.getOutOf();
            
            panel.addGradingSheetListener(new GradingSheetListener()
            {
                @Override
                public void earnedChanged(double prevEarned, double currEarned)
                {
                    double prevTotalEarned = _totalEarned;

                    _totalEarned -= prevEarned;
                    _totalEarned += currEarned;

                    notifyEarnedChanged(prevTotalEarned, _totalEarned);
                    notifyUnsavedChangeOccurred();
                }
              
                @Override
                public void saveChanged(boolean hasUnsavedChanges)
                {
                    _gradableEventPanelSaveStatus.put(panel, hasUnsavedChanges);
                    
                    if(hasUnsavedChanges)
                    {
                        notifyUnsavedChangeOccurred();
                    }
                    else
                    {
                        boolean allSaved = true;
                        for(boolean unsavedChanges : _gradableEventPanelSaveStatus.values())
                        {
                            allSaved = allSaved && !unsavedChanges;
                        }

                        if(allSaved)
                        {
                            notifySavedSuccessfully();
                        }
                    }
                };
            });
            
            panel.setAlignmentX(LEFT_ALIGNMENT);
            addContent(panel);
            if(i != _asgn.getGradableEvents().size() - 1)
            {
                addContent(Box.createVerticalStrut(10));
            }
        }
    }

    @Override
    public double getEarned()
    {
        return _totalEarned;
    }

    @Override
    public double getOutOf()
    {
        return _totalOutOf;
    }

    @Override
    public void save()
    {
        for(GradableEventPanel panel : _gradableEventPanelSaveStatus.keySet())
        {
            if(panel.hasUnsavedChanges())
            {
                panel.save();
            }
        }
    }
}