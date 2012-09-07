package cakehat.views.shared.gradingsheet;

import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.Group;
import cakehat.database.Student;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import support.ui.FormattedLabel;
import support.utils.NullMath;

/**
 *
 * @author jak2
 */
class AssignmentPanel extends GradingSheetPanel
{
    private final Assignment _asgn;
    private final Group _group;
    private final boolean _isAdmin;
    
    //Map from panel to if it has unsaved changes (true = unsaved changes)
    private final Map<GradableEventPanel, Boolean> _gradableEventPanelSaveStatus =
            new HashMap<GradableEventPanel, Boolean>();
    
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    
    private Double _totalEarned = null;
    private Double _totalOutOf = null;
    
    AssignmentPanel(Assignment asgn, Group group, boolean isAdmin, boolean showBorder)
    {
        super(Color.WHITE, showBorder);
        
        if(asgn == null)
        {
            throw new NullPointerException("asgn may not be null");
        }
        if(group == null)
        {
            throw new NullPointerException("group may not be null");
        }
        
        _asgn = asgn;
        _group = group;
        _isAdmin = isAdmin;
        
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
        addContent(FormattedLabel.asHeader(_asgn.getName()));
        
        if(_group.isGroupOfOne())
        {
            Student student = _group.getOnlyMember();
            addContent(FormattedLabel.asSubheader("Student: " +  student.getName() + " (" + student.getLogin() + ")")
                    .grayOut());
        }
        else
        {
            addContent(FormattedLabel.asSubheader("Group: " + _group.getName()).grayOut());
            for(Student member : _group)
            {
                addContent(FormattedLabel.asSubheader("\t" + member.getName() + " (" + member.getLogin() + ")")
                        .grayOut());
            }
        }
    }
    
    private void initGradableEventsUI()
    {
        for(Iterator<GradableEvent> geIterator = _asgn.iterator(); geIterator.hasNext();)
        {
            GradableEvent gradableEvent = geIterator.next();
            
            final GradableEventPanel panel = new GradableEventPanel(gradableEvent, gradableEvent.getParts(), _group,
                    _isAdmin, true);
            _focusableComponents.addAll(panel.getFocusableComponents());
            
            _gradableEventPanelSaveStatus.put(panel, false);
            _totalEarned = NullMath.add(_totalEarned, panel.getEarned());
            _totalOutOf = NullMath.add(_totalOutOf, panel.getOutOf());
            
            panel.addGradingSheetListener(new GradingSheetListener()
            {
                @Override
                public void earnedChanged(Double prevEarned, Double currEarned)
                {
                    Double prevTotalEarned = _totalEarned;

                    _totalEarned = NullMath.subtract(_totalEarned, prevEarned);
                    _totalEarned = NullMath.add(_totalEarned, currEarned);

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
            
            addContent(panel);
            if(geIterator.hasNext())
            {
                addContent(Box.createVerticalStrut(10));
            }
        }
    }
    
    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }

    @Override
    Double getEarned()
    {
        return _totalEarned;
    }

    @Override
    Double getOutOf()
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