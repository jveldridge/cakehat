package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;

/**
 *
 * @author jak2
 */
public interface GradingSheetManager
{
    /**
     * Shows a visualization for the specified part and group hosted in a frame.
     * 
     * @param part
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin 
     * @return
     */
    public GradingSheet showFrame(Part part, Group group, boolean isAdmin);
    
    /**
     * Shows a visualization for the specified assignment and group hosted in a panel.
     * 
     * @param asgn 
     * @param group may be {@code null}, a template view will be shown
     * @return 
     */
    public GradingSheet getGradingSheet(Assignment asgn, Group group, boolean isAdmin);
    
    /**
     * Shows a visualization for the specified gradable event and group hosted in a panel.
     * 
     * @param gradableEvent
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin
     * @return 
     */
    public GradingSheet getGradingSheet(GradableEvent gradableEvent, Group group, boolean isAdmin);
    
    /**
     * Shows a visualization for the specified part and group hosted in a panel.
     * 
     * @param part
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin
     * @return 
     */
    public GradingSheet getGradingSheet(Part part, Group group, boolean isAdmin);
}