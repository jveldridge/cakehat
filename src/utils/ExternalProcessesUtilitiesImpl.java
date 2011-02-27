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
    public void executeInVisibleTerminal(String title, String cmd, File dir) throws IOException
    {
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("xterm");
        arguments.add("-fg");
        arguments.add("black");
        arguments.add("-bg");
        arguments.add("gray");
        arguments.add("-title");
        arguments.add(title);

        //If there is a command, execute it, and then afterwards return to a
        //bash shell
        if(cmd != null)
        {
            arguments.add("-e");
            arguments.add(cmd + "; bash");
        }

        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.directory(dir);

        builder.start();
    }

    public void executeAsynchronously(String cmd) throws IOException
    {
        this.executeAsynchronously(cmd, new File("/bin"));
    }

    public void executeAsynchronously(String cmd, File dir) throws IOException
    {
        this.executeAsynchronously(Arrays.asList(new String[] { cmd }), dir);
    }

    public void executeAsynchronously(Iterable<String> cmds, File dir) throws IOException
    {
	Process proc = Runtime.getRuntime().exec("/bin/bash", null, dir);
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
    
    public ProcessResponse executeSynchronously(String cmd, File dir) throws IOException
    {
        return this.executeSynchronously(Arrays.asList(new String[] { cmd }), dir);
    }

    public ProcessResponse executeSynchronously(Iterable<String> cmds, File dir) throws IOException
    {
        List<String> normalOutput = new ArrayList<String>();
        List<String> errorOutput = new ArrayList<String>();

	Process proc = Runtime.getRuntime().exec("/bin/bash", null, dir);
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