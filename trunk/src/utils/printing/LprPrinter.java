/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils.printing;

import java.io.File;
import utils.BashConsole;

/**
 * Uses lpr to print the PrintRequest in portrait.
 *
 * @author jak2
 */
public class LprPrinter extends Printer
{
    public void print(Iterable<PrintRequest> requests, String printer)
    {
        //Build command
        String cmd = "lpr -P" + printer + " ";

	for(PrintRequest request : requests)
	{
            for(File file : request.getFiles())
            {
                cmd += file.getAbsolutePath() + " ";
            }
	}

        //Testing
        System.out.println("lpr Command:");
        System.out.println(cmd);

	//Execute command
	BashConsole.writeThreaded(cmd);
    }
}