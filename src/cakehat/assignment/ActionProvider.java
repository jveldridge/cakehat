package cakehat.assignment;

import java.util.List;

/**
 * A provider of {@link PartActionDescription}s.
 *
 * @author jak2
 */
interface ActionProvider
{
    /**
     * This name should describe the types of actions provided, such as "java" or "matlab". It will become the prefix
     * for the names of the descriptions provided.
     * 
     * @return
     */
    public String getNamespace();

    /**
     * The {@link PartActionDescription}s provided by this ActionProvider.
     *
     * @return
     */
    public List<PartActionDescription> getActionDescriptions();
}