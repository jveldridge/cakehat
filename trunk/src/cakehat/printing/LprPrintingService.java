package cakehat.printing;

import cakehat.Allocator;
import cakehat.CakehatSession;
import java.io.File;
import java.io.IOException;

/**
 * Uses lpr to print the PrintRequest in portrait.
 *
 * @author jak2
 */
public class LprPrintingService extends PrintingService
{
    @Override
    public void print(Iterable<PrintRequest> requests, PhysicalPrinter printer) throws IOException
    {
        //Build command
        String cmd = "lpr -P" + printer.getPrinterName();

	for(PrintRequest request : requests)
	{
            for(File file : request.getFiles())
            {
                cmd += " " + "'" + file.getAbsolutePath() + "'";
            }
	}

	//Execute command
        File workspace = Allocator.getPathServices().getUserWorkspaceDir();
        Allocator.getExternalProcessesUtilities().executeAsynchronously(cmd, workspace);

        //If in developer mode, print out the command so it can be verified as the developer will quite possibly not be
        //on a department machine where printing could actually occur
        if(CakehatSession.isDeveloperMode())
        {
            System.out.println("lpr command:");
            System.out.println(cmd);
        }
    }
}