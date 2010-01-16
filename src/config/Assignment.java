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
    private Vector<NonHandinPart> _nonHandinParts = new Vector<NonHandinPart>();
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

    void addNonHandinPart(NonHandinPart part)
    {
        _nonHandinParts.add(part);
    }

    public Iterable<NonHandinPart> getNonHandinParts()
    {
        return _nonHandinParts;
    }

    public boolean hasNonHandinParts()
    {
        return !_nonHandinParts.isEmpty();
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

    public Iterable<Part> getParts()
    {
        Vector<Part> parts = new Vector<Part>();

        parts.addAll(_labParts);
        parts.addAll(_nonHandinParts);
        if(this.hasHandinPart())
        {
            parts.add(_handinPart);
        }

        return parts;
    }

    // Points

    public int getTotalPoints()
    {
        int points = 0;

        for(Part part : _nonHandinParts)
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