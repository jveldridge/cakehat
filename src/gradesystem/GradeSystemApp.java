package gradesystem;

import backend.BackendView;
import frontend.FrontendView;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class GradeSystemApp extends SingleFrameApplication
{
    private static String[] _args;
    private static final String BACKEND_ARG = "backend";
    private static final String FRONTEND_ARG = "frontend";

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup()
    {
        this.applyDisplaySettings();

        Runnable initialWindowCommand;
        if (inTestMode())
        {
            initialWindowCommand = new Runnable()
            {
                public void run()
                {
                    show(new GradeSystemView());
                }
            };
        }
        else if (_args[0].equalsIgnoreCase(BACKEND_ARG))
        {
            initialWindowCommand = new Runnable()
            {
                public void run()
                {
                    BackendView.launch();
                }
            };
        }
        else if (_args[0].equalsIgnoreCase(FRONTEND_ARG))
        {
            initialWindowCommand = new Runnable()
            {
                public void run()
                {
                    FrontendView.launch();
                }
            };
        }
        else
        {
            initialWindowCommand = new Runnable()
            {
                public void run()
                {
                    JOptionPane.showMessageDialog(null, "Invalid run arg: " + _args[0]);
                }
            };
        }

        if (isUsingSSH())
        {
            invokeAfterSwingLaunch(initialWindowCommand);
        }
        else
        {
            initialWindowCommand.run();
        }
    }

    /**
     * Invokes the <code>action</code> after Swing has initialized. Swing is
     * initialized by creating a small empty <code>JFrame</code> and then
     * disposing it after the action is invokes.
     *
     * NOTE: This is a hack to solve the SSH/Swing issue.
     *
     * @param action
     */
    private static void invokeAfterSwingLaunch(final Runnable action)
    {
        final JFrame loadingFrame = new JFrame("cakehat loading");

        loadingFrame.addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
                action.run();
                loadingFrame.dispose();
            }
        });

        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setVisible(true);
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
                    if(isUsingSSH())
                    {
                        System.setProperty("awt.useSystemAAFontSettings", "false");
                        System.setProperty("swing.aatext", "false");
                    }
                    else
                    {
                        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
                    }
                }
                //Setting the look and feel may fail, if so the program will most
                //likely still operate correctly but may display slightly oddly
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

    public static boolean isUsingSSH()
    {
        return _args.length >= 2 && _args[1].equalsIgnoreCase("ssh");
    }

    public static boolean inTestMode()
    {
        return _args.length == 0;
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