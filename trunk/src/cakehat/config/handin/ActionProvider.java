package cakehat.config.handin;

import java.util.List;

/**
 * A provider of {@link DistributableActionDescription}s.
 *
 * @author jak2
 */
@Deprecated
interface ActionProvider
{
    /**
     * This name should describe the types of actions provided, such as "java"
     * or "matlab". It will become the prefix for the names of the descriptions
     * provided.
     * 
     * @return
     */
    public String getNamespace();

    /**
     * The {@link DistributableActionDescription}s provided by this
     * ActionProvider.
     *
     * @return
     */
    public List<DistributableActionDescription> getActionDescriptions();
}