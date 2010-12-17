package gradesystem;

import gradesystem.views.backend.BackendView;
import gradesystem.views.frontend.FrontendView;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * THIS CODE CANNOT BECOME THE NEW ENTRY POINT UNTIL *ALL* FORM BASED CLASSES
 * HAVE BEEN REMOVED.
 *
 * The entry point for the cakehat grading system.
 *
 * @author jak2
 */
public class NewGradeSystemApp
{
    private static boolean _isSSHMode;
    private static boolean _isTestMode;

    //package-protected so can be set by GradeSystemView when in test mode.
    static boolean _isFrontend;
    static boolean _isBackend;

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
     * If the application is being run in test mode.
     *
     * @return if in test mode
     */
    public static boolean inTestMode()
    {
        return _isTestMode;
    }

    /**
     * Returns whether or not the application is running the frontend (grader)
     * interface.
     *
     * @return true if running frontend; false otherwise
     */
    public static boolean isFrontend()
    {
        return _isFrontend;
    }

    /**
     * Returns whether or not the application is running the backend (admin)
     * interface.
     *
     * @return true if running backend; false otherwise
     */
    public static boolean isBackend()
    {
        return _isBackend;
    }

    public static void main(String[] args)
    {
        // Values from run arguments
        _isSSHMode = args.length >= 2 && args[1].equalsIgnoreCase("ssh");
        _isTestMode = (args.length == 0);

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
                UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            }
            catch(Exception e) {}
        }

        // Launch the appropriate view
        if(_isTestMode)
        {
            GradeSystemView.launch();
        }
        else if(args[1].equalsIgnoreCase("frontend"))
        {
            _isFrontend = true;
            FrontendView.launch();
        }
        else if(args[1].equalsIgnoreCase("backend"))
        {
            _isBackend = true;
            BackendView.launch();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Invalid run property: " + args[1]);
        }
    }
}