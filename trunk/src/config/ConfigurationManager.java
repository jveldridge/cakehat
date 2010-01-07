package config;

import utils.ErrorView;

/**
 *
 * @author jak2
 */
public class ConfigurationManager
{
    private static Configuration _config;

    private static Configuration getConfiguration()
    {
        if(_config == null)
        {
            try
            {
                _config = ConfigurationParser.parse();
            }
            catch (ConfigurationException ex)
            {
                new ErrorView(ex);
            }
        }
        
        return _config;
    }

    public static void main(String[] args)
    {
        Configuration config = getConfiguration();

        for(Assignment asgn : config.getAssigments())
        {
            System.out.println(asgn.getName());
        }
    }

    public static Iterable<Assignment> getAssignments()
    {
        return getConfiguration().getAssigments();
    }

    public static Iterable<TA> getTAs()
    {
        return getConfiguration().getTAs();
    }
}