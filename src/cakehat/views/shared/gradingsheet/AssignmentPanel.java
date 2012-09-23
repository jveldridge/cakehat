package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.views.shared.gradingsheet.GradableEventPanel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    
    private final Set<GradableEventPanel> _gradableEventPanels = new HashSet<GradableEventPanel>();
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    
    private Double _totalEarned = null;
    private Double _totalOutOf = null;
    
    AssignmentPanel(Assignment asgn, Group group, boolean isAdmin, boolean showBorder)
            throws GradingSheetInitializationException
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
    
    private void initUI() throws GradingSheetInitializationException
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
    
    private void initGradableEventsUI() throws GradingSheetInitializationException
    {
        for(Iterator<GradableEvent> geIterator = _asgn.iterator(); geIterator.hasNext();)
        {
            GradableEvent gradableEvent = geIterator.next();
            
            final GradableEventPanel panel = new GradableEventPanel(gradableEvent, gradableEvent.getParts(), _group,
                    _isAdmin, true);
            _gradableEventPanels.add(panel);
            _focusableComponents.addAll(panel.getFocusableComponents());
            
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
                }

                @Override
                public void modificationOccurred()
                {
                    notifyModificationOccurred();
                }

                @Override
                public void submissionChanged(Part part, boolean submitted)
                {
                    notifySubmissionChanged(part, submitted);
                }
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
        for(GradableEventPanel panel : _gradableEventPanels)
        {
            panel.save();
        }
    }
}