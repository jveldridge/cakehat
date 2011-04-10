package cakehat;

import cakehat.labcheckoff.CheckoffCLI;
import cakehat.views.admin.AdminView;
import cakehat.views.frontend.FrontendView;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import support.utils.posix.NativeException;

/**
 * The entry point for the cakehat grading system.
 *
 * @author jak2
 */
public class CakehatMain
{
    private static boolean _isDeveloperMode = false;
    private static boolean _didStartNormally = false;
    private static CakehatRunMode _runMode = CakehatRunMode.UNKNOWN;

    /**
     * If the application was run in developer mode, meaning the developer was
     * able to select either the frontend or admin.
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
     * <strong>exclusively</strong> by {@link DeveloperModeView} after the
     * developer has selected whether to launch the frontend or admin.
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

        // Launch the appropriate view
        if(args.length == 0)
        {
            _isDeveloperMode = true;
            applyLookAndFeel();
            adjustIfRemote();
            DeveloperModeView.launch();
        }
        else if(args[0].equalsIgnoreCase(CakehatRunMode.FRONTEND.getTerminalFlag()))
        {
            _runMode = CakehatRunMode.FRONTEND;
            applyLookAndFeel();
            adjustIfRemote();
            FrontendView.launch();
        }
        else if(args[0].equalsIgnoreCase(CakehatRunMode.ADMIN.getTerminalFlag()))
        {
            _runMode = CakehatRunMode.ADMIN;
            applyLookAndFeel();
            adjustIfRemote();
            AdminView.launch();
        }
        else if(args[0].equalsIgnoreCase(CakehatRunMode.LAB.getTerminalFlag()))
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
            System.out.println("Invalid run property: " + args[0]);
        }
    }

    private static void adjustIfRemote()
    {
        // Turn off anti-aliasing if running cakehat remotely (ssh)
        try
        {
            if(Allocator.getUserUtilities().isUserRemotelyConnected())
            {
                System.setProperty("awt.useSystemAAFontSettings", "false");
                System.setProperty("swing.aatext", "false");
            }
        }
        catch(NativeException e)
        {
            System.err.println("Unable to determine if you are remotely " +
                    "connected. cakehat will run as if you were running " +
                    "locally. Underlying cause: \n");
            e.printStackTrace();
        }
    }

    /**
     * This method should only be called from within this class or from the
     * test main methods. Applies the look and feel that cakehat uses, which
     * may differ from the default look and feel used by a given operating
     * system or Linux windowing toolkit.
     */
    public static void applyLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        // Depending on the windowing toolkit the user has, this call may fail
        // but cakehat most likely will still appear similar enough to what
        // is intended to be functional
        catch(Exception e)
        {
            System.err.println("cakehat could not set its default appearance. " +
                    "Some interfaces may not appear as intended.");
        }
    }
}