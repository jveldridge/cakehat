package config;

import matlab.MatlabClient;
import utils.*;
import java.util.Collection;
import matlab.SetupScriptWriter;

/**
 * Adds support for MATLAB student handins.
 *
 * @author spoletto
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
    public void openCode(String studentLogin)
    {
        MatlabClient c = setupClient(studentLogin);
        try
        {
            c.sendCommand("cd " + super.getStudentHandinDirectory(studentLogin));
            //get all .m files from current working directory
            //open up all .m files in MATLAB editor with 'edit' command
        }
        catch(Exception e) {
            new ErrorView(e, "Could not connect to MATLAB server. If you were " +
                    "running an active session of MATLAB before pressing the run " +
                    "button, please close MATLAB and try again");
        }
    }

    private Collection<String> getMFiles(String studentLogin, MatlabClient c)
    {
        //get the M files using 'what'
        return null;
    }

    private MatlabClient setupClient(String studentLogin)
    {
        if(!SetupScriptWriter.exists(studentLogin))
        {
            SetupScriptWriter.createScript(studentLogin);
        }
        super.untar(studentLogin);
        //ps -u graderlogin | grep matlab
        Collection<String> response = BashConsole.write("ps -u " +
                Allocator.getGeneralUtilities().getUserLogin() + " | grep matlab");
        if(response.isEmpty()) { //MATLAB is not currently running
            BashConsole.writeThreaded("cd " + Allocator.getCourseInfo().getGradingDir() +
                    "bin ; matlab -r setup");
        }
        //MATLAB is currently running; we want to tell it to 'cd'
        return new MatlabClient();
    }


    @Override //TODO: dynamically create setup.m script for use in
    //courses other than CS4
    public void run(String studentLogin)
    {
        MatlabClient c = setupClient(studentLogin);
        try
        {
            c.sendCommand("cd " + super.getStudentHandinDirectory(studentLogin));
            //get all .m files from current working directory
            //open up all .m files in MATLAB editor with 'edit' command
        }
        catch(Exception e) {
            new ErrorView(e, "Could not connect to MATLAB server. If you were " +
                    "running an active session of MATLAB before pressing the run " +
                    "button, please close MATLAB and try again");
        }
        //check if setup script exists within setupClient
        //using PrintWriter, write findMFiles function
        //
        
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