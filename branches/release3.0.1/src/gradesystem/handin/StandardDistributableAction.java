package gradesystem.handin;

import gradesystem.database.Group;
import java.util.Collection;

/**
 * A convenience partial implementation of DistributableAction which always
 * throws an exception if the {@link #performAction(gradesystem.handin.DistributablePart, java.util.Collection) }
 * method is invoked. This class is intended to be used to simplify creating
 * DistributableAction for RUN, TEST, DEMO and OPEN modes as only the PRINT
 * mode needs to be able to take in multiple groups.
 *
 * @author jak2
 */
abstract class StandardDistributableAction implements DistributableAction
{
    public void performAction(DistributablePart part, Collection<Group> groups) throws ActionException
    {
        throw new ActionException("This type of action is only intended for printing");
    }
}