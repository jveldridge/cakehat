package config;

/**
 *
 * @author jak2
 */
public class LabPart extends Part
{
    private int _labNumber;

    public LabPart(Assignment asgn, String name, int points, int labNumber)
    {
        super(asgn, name, points);

        _labNumber = labNumber;
    }

    //TODO: Implement parsing of lab score / completion
    public int getScore(String studentLogin)
    {
        return 0;
    }
}