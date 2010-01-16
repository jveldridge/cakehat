package config;

/**
 *
 * @author jak2
 */
public class LabPart extends Part
{
    private int _labNumber;

    LabPart(Assignment asgn, String name, int points, int labNumber)
    {
        super(asgn, name, points);

        _labNumber = labNumber;
    }

    public int getLabNumber()
    {
        return _labNumber;
    }

    //TODO: Implement parsing of lab score / completion
    public double getScore(String studentLogin)
    {
        return 0;
    }
}