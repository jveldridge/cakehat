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
            BashConsole.write("matlab -r " + "/course/cs004/cakehattemp/setup");
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTester()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}