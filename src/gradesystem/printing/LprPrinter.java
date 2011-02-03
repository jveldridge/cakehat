package gradesystem.printing;

import gradesystem.Allocator;
import gradesystem.GradeSystemApp;
import java.io.File;
import java.io.IOException;

/**
 * Uses lpr to print the PrintRequest in portrait.
 *
 * @author jak2
 */
public class LprPrinter extends Printer
{
    public void print(Iterable<PrintRequest> requests, String printer) throws IOException
    {
        //Build command
        String cmd = "lpr -P" + printer;

	for(PrintRequest request : requests)
	{
            for(File file : request.getFiles())
            {
                cmd += " " + "'" + file.getAbsolutePath() + "'";
            }
	}

        if (GradeSystemApp.inTestMode())
        {
            //Testing
            System.out.println("lpr Command:");
            System.out.println(cmd);
        }

	//Execute command
        Allocator.getExternalProcessesUtilities().executeAsynchronously(cmd);
    }
}