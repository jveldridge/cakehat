package cakehat.views.shared.gradingsheet;

import cakehat.database.Group;
import cakehat.database.assignment.Part;
import java.awt.Component;
import java.util.Collections;
import java.util.List;

/**
 * A panel which serves to only show the header information for a {@link Part}.
 *
 * @author jak2
 */
class PartInfoPanel extends PartPanel
{
    PartInfoPanel(Part part, Group group, boolean showBorder)
    {
        super(part, group, false, showBorder);
    }

    @Override
    List<Component> getFocusableComponents()
    {
        return Collections.<Component>emptyList();
    }

    @Override
    public double getEarned()
    {
        return 0;
    }

    @Override
    public void save() { }
}