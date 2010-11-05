package utils;

/**
 * Utility methods that involve external processes.
 */
public class ExternalProcessesUtilities
{
    /**
     * Executes the given cmd in a separate visible terminal.  Upon the command's
     * completion, the terminal remains open and can be used interactively.
     *
     * @param title
     * @param cmd
     */
    public void executeInVisibleTerminal(String title, String cmd)
    {
        String terminalCmd = "/usr/bin/xterm -fg black -bg gray" +
                " -title " + "\"" + title + "\"" +
                " -e " + "\"" + cmd + "; bash" + "\"";

        BashConsole.writeThreaded(terminalCmd);
    }

    //TODO: Method to open Kate

    //TODO: Method to open kpdf (or some other pdf reader)
}