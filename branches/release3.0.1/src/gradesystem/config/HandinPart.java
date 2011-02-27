package gradesystem.config;

import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;

/**
 *
 * @deprecated Replaced by {@link Handin} and {@link DistributablePart}.
 * @author jak2
 */
public abstract class HandinPart extends Part
{
    HandinPart(Assignment asgn, String name, int points)
    {
        super(asgn, name, Integer.MIN_VALUE, points);
    }

    /**
     * Checks whether or not a student login has a handin for this project.
     *
     * @param studentLogin
     * @return if student has handin for this project
     */
    public boolean hasHandin(String studentLogin)
    {
        throw new UnsupportedOperationException("Deprecated method");
    }
}