package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExternalProcessesUtilitiesImpl implements ExternalProcessesUtilities
{
    public void executeInVisibleTerminal(String title, String cmd) throws IOException
    {
        String terminalCmd = "/usr/bin/xterm -fg black -bg gray" +
                " -title " + "\"" + title + "\"" +
                " -e " + "\"" + cmd + "; bash" + "\"";

        this.executeAsynchronously(terminalCmd);
    }

    public void executeAsynchronously (String cmd) throws IOException
    {
        this.executeAsynchronously(Arrays.asList(new String[] { cmd }));
    }

    public void executeAsynchronously(Iterable<String> cmds) throws IOException
    {
        File wd = new File("/bin");
	Process proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
        if (proc == null)
        {
            throw new IOException("Unable to find bash at /bin/bash");
        }
        else
        {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for(String line : cmds)
            {
                out.println(line);
            }
            out.println("exit");
            out.close();
	}
    }

    public ProcessResponse executeSynchronously(String cmd) throws IOException
    {
        return this.executeSynchronously(Arrays.asList(new String[] { cmd }));
    }

    public ProcessResponse executeSynchronously(Iterable<String> cmds) throws IOException
    {
        List<String> normalOutput = new ArrayList<String>();
        List<String> errorOutput = new ArrayList<String>();

	File wd = new File("/bin");
	Process proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
        if (proc == null)
        {
            throw new IOException("Unable to find bash at /bin/bash");
        }
        else
        {
            //This is slightly confusing:
            //in  - the output stream of the bash console
            //out - what we are going to write to the console (using the cmds parameter passed in)
            //err - the error stream of the bash console
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for(String line : cmds)
            {
                out.println(line);
            }
            out.println("exit");

            //Read out response
            String line;
            while ((line = in.readLine()) != null)
            {
                normalOutput.add(line);
            }
            while ((line = err.readLine()) != null)
            {
                errorOutput.add(line);
            }

            //Wait for process to complete
            try
            {
                proc.waitFor();
            }
            catch (InterruptedException ex)
            {
                throw new IOException("Unable to wait for external process to finish", ex);
            }

            //Shut everything down
            in.close();
            err.close();
            out.close();
            proc.destroy();
	}

	return new ProcessResponse(normalOutput, errorOutput);
    }
}