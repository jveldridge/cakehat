package cakehat.views.shared.gradingsheet;

import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.awt.Window;
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
    private final Map<Pair<Part, Group>, GradingSheetFrame> _openGradingSheetWindows =
            new HashMap<Pair<Part, Group>, GradingSheetFrame>();
    
    @Override
    public GradingSheet showWindow(Window owner, Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        final Pair<Part, Group> pair = Pair.of(part, group);
        
        GradingSheetFrame window = _openGradingSheetWindows.get(pair);
        if(window != null)
        {
            window.setLocationRelativeTo(owner);
            window.toFront();
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
            window = new GradingSheetFrame<GradableEventPanel>(owner, panel, title);
            _openGradingSheetWindows.put(pair, window);
            
            window.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosed(WindowEvent we)
                {
                    GradingSheetFrame window = _openGradingSheetWindows.remove(pair);
                    
                    //In some situations windowClosed(...) can be called twice when the window is closed - investigation
                    //into this indicates the cause is a Java/Swing bug
                    //To work around this issue, if the mapping no longer has a value because it was removed on the
                    //first call then do not call save() on the window
                    if(window != null)
                    {
                        window.save();
                    }
                }
            });
            
            window.setLocationRelativeTo(owner);
            window.setVisible(true);
        }
        
        return window;
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