/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simpledb;

import java.util.Arrays;
import javax.swing.UIManager;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 *
 * @author psastras
 */
public class Main {

    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(Exception e) {}
        try{
            TADB.open();
        }
        catch(SqlJetException e) {
            e.printStackTrace();
            return;
        }

        DatabaseGUI dg = new DatabaseGUI();

        


        try{
            TADB.close();
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
        
    }

}
