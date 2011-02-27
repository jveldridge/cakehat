package gradesystem.config;

/**
 * A part of an {@link Assignment}.
 *
 * @author jak2
 */
public abstract class Part implements Comparable<Part>
{
    private final String _name;
    private final int _points;
    private final Assignment _asgn;
    private final int _number;

    protected Part(Assignment asgn, String name, int number, int points)
    {
        _asgn = asgn;
        _name = name;
        _points = points;
        _number = number;
    }

    public Assignment getAssignment()
    {
        return _asgn;
    }

    public String getName()
    {
        return _name;
    }

    public String getDBID()
    {
        return _asgn.getName() + "-" + _name;
    }

    public int getPoints()
    {
        return _points;
    }

    public int getNumber()
    {
        return _number;
    }

    @Override
    public String toString()
    {
        return _name;
    }

    /**
     * Comparison based on their {@link Assignment}s, {@link #getNumber()}, and
     * {@link #getName()} in that order.
     *
     * @param p
     * @return
     */
    public int compareTo(Part p)
    {
        int comparison = this.getAssignment().compareTo(p.getAssignment());

        if(comparison == 0)
        {
            comparison = ((Integer) this.getNumber()).compareTo(p.getNumber());
        }

        if(comparison == 0)
        {
            comparison = this.getName().compareTo(p.getName());
        }

        return comparison;
    }
}