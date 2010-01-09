/*
 * GradeSystemApp.java
 */
package gradesystem;

import backend.BackendView;
import frontend.FinalProjectFrontendView;
import frontend.FrontendView;
import java.awt.Font;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jvnet.substance.skin.SubstanceCremeLookAndFeel;

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
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    if(_testing){
                        System.setProperty("awt.useSystemAAFontSettings", "false");
                        System.setProperty("swing.aatext", "false");
                    }
                    else if ((_args != null && _args.length >= 2 && _args[1].compareToIgnoreCase("ssh") != 0) || _args == null || _args.length < 2) {
                        UIManager.setLookAndFeel(new SubstanceCremeLookAndFeel());

                        Font font = new Font("Deja Vu Sans", Font.TRUETYPE_FONT, 12);

                        UIManager.put("MenuItem.font", font);
                        UIManager.put("Menu.font", font);
                        UIManager.put("Button.font", font);
                        UIManager.put("ComboBox.font", font);
                        UIManager.put("CheckBox.font", font);
                        UIManager.put("Label.font", font);
                        UIManager.put("TabbedPane.font", font);
                        UIManager.put("TextField.font", font);
                        UIManager.put("List.font", font);
                        UIManager.put("RadioButton.font", font);
                        System.setProperty("awt.useSystemAAFontSettings", "true");
                        System.setProperty("swing.aatext", "true");
                    } else {
                        System.setProperty("awt.useSystemAAFontSettings", "false");
                        System.setProperty("swing.aatext", "false");
                    }
                } catch (Exception e) {
                }
            }
        });
        if (_args != null && _args.length >= 1) {
            if (_args[0].compareToIgnoreCase("backend") == 0) {
                BackendView bv = new BackendView();
                bv.setLocationRelativeTo(null);
                show(bv);
            } else if (_args[0].compareToIgnoreCase("frontend") == 0) {
                FrontendView fv = new FrontendView();
                fv.setLocationRelativeTo(null);
                show(fv);
            } else if(_args[0].compareToIgnoreCase("final_frontend") == 0) {
                FinalProjectFrontendView fpfv = new FinalProjectFrontendView();
                fpfv.setLocationRelativeTo(null);
                show(fpfv);
            }
            _testing = false;
        } else {
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
    }
}