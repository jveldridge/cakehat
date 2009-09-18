/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;

import java.awt.Font;
import javax.swing.UIManager;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 *
 * @author Paul
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
        StartupDialog sd = new StartupDialog();
        sd.setVisible(true);

        try {
            DatabaseInterops.close();
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }
}
