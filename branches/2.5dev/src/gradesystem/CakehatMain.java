package gradesystem;

import gradesystem.labcheckoff.CheckoffCLI;
import gradesystem.views.backend.BackendView;
import gradesystem.views.frontend.FrontendView;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * The entry point for the cakehat grading system.
 *
 * @author jak2
 */
public class CakehatMain
{
    private static boolean _isSSHMode = false;
    private static boolean _isDeveloperMode = false;
    private static boolean _didStartNormally = false;
    private static CakehatRunMode _runMode = CakehatRunMode.UNKNOWN;

    /**
     * If the application is being run in SSH mode. This is no guarantee that
     * the application is actually being run over ssh.
     *
     * @return if in SSH mode
     */
    public static boolean isSSHMode()
    {
        return _isSSHMode;
    }

    /**
     * If the application was run in developer mode, meaning the developer was
     * able to select either the frontend or backend.
     *
     * @return
     */
    public static boolean isDeveloperMode()
    {
        return _isDeveloperMode;
    }

    /**
     * Whether or not this class's main method was run. This should be the case
     * during normal operation, but will not be the case during a test.
     * 
     * @return
     */
    public static boolean didStartNormally()
    {
        return _didStartNormally;
    }

    /**
     * The mode in which cakehat is running.
     *
     * @return
     */
    public static CakehatRunMode getRunMode()
    {
        return _runMode;
    }

    /**
     * Sets the mode that cakehat is running in. This is to be used
     * <strong>exclusively</strong> by {@link GradeSystemView} after the
     * developer has selected whether to launch the frontend or backend.
     *
     * @param mode
     */
    static void setRunMode(CakehatRunMode mode)
    {
        if(!isDeveloperMode())
        {
            throw new IllegalStateException("cakehat's run mode can only be " +
                    "set when running in developer mode");
        }

        _runMode = mode;
    }

    public static void main(String[] args)
    {
        _didStartNormally = true;
        
        CakehatUncaughtExceptionHandler.registerHandler();

        // Values from run arguments
        _isSSHMode = (args.length >= 2) && args[1].equalsIgnoreCase("ssh");
        _isDeveloperMode = (args.length == 0);

        // Appearance
        if(_isSSHMode)
        {
            System.setProperty("awt.useSystemAAFontSettings", "false");
            System.setProperty("swing.aatext", "false");
        }
        else
        {
            try
            {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            }
            //Depending on the windowing toolkit the user has this call may fail
            //but cakehat most likely will still appear similar enough to what
            //is intended to be functional
            catch(Exception e)
            {
                System.out.println("cakehat could not set its default appearance. " +
                        "Some interfaces may not appear as intended.");
            }
        }

        // Launch the appropriate view
        if(_isDeveloperMode)
        {
            DeveloperModeView.launch();
        }
        else if(args[0].equalsIgnoreCase("frontend"))
        {
            _runMode = CakehatRunMode.FRONTEND;
            FrontendView.launch();
        }
        else if(args[0].equalsIgnoreCase("backend"))
        {
            _runMode = CakehatRunMode.BACKEND;
            BackendView.launch();
        }
        else if(args[0].equalsIgnoreCase("lab"))
        {
            _runMode = CakehatRunMode.LAB;
            //Creating the ArrayList is necessary because the list created
            //by Arrays.asList(...) is immutable
            ArrayList<String> argList = new ArrayList(Arrays.asList(args));
            argList.remove(0);

            CheckoffCLI.performCheckoff(argList);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Invalid run property: " + args[0]);
        }
    }
}