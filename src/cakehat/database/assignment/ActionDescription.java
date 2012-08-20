package cakehat.database.assignment;

import com.google.common.collect.ImmutableList;
import java.util.List;
import support.resources.icons.IconLoader.IconImage;

/**
 *
 * @author jak2
 */
public class ActionDescription
{
    private final String _name;
    private final IconImage _icon;

    public static final ActionDescription
            RUN = new ActionDescription("Run", IconImage.GO_NEXT),
            DEMO = new ActionDescription("Demo", IconImage.APPLICATIONS_SYSTEM),
            GRADING_GUIDE = new ActionDescription("Grading Guide", IconImage.TEXT_X_GENERIC),
            TEST = new ActionDescription("Test", IconImage.UTILITIES_SYSTEM_MONITOR),
            OPEN = new ActionDescription("Open", IconImage.DOCUMENT_OPEN),
            README = new ActionDescription("Readme", IconImage.DOCUMENT_PROPERTIES),
            PRINT = new ActionDescription("Print", IconImage.PRINTER),
            CUSTOM = new ActionDescription("<Custom>", IconImage.SOFTWARE_UPDATE_AVAILABLE);
   
    private static final List<ActionDescription> DEFAULT_DESCRIPTIONS = ImmutableList.of(GRADING_GUIDE, OPEN, RUN,
            DEMO, TEST, README, PRINT, CUSTOM);
    
    public ActionDescription(String name, IconImage icon)
    {
        _name = name;
        _icon = icon;
    }

    public IconImage getIcon()
    {
        return _icon;
    }

    public String getName()
    {
        return _name;
    }
    
    public static List<ActionDescription> getDefaultDescriptions()
    {
        return DEFAULT_DESCRIPTIONS;
    }
    
    @Override
    public String toString()
    {
        return _name;
    }
}