/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs015Database;
import database_editor.*;
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
        //DatabaseGUI dg = new DatabaseGUI();

        try {
            DatabaseInterops.close();
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }
}
