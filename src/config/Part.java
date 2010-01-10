package config;

/**
 * A part of an assignment.
 *
 * @author jak2
 */
public abstract class Part
{
    private String _name;
    private int _points;
    private Assignment _asgn;

    protected Part(Assignment asgn, String name, int points)
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

    @Override
    public String toString()
    {
        return _name;
    }
}