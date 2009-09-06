/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledb;

import java.awt.Toolkit;
import javax.swing.UIManager;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 *
 * @author psastras
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        Toolkit.getDefaultToolkit().beep();
//        System.out.print("\007");
//        System.out.flush();
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

        DatabaseGUI dg = new DatabaseGUI();




        try {
            DatabaseInterops.close();
        } catch (SqlJetException e) {
            e.printStackTrace();
        }

    }
}
