package gradesystem;

import backend.BackendView;
import frontend.FrontendView;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class GradeSystemApp extends SingleFrameApplication
{
    private static String[] _args;
    private static boolean testing;

    public static boolean inTestMode() {
        return testing;
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup()
    {
        this.applyDisplaySettings();

        if (_args.length >= 1)
        {
            if (_args[0].compareToIgnoreCase("backend") == 0)
            {
                BackendView.launch();
            }
            else if (_args[0].compareToIgnoreCase("frontend") == 0)
            {
                FrontendView.launch();
            }
            testing = false;
        }
        else
        {
            testing = true;
            show(new GradeSystemView(this));
        }

    }

    /**
     * Applies appropriate fonts and anti-aliasing depending on the command
     * line arguements.
     */
    private void applyDisplaySettings()
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    //If not ssh
                    if ( (_args.length >= 2 && _args[1].compareToIgnoreCase("ssh") != 0)
                              || _args.length < 2)
                    {
                        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
                    }
                    //If ssh
                    else
                    {
                        System.setProperty("awt.useSystemAAFontSettings", "false");
                        System.setProperty("swing.aatext", "false");
                    }
                }
                catch (Exception e) { }
            }
        });
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {}

    /**
     * A convenient static getter for the application instance.
     * @return the instance of GradeSystemApp
     */
    public static GradeSystemApp getApplication()
    {
        return Application.getInstance(GradeSystemApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args)
    {
        _args = args;
        launch(GradeSystemApp.class, args);
    }
}