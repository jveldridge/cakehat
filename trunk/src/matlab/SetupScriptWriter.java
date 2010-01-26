
package matlab;

import java.io.File;
import java.io.PrintWriter;
import utils.Allocator;
import utils.ErrorView;

/**
 * Since MATLAB requires the setup script to be run upon launch,
 * we must make sure it exists in the course directory. The 
 * SetupScriptWriter allows for checking to see if the setup.m
 * file exists and if not, to create it.
 *
 * @author spoletto
 */
public class SetupScriptWriter {

    public static void createScript()
    {
        String path = Allocator.getCourseInfo().getGradingDir() + "bin/";
        File setupScript = new File(path + "setup.m");
        PrintWriter printer = null;
        try
        {
            printer = new PrintWriter(setupScript);
            printer.println("javaaddpath " + path);
            printer.println("server = MatlabServer;");

        }
        catch(Exception e)
        {
            new ErrorView(e, "Could not create setup.m file");
        }
        finally
        {
            if(printer != null)
            {
                printer.close();
            }
        }
    }

    public static boolean exists()
    {
           File f = new File(Allocator.getCourseInfo().getGradingDir()
                   + "bin/setup.m");
           return f.exists();
    }

}
