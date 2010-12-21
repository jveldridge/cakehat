package utils;

/**
 * Utility methods that involve external processes.
 */
public interface ExternalProcessesUtilities
{
    /**
     * Executes the given cmd in a separate visible terminal.  Upon the command's
     * completion, the terminal remains open and can be used interactively.
     *
     * @param title
     * @param cmd
     */
    public void executeInVisibleTerminal(String title, String cmd);

    //TODO: Method to open Kate

    //TODO: Method to open kpdf (or some other pdf reader)
}