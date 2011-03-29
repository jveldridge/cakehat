package gradesystem.rubric;

import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;

/**
 * Implement this interface to be notified of rubric saving.
 *
 * @author jak2
 */
public interface RubricSaveListener
{
    public void rubricSaved(DistributablePart part, Group group);
}