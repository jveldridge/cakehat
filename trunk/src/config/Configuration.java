package config;

import java.util.Vector;

/**
 *
 * @author jak2
 */
class Configuration
{
    private Vector<Assignment> _assignments = new Vector<Assignment>();
    private Vector<TA> _tas = new Vector<TA>();

    Vector<Assignment> getAssigments()
    {
        return _assignments;
    }
    
    void addAssignment(Assignment asgn)
    {
        _assignments.add(asgn);
    }
    

    void addTA(TA ta)
    {
        _tas.add(ta);
    }

    Vector<TA> getTAs()
    {
        return _tas;
    }
}
