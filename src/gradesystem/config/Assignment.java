package gradesystem.config;

import java.util.Vector;
import gradesystem.utils.Allocator;

/**
 * A representation of an Assigment, composed of any number of non-handin parts,
 * any number of lab parts, and zero or one handin parts. This assignment has
 * a name, that must be unique, and a number that does not need to be unique. If
 * the number of this assignment is the same as that of another assignment then
 * those assignments will be considered alternatives of one another, e.g.
 * multiple options for a final project.
 *
 * @author jak2
 */
public class Assignment implements Comparable<Assignment>
{
    private String _name;
    private int _number;
    private Vector<NonHandinPart> _nonHandinParts = new Vector<NonHandinPart>();
    private Vector<LabPart> _labParts = new Vector<LabPart>();
    private HandinPart _handinPart;
    private Vector<Part> _allParts = null;

    Assignment(String name, int number)
    {
        _name = name;
        _number = number;
    }

    public String getName()
    {
        return _name;
    }

    /**
     * The number of this assignment. It does not need to be unique; assignments
     * with the same number are considered to be alternatives, e.g. multiple
     * options for a final project.
     *
     * @return
     */
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

    /**
     * Returns all Parts of this Assignment.
     * 
     * @return
     */
    public Iterable<Part> getParts()
    {
        //If this has not been created yet, build it
        if(_allParts == null)
        {
            _allParts = new Vector<Part>();

            _allParts.addAll(_labParts);
            _allParts.addAll(_nonHandinParts);
            if(this.hasHandinPart())
            {
                _allParts.add(_handinPart);
            }
        }

        return _allParts;
    }

    /**
     * Sums all of the point values for this Assignment's parts.
     *
     * @return
     */
    public int getTotalPoints()
    {
        int points = 0;

        for(Part part : this.getParts())
        {
            points += part.getPoints();
        }

        return points;
    }

    /**
     * Returns if this assignment has a unique assignment number.
     *
     * @return
     */
    public boolean isUnique()
    {
        for(Assignment asgn : Allocator.getCourseInfo().getAssignments())
        {
            if(asgn != this)
            {
                if(asgn.getNumber() == this.getNumber())
                {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return _name;
    }

    /**
     * Compares this assignment to another based on its assignment number.
     * 
     * @param a
     * @return
     */
    public int compareTo(Assignment a)
    {
        return ((Integer)this.getNumber()).compareTo(a.getNumber());
    }
}