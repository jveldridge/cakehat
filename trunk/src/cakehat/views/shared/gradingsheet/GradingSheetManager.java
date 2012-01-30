package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;
import javax.swing.JComponent;

/**
 *
 * @author jak2
 */
public interface GradingSheetManager
{
    /**
     * Shows a visualization for the specified part and group.
     * 
     * @param part
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin 
     * @return
     */
    public GradingSheet showFrame(Part part, Group group, boolean isAdmin);
    
    /**
     * 
     * @param <T>
     * @param asgn 
     * @param group may be {@code null}, a template view will be shown
     * @return 
     */
    public <T extends JComponent & GradingSheet> T getGradingSheet(Assignment asgn, Group group, boolean isAdmin);
    
    /**
     * 
     * @param <T>
     * @param gradableEvent
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin
     * @return 
     */
    public <T extends JComponent & GradingSheet> T getGradingSheet(GradableEvent gradableEvent, Group group, boolean isAdmin);
    
    /**
     * 
     * @param <T>
     * @param part
     * @param group may be {@code null}, a template view will be shown
     * @param isAdmin
     * @return 
     */
    public <T extends JComponent & GradingSheet> T getGradingSheet(Part part, Group group, boolean isAdmin);
}