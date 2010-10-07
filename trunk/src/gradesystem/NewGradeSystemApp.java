package gradesystem;

import backend.BackendView;
import frontend.FrontendView;
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
            FrontendView.launch();
        }
        else if(args[1].equalsIgnoreCase("backend"))
        {
            BackendView.launch();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Invalid run property: " + args[1]);
        }
    }
}