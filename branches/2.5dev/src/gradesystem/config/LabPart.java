package gradesystem.config;

/**
 * Represents a lab part of an assignment. Can retrieve check off scores for a
 * lab.
 *
 * @author jak2
 */
public class LabPart extends Part
{
    private final int _labNumber;

    LabPart(Assignment asgn, String name, int number, int points, int labNumber)
    {
        super(asgn, name, number, points);

        _labNumber = labNumber;
    }

    /**
     * Lab number that is used when checking off a lab. Also used by this
     * class to retrieve the scores given.
     *
     * @return
     */
    public int getLabNumber()
    {
        return _labNumber;
    }
}