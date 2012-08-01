package cakehat.views.shared.gradingsheet;

import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author jak2
 */
public class GradingSheetManagerImpl implements GradingSheetManager
{
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
                ImmutableSet.copyOf(gradableEvent.getParts()), group, isAdmin, submitOnSave, false));
    }

    @Override
    public GradingSheet getGradingSheet(Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        return new WidthTrackingViewportPanel<PartPanel>(
                PartPanel.getPartPanel(part, group, isAdmin, submitOnSave, false));
    }
}