package config;

import utils.*;
import java.util.Collection;

/**
 *
 * @author <author-here>
 */
class MatlabHandin extends CodeHandin
{
    private static final String[] _sourceFileTypes = { "m" };

    //TODO: Fill in my properties
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("Matlab", null, null, null);

    MatlabHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

    @Override
    public void run(String studentLogin)
    {
        //ps -u graderlogin | grep matlab
        Collection<String> response = BashConsole.write("ps -u " +
                Allocator.getGeneralUtilities().getUserLogin() + " | grep matlab");
        if(response.isEmpty()) { //MATLAB is not currently running
            BashConsole.write("cd /course/cs004/cakehat/bin ; matlab -r "
                    + "setup");
        }
        else { //MATLAB is currently running; we want to tell it to 'cd'

        }
    }

    @Override
    public boolean hasDemo()
    {
        return false;
    }

    @Override
    public void runDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTester()
    {
        return false;
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}