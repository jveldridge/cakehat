package cakehat.assignment;

import java.util.Set;

/**
 * A provider of {@link PartActionDescription}s.
 *
 * @author jak2
 */
interface ActionProvider
{
    /**
     * This name should describe the types of actions provided, such as "java" or "matlab". It will become the prefix
     * for the names of the action descriptions provided.
     * 
     * @return
     */
    public String getNamespace();

    /**
     * The {@link PartActionDescription}s provided by this ActionProvider.
     *
     * @return
     */
    public Set<? extends PartActionDescription> getActionDescriptions();
}