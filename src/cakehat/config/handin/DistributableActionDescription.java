package cakehat.config.handin;

import java.util.List;
import java.util.Map;

/**
 * Describes an action that will be used for running, testing, demoing, opening,
 * and printing;
 *
 * @author jak2
 */
@Deprecated
public interface DistributableActionDescription
{
    public static enum ActionMode
    {
        RUN, OPEN, TEST, DEMO, PRINT;
    }

    /**
     * The provider this action description belongs to.
     * 
     * @return
     */
    public ActionProvider getProvider();

    /**
     * The name of the action.
     *
     * @return
     */
    public String getName();

    /**
     * A human readable description of the action. This description will be
     * displayed to users of cakehat when setting up the configuration file.
     *
     * @return
     */
    public String getDescription();

    /**
     * Returns the properties this action either requires or optionally allows.
     * 
     * @return
     */
    public List<DistributableActionProperty> getProperties();

    /**
     * Modes are RUN, OPEN, TEST, DEMO & PRINT.
     * <br/><br/>
     * Generally speaking actions may be used interchangeably. For instance a
     * mode that opens files in Kate would have a suggested mode of OPEN, but
     * could be configured to be the RUN mode. The modes returned by this method
     * are only suggested uses.
     * <br/><br/>
     * There are some fundamental limitations, so not all actions can be used
     * for all modes. Consult {@link #getCompatibleModes()} for this.
     *
     * @return
     */
    public List<ActionMode> getSuggestedModes();

    /**
     * Modes are RUN, OPEN, TEST, DEMO & PRINT.
     * <br/><br/>
     * Running, opening, and testing code works for one group at a time. Demos
     * operate on no groups. Printing operates on any number of groups at once.
     * Due to these differences not all {@link DistributableAction}s can be used
     * for all modes.
     *
     * @return
     */
    public List<ActionMode> getCompatibleModes();

    /**
     * Returns an implementation described by this action that has the property
     * values provided.
     *
     * @param properties
     * @return
     */
    public DistributableAction getAction(Map<DistributableActionProperty, String> properties);
}