package config;

/**
 *
 * @author jak2
 */
public abstract class Part
{
    private String _name;
    private int _points;
    private Assignment _asgn;

    public Part(Assignment asgn, String name, int points)
    {
        _asgn = asgn;
        _name = name;
        _points = points;
    }

    public Assignment getAssignment()
    {
        return _asgn;
    }

    public String getName()
    {
        return _name;
    }

    public int getPoints()
    {
        return _points;
    }

}