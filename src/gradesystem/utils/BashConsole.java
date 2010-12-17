package gradesystem.utils;

import gradesystem.views.shared.ErrorView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.List;

/**
 * A utility class that allows for sending commands to a hidden bash console
 * so that any commands that could be written to a normal terminal can be used.
 * Avoid using this class to do things that can be done more appropriately in
 * pure Java code.
 *
 * Be aware that this hidden bash console will not necessarily have the same
 * environment variables as your normal terminal.
 *
 *
 * @author jak2 (Joshua Kaplan)
 */
public class BashConsole {

    /**
     * Creates a non-visible bash console, writes the input command
     * to the the console. Then terminates the bash console.
     *
     * @param cmd The command to execute in the bash console
     */
    public static void writeThreaded(final String cmd) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                write(cmd);
            }
        });
    }

    /**
     * Creates a non-visible bash console, writes the input commands
     * to the the console. Then terminates the bash console.
     *
     * @param input The commands to execute in the bash console
     */
    public static void writeThreaded(final Iterable<String> input) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                write(input);
            }
        });
    }

    /**
     * Creates a non-visible bash console, writes the command to the console.
     * Then terminates the bash console. Returns a Collection of the console
     * output stream followed by the console error stream.
     *
     * @param cmd The command to execute in the bash console
     * @return output and error streams
     */
    public static List<String> write(String cmd) {
        List<String> cmds = new Vector<String>();
        cmds.add(cmd);

        return write(cmds);
    }

    /**
     * Creates a non-visible bash console, writes the input commands
     * to the the console. Then terminates the bash console. Returns a
     * Collection of the console output stream followed by the console error
     * stream.
     * 
     * @param input The commands to execute in the bash console
     * @return output and error streams
     */
    public static List<String> write(Iterable<String> input) {
        List<String> output = new Vector<String>();
		
	File wd = new File("/bin");
	Process proc = null;

	try{ 
            proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
	} 
	catch (IOException e) {
            new ErrorView(e);
	}

	if (proc != null) {
            //This is slightly confusing:
            //in  - the output stream of the bash console
            //out - what we are going to write to the console (using the input parameter passed in)
            //err - the error stream of the bash console
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for(String line : input) {
                out.println(line);
            }
            out.println("exit");
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    output.add(line);
                }
                while ((line = err.readLine()) != null) {
                    output.add(line);
                }
                proc.waitFor();
                in.close();
                err.close();
                out.close();
                proc.destroy();
            }
            catch (Exception e) {
                new ErrorView(e);
            }
	}
		
	return output;
    }

    public static List<String> writeErrorStream(Iterable<String> input) {
    Vector<String> output = new Vector<String>();

	File wd = new File("/bin");
	Process proc = null;

	try{
            proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
	}
	catch (IOException e) {
            new ErrorView(e);
	}

	if (proc != null) {
            //This is slightly confusing:
            //in  - the output stream of the bash console
            //out - what we are going to write to the console (using the input parameter passed in)
            //err - the error stream of the bash console
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for(String line : input) {
                out.println(line);
            }
            out.println("exit");
            try {
                String line;
                while ((line = err.readLine()) != null) {
                    output.add(line);
                }
                proc.waitFor();
                err.close();
                out.close();
                proc.destroy();
            }
            catch (Exception e) {
                new ErrorView(e);
            }
	}

	return output;
    }
}