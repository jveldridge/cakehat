package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
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
            frame.setLocationRelativeTo(Allocator.getGeneralUtilities().getFocusedFrame());
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
            
            GradableEventPanel panel = new GradableEventPanel(part.getGradableEvent(), ImmutableSet.of(part), group,
                    isAdmin, submitOnSave, false);
            frame = new GradingSheetFrame<GradableEventPanel>(panel, title);
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
            
            frame.setLocationRelativeTo(Allocator.getGeneralUtilities().getFocusedFrame());
            frame.setVisible(true);
        }
        
        return frame;
    }

    @Override
    public GradingSheet getGradingSheet(Assignment asgn, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel<AssignmentPanel>(
                new AssignmentPanel(asgn, group, isAdmin, submitOnSave, false));
    }

    @Override
    public GradingSheet getGradingSheet(GradableEvent gradableEvent, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel<GradableEventPanel>(new GradableEventPanel(gradableEvent,
                ImmutableSet.copyOf(gradableEvent.getParts()), group,isAdmin, submitOnSave, false));
    }

    @Override
    public GradingSheet getGradingSheet(Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel<PartPanel>(
                PartPanel.getPartPanel(part, group, isAdmin, submitOnSave, false));
    }
}