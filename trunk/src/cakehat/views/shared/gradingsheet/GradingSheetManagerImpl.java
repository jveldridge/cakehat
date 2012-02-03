package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import support.utils.Pair;

/**
 *
 * @author jak2
 */
public class GradingSheetManagerImpl implements GradingSheetManager
{
    private final Map<Pair<Part, Group>, GradingSheetFrame> _openGradingSheetFrames =
            new HashMap<Pair<Part, Group>, GradingSheetFrame>();
    
    @Override
    public GradingSheet showFrame(Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        final Pair<Part, Group> pair = Pair.of(part, group);
        
        GradingSheetFrame frame = _openGradingSheetFrames.get(pair);
        if(frame != null)
        {
            frame.toFront();
        }
        else
        {   
            String title;
            if(group == null)
            {
                title = "Template for " + part.getFullDisplayName();
            }
            else
            {
                title = group.getName() + "'s " + part.getFullDisplayName();
            }
            
            frame = new GradingSheetFrame(PartPanel.getPartPanel(part, group, isAdmin, submitOnSave), title);
            _openGradingSheetFrames.put(pair, frame);
            
            frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosed(WindowEvent we)
                {
                    GradingSheetFrame frame = _openGradingSheetFrames.get(pair);
                    _openGradingSheetFrames.remove(pair);
                    frame.save();
                }
            });
            
            frame.setVisible(true);
        }
        
        return frame;
    }

    @Override
    public GradingSheet getGradingSheet(Assignment asgn, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel(new AssignmentPanel(asgn, group, isAdmin, submitOnSave));
    }

    @Override
    public GradingSheet getGradingSheet(GradableEvent gradableEvent, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel(new GradableEventPanel(gradableEvent, group,isAdmin, submitOnSave));
    }

    @Override
    public GradingSheet getGradingSheet(Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel(PartPanel.getPartPanel(part, group, isAdmin, submitOnSave));
    }
}