package cakehat.views.shared.gradingsheet;

import cakehat.database.Group;
import cakehat.database.assignment.Part;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;

/**
 * A panel which serves to only show the header information for a {@link Part}.
 *
 * @author jak2
 */
class PartInfoPanel extends PartPanel
{
    PartInfoPanel(Part part, Group group)
    {
        super(part, group, false);
    }

    @Override
    List<JComponent> getFocusableComponents()
    {
        return Collections.<JComponent>emptyList();
    }

    @Override
    public double getEarned()
    {
        return 0;
    }

    @Override
    public void save() { }
}