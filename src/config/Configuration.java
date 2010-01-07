package config;

import java.util.Vector;

/**
 * Object representation of the XML config file. Only meant to be used by
 * ConfigurationParser and ConfigurationManager.
 *
 * @author jak2
 */
class Configuration
{
    private Vector<Assignment> _assignments = new Vector<Assignment>();
    private Vector<TA> _tas = new Vector<TA>();

    Iterable<Assignment> getAssigments()
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

    Iterable<TA> getTAs()
    {
        return _tas;
    }
}
