/*
 * GradeSystemApp.java
 */
package gradesystem;

import backend.BackendView;
import frontend.FrontendView;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class GradeSystemApp extends SingleFrameApplication {

    private static String[] _args;
    public static boolean _testing; //I'm a sinner

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        setTheme();
        if(_args!=null && _args.length == 1) {
            if(_args[0].compareToIgnoreCase("backend") == 0) {
                BackendView bv = new BackendView();
                bv.setLocationRelativeTo(null);
                show(bv);
            } else if(_args[0].compareToIgnoreCase("frontend") == 0) {
                FrontendView fv = new FrontendView();
                fv.setLocationRelativeTo(null);
                show(fv);
            }
            _testing = false;
        }
        else{
            show(new GradeSystemView(this));
            _testing = true;
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of GradeSystemApp
     */
    public static GradeSystemApp getApplication() {
        return Application.getInstance(GradeSystemApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        _args = args;
        launch(GradeSystemApp.class, args);
    }

    private static void setTheme() {
        try {
            UIManager.LookAndFeelInfo plafinfo[] = UIManager.getInstalledLookAndFeels();
            boolean nimbusfound = false;
            int nimbusindex = 0;
            for (int look = 0; look < plafinfo.length; look++) {
                if (plafinfo[look].getClassName().toLowerCase().contains("nimbus")) {
                    nimbusfound = true;
                    nimbusindex = look;
                }
            }

            if (nimbusfound && !UIManager.getSystemLookAndFeelClassName().toLowerCase().contains("gtk") && !UIManager.getSystemLookAndFeelClassName().toLowerCase().contains("windows")) {
                UIManager.setLookAndFeel(plafinfo[nimbusindex].getClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

        } catch (Exception e) {
        }
    }
}
