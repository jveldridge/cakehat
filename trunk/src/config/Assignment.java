package config;

import java.util.Vector;

/**
 *
 * @author jak2
 */
public class Assignment
{
    private String _name;
    private int _number;
    private Vector<NonHandinPart> _nonCodeParts = new Vector<NonHandinPart>();
    private Vector<LabPart> _labParts = new Vector<LabPart>();
    private HandinPart _handinPart;

    Assignment(String name, int number)
    {
        _name = name;
        _number = number;
    }

    public String getName()
    {
        return _name;
    }

    public int getNumber()
    {
        return _number;
    }

    // Parts

    void addNonCodePart(NonHandinPart part)
    {
        _nonCodeParts.add(part);
    }

    public Iterable<NonHandinPart> getNoncodeParts()
    {
        return _nonCodeParts;
    }

    public boolean hasNoncodeParts()
    {
        return !_nonCodeParts.isEmpty();
    }

    void addLabPart(LabPart part)
    {
        _labParts.add(part);
    }

    public Iterable<LabPart> getLabParts()
    {
        return _labParts;
    }

    public boolean hasLabParts()
    {
        return !_labParts.isEmpty();
    }

    void addHandinPart(HandinPart codePart)
    {
        _handinPart = codePart;
    }

    public HandinPart getHandinPart()
    {
        return _handinPart;
    }

    public boolean hasHandinPart()
    {
        return (_handinPart != null);
    }

    // Points

    public int getTotalPoints()
    {
        int points = 0;

        for(Part part : _nonCodeParts)
        {
            points += part.getPoints();
        }
        for(Part part : _labParts)
        {
            points += part.getPoints();
        }
        if(this.hasHandinPart())
        {
            points += _handinPart.getPoints();
        }

        return points;
    }

    @Override
    public String toString()
    {
        return _name;
    }
    
}