package gradesystem.handin;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Consider if this class would really be better served by an external
 * script or two. Compiling a single .c file without linking against any libraries
 * (except the standard lib), seems remarkably specific.
 *
 * @author jak2
 */
class CActions implements ActionProvider
{
    public String getNamespace()
    {
        return "c";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();

        return descriptions;
    }
}