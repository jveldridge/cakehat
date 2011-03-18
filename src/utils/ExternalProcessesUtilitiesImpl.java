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
    
    /* See the javadoc comment in the interface for the larger picture, the
     * following explains how what is described there is accomplished.
     *
     * To check that the string will not cause the terminal to prompt for
     * another line of input, the following must happen:
     *  - Single quote marks must occur in pairs
     *  - Double quote marks must occur in pairs
     *  - The string cannot terminate with a backslash
     *
     * The complications to this are that single quote marks, double quote
     * marks, and backslashes all interact with one another.
     *
     * --Backslashes--
     * A backslash occuring before a slash, single quote mark, or double quote
     * mark will act as an escape. In that case if one of those is following the
     * slash it does not count as it normally does it, it is interpreted
     * literally. The exception to this rule is that backslashes inside of
     * single quotation marks are interpreted literally, they do not act as
     * escapes.
     *
     * --Single Quote Marks--
     * Text inside of single quote marks are interpreted literally. There is
     * no way to have a single quote mark inside of a single quote block. Any
     * backslashes or double quote marks inside of a single quote block is
     * interpreted literally.
     *
     * --Double Quote Marks--
     * Single quote marks are automatically escaped inside of double quote
     * blocks. Backslashes are not escaped inside of double quote blocks.
     * Double quote marks may be escaped inside of double quote blocks by use
     * of a backslash.
     *
     * To accomplish this, the string is iterated over one character at a time.
     * It keeps track of if previous character was an escape character, if
     * currently inside of a single quote block, or if inside of a double quote
     * block. The number of times that non-escaped single and double quotes
     * occur is tracked to ensure that they occur an even number of times,
     * meaning there is a pair.
     */
    public TerminalStringValidity checkTerminalValidity(String str)
    {
        char[] strArray = str.toCharArray();

        int singleOccurences = 0;
        int doubleOccurences = 0;
        boolean previousWasEscape = false;
        boolean insideSingleQuotes = false;
        boolean insideDoubleQuotes = false;
        for(char c : strArray)
        {
            boolean currIsEscape = false;

            //Inside of single quotes everything is interpreted literally
            //The single quote block will not terminate until the closing single
            //quote is encountered
            if(insideSingleQuotes)
            {
                //If at the end of single quote block
                if(c == '\'') // In Java a ' must be escaped as \'
                {
                    insideSingleQuotes = false;
                    singleOccurences++;
                }
            }
            //If this backslash is an escape character
            //It is not an escape character if the previous character was an escape character
            else if(!previousWasEscape && c == '\\')
            {
                currIsEscape = true;
            }
            //Inside of a double quote, single quotes are automatically escaped
            //But backslashes and double quotes are not
            else if(insideDoubleQuotes)
            {
                //If at the end of double quote block
                if(!previousWasEscape && c == '"')
                {
                    insideDoubleQuotes = false;
                    doubleOccurences++;
                }
            }
            //Not inside of single or double quotes and the previous character
            //is not causing this character to be escaped
            else if(!previousWasEscape)
            {
                if(c == '"')
                {
                    insideDoubleQuotes = true;
                    doubleOccurences++;
                }
                else if(c == '\'') // ' must be escaped as \'
                {
                    insideSingleQuotes = true;
                    singleOccurences++;
                }
            }

            previousWasEscape = currIsEscape;
        }

        //Only an even number of quotes are valid
        boolean singleValid = (singleOccurences % 2) == 0;
        boolean doubleValid = (doubleOccurences % 2) == 0;

        //Cannot end with an escape
        boolean terminatesProperly = !previousWasEscape;

        TerminalStringValidity validity = new TerminalStringValidity(singleValid,
                doubleValid, terminatesProperly);

        return validity;
    }
}