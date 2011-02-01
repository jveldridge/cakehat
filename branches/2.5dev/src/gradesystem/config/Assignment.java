package gradesystem.config;

import java.util.Vector;
import gradesystem.Allocator;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;

/**
 * A representation of an Assigment, composed of any number of non-handin parts,
 * any number of lab parts, any number of distributable parts and zero or one
 * handins. If there are any distributable parts then there will be a handin,
 * and if there is a handin there will be at least one distributle part. This
 * assignment has a name and number, that must both be unique.
 *
 * @author jak2
 */
public class Assignment implements Comparable<Assignment>
{
    private final String _name;
    private final int _number;
    private final Vector<NonHandinPart> _nonHandinParts = new Vector<NonHandinPart>();
    private final Vector<LabPart> _labParts = new Vector<LabPart>();
    private final Vector<DistributablePart> _distributableParts = new Vector<DistributablePart>();
    private final boolean _hasGroups;
    private Vector<Part> _allParts = null;
    private Handin _handin = null;

    /**
     * To be replaced by {@link #_handin} and {@link #_distributableParts}.
     * @deprecated
     */
    private HandinPart _handinPart;

    Assignment(String name, int number, boolean hasGroups)
    {
        _name = name;
        _number = number;
        _hasGroups = hasGroups;
    }

    public String getName()
    {
        return _name;
    }

    public String getDBID() {
        return _name;
    }

    /**
     * The number of this assignment. It must be unique.
     *
     * @return
     */
    public int getNumber()
    {
        return _number;
    }

    /**
     * Whether this assignment has groups or not.
     * <br/><br/>
     * From a code point of view many times there are implicit groups of one,
     * this is not what this method describes. Instead it is whether the course
     * will be specifying groups for this assignment.
     *
     * @return
     */
    public boolean hasGroups()
    {
        return _hasGroups;
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

    /**
     * @deprecated
     */
    public HandinPart getHandinPart()
    {
        return _handinPart;
    }

    /**
     * @deprecated
     */
    public boolean hasHandinPart()
    {
        return (_handinPart != null);
    }

    void addDistributablePart(DistributablePart part)
    {
        _distributableParts.add(part);
    }

    public Iterable<DistributablePart> getDistributableParts()
    {
        return _distributableParts;
    }

    public boolean hasDistributablePart()
    {
        return !_distributableParts.isEmpty();
    }

    void setHandin(Handin handin)
    {
        _handin = handin;
    }

    public Handin getHandin()
    {
        return _handin;
    }

    public boolean hasHandin()
    {
        return _handin != null;
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
            _allParts.addAll(_distributableParts);
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