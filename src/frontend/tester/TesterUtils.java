/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.tester;

/**
 *
 * @author spoletto
 */
public class TesterUtils {

    public static String getTesterName(String asgn)
    {
        if(asgn.equals("Cartoon") || asgn.equals("Swarm") || asgn.equals("Tetris"))
        {
            return "gfx";
        }
        else
        {
            return asgn;
        }

    }

}
