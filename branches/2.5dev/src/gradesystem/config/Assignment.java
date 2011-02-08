package gradesystem.config;

import com.google.common.collect.ImmutableList;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import java.util.List;

/**
 * A representation of an assigment, composed of any number of non-handin parts,
 * any number of lab parts, any number of distributable parts and zero or one
 * handins. If there are any distributable parts then there will be a handin,
 * and if there is a handin there will be at least one distributable part. This
 * assignment has a name and number, both of which are unique.
 *
 * @author jak2
 */
public class Assignment implements Comparable<Assignment>
{
    private final String _name;
    private final int _number;
    private final boolean _hasGroups;

    private final ImmutableList.Builder<NonHandinPart> _nonHandinBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<LabPart> _labBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<DistributablePart> _distributableBuilder = ImmutableList.builder();
    
    private Handin _handin = null;

    Assignment(String name, int number, boolean hasGroups)
    {
        _name = name;
        _number = number;
        _hasGroups = hasGroups;
    }

    /**
     * The name of the this assignment. It is unique.
     *
     * @return
     */
    public String getName()
    {
        return _name;
    }

    /**
     * The number of this assignment. It is unique.
     *
     * @return
     */
    public int getNumber()
    {
        return _number;
    }

    /**
     * The String used by the database that identifies this assignment.
     * @return
     */
    public String getDBID()
    {
        return _name;
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
        _nonHandinBuilder.add(part);
    }

    private List<NonHandinPart> _nonHandinParts;
    public List<NonHandinPart> getNonHandinParts()
    {
        if(_nonHandinParts == null)
        {
            _nonHandinParts = _nonHandinBuilder.build();
        }

        return _nonHandinParts;
    }

    public boolean hasNonHandinParts()
    {
        return !getNonHandinParts().isEmpty();
    }

    void addLabPart(LabPart part)
    {
        _labBuilder.add(part);
    }

    private List<LabPart> _labParts;
    public List<LabPart> getLabParts()
    {
        if(_labParts == null)
        {
            _labParts = _labBuilder.build();
        }

        return _labParts;
    }

    public boolean hasLabParts()
    {
        return !getLabParts().isEmpty();
    }

    void addDistributablePart(DistributablePart part)
    {
        _distributableBuilder.add(part);
    }

    private List<DistributablePart> _distributableParts;
    public List<DistributablePart> getDistributableParts()
    {
        if(_distributableParts == null)
        {
            _distributableParts = _distributableBuilder.build();
        }

        return _distributableParts;
    }

    public boolean hasDistributableParts()
    {
        return !getDistributableParts().isEmpty();
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

    private List<Part> _allParts;
    /**
     * Returns all Parts of this Assignment.
     * 
     * @return
     */
    public List<Part> getParts()
    {
        //If this has not been created yet, build it
        if(_allParts == null)
        {
            ImmutableList.Builder<Part> builder = ImmutableList.builder();

            builder.addAll(getLabParts());
            builder.addAll(getNonHandinParts());
            builder.addAll(getDistributableParts());

            _allParts = builder.build();
        }

        return _allParts;
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

    /**
     * Sums all of the point values for this Assignment's parts.
     *
     * @deprecated This method is no longer accurate because parts of an
     * assignment with the same number are considered equivalent and are not
     * required to have the same point value. Currently this method totals up
     * the points for <b>all parts</b> - which is absolutely an incorrect
     * behavior - but there is no possible correct behavior without specifying
     * which parts to use when there are multiple with the same part number.
     * @return
     */
    @Deprecated
    public int getTotalPoints()
    {
        int points = 0;

        for(Part part : this.getParts())
        {
            points += part.getPoints();
        }

        return points;
    }

    @Deprecated
    public HandinPart getHandinPart()
    {
        throw new UnsupportedOperationException("This method is deprecated");
    }
}