package cakehat.database.assignment;

import cakehat.database.Group;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;

/**
 * An {@code Assignment} is a collection of {@link GradableEvent}s. {@link Group}s belong to a given {@code Assignment}.
 * Conceptually an assignment is one or more gradable events which include, but are not limited, to paper handins,
 * digital handins, labs, design checks, and exams done by groups of one or more people. (Technically an assignment may
 * have zero {@link GradableEvent}s that belong to it.) An {@code Assignment} is designed to conceptually align with
 * what courses consider to be an assignment, but there is no requirement a course have a 1:1 correspondence between
 * {@code Assignment}s and actual assignments.
 * <br/><br/>
 * A course may specify any number of assignments. Each assignment will be represented as a single instance of this
 * class in memory. Each assignment has a unique identifier that will remain stable regardless of changes to other
 * properties.
 *
 * @author jak2
 */
public class Assignment implements Comparable<Assignment>, Iterable<GradableEvent>
{
    private final int _id;
    private final String _name;
    private final int _order;
    private final boolean _hasGroups;
    
    private final ImmutableList<GradableEvent> _gradableEvents;
    
    /**
     * Constructs an Assignment.
     * 
     * @param id unique identifier for this Assignment relative to all other Assignments, stable regardless of changes
     * @param name human readable name of this Assignment, may not be {@code null}
     * @param order relative order of this Assignment to other Assignments, must be unique
     * @param hasGroups whether this Assignment has groups that will be specified by the course staff
     * @param gradableEvents may not be {@code null}
     */
    Assignment(int id, String name, int order, boolean hasGroups, List<GradableEvent> gradableEvents)
    {
        //Validation
        if(name == null)
        {
            throw new NullPointerException("name may not be null");
        }
        if(gradableEvents == null)
        {
            throw new NullPointerException("gradableEvents may not be null");
        }
        
        _id = id;
        _name = name;
        _order = order;
        _hasGroups = hasGroups;
        
        _gradableEvents = ImmutableList.copyOf(gradableEvents);
    }

    /**
     * A unique identifier for this assignment.
     * 
     * @return
     */
    public int getId()
    {
        return _id;
    }
    
    /**
     * The name of the this assignment.
     *
     * @return
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Whether this assignment has groups or not.
     * <br/><br/>
     * From a code point of view many times there are implicit groups of one, this is not what this method describes.
     * Instead it is whether the course will be specifying groups for this assignment.
     *
     * @return
     */
    public boolean hasGroups()
    {
        return _hasGroups;
    }

    /**
     * Returns an immutable list of all of the {@code GradableEvent}s that belong to this assignment. 
     * 
     * @return 
     */
    public List<GradableEvent> getGradableEvents()
    {
        return _gradableEvents;
    }
    
    /**
     * Returns the name of the assignment.
     *
     * @return
     */
    @Override
    public String toString()
    {
        return _name;
    }

    /**
     * Compares this assignment to another based on its ordering.
     * 
     * @param a
     * @return
     */
    @Override
    public int compareTo(Assignment a)
    {
        return ((Integer)this._order).compareTo(a._order);
    }

    @Override
    public Iterator<GradableEvent> iterator()
    {
        return this.getGradableEvents().iterator();
    }
}