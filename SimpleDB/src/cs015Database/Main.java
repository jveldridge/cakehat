/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import GradingCommander.GradingCommanderGUI;
import cs015.tasupport.grading.config.ConfigurationManager;
import cs015.tasupport.utils.Utils;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.UIManager;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 *
 * @author Paul
 */
public class Main {

    public static void main(String[] args) {
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

            Font font = new Font("Sans-Serif", Font.PLAIN, 11);
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
        } catch (Exception e) {
        }
        try {
            DatabaseInterops.open();
        } catch (SqlJetException e) {
            e.printStackTrace();
            return;
        }

        if (args.length != 1) {
            System.out.println("Correct usage: 'frontend' or 'backend'");
        } else {
            if (args[0].compareToIgnoreCase("backend") == 0) {
                String u = Utils.getUserLogin();
                boolean found = false;
                for (String s : ConfigurationManager.getAdminLogins()) {
                    if (s.compareToIgnoreCase(u) == 0) {
                        StartupDialog sd = new StartupDialog();
                        sd.setVisible(true);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    System.out.println("You are not an authorized user.");
                    System.out.println("Authorized users are: " + Arrays.toString(ConfigurationManager.getAdminLogins()));
                }

            } else if (args[0].compareToIgnoreCase("frontend") == 0) {
                GradingCommanderGUI g = new GradingCommanderGUI();
                g.setVisible(true);
            } else {
                System.out.println("Correct usage: 'frontend' or 'backend'");
            }
        }

        try {
            DatabaseInterops.close();
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }
}
