package utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility methods that involve external processes.
 */
public interface ExternalProcessesUtilities
{
    /**
     * The normal and error streams, converted to lists of Strings, from
     * executing an external process.
     */
    public static final class ProcessResponse
    {
        private final List<String> _outputResponse, _errorResponse;

        public ProcessResponse(List<String> outputResponse, List<String> errorResponse)
        {
            _outputResponse = outputResponse;
            _errorResponse = errorResponse;
        }

        public List<String> getOutputResponse()
        {
            return _outputResponse;
        }

        public List<String> getErrorResponse()
        {
            return _errorResponse;
        }
    }

    /**
     * Executes the given command <code>cmd</code> in a separate visible
     * terminal.  Upon the command's completion, the terminal remains open and
     * can be used interactively.
     *
     * @param title The title of the terminal
     * @param cmd may be <code>null</code>, if so the terminal is opened in
     * the directory specified by <code>dir</code>
     * @param dir the directory the command will be executed in
     * @throws IOException
     */
    public void executeInVisibleTerminal(String title, String cmd, File dir) throws IOException;

    /**
     * Executes a command. Does not wait for the response.
     *
     * @param cmd
     * @param dir the directory the command will be executed in
     * @throws IOException
     */
    public void executeAsynchronously(String cmd, File dir) throws IOException;

    /**
     * Executes a series of commands. Does not wait for the response.
     *
     * @param cmds
     * @param dir the directory the command will be executed in
     * @throws IOException
     */
    public void executeAsynchronously(Iterable<String> cmds, File dir) throws IOException;

    /**
     * Executes a command. Waits until the resulting process is complete and
     * then returns the normal stream and error stream as a ProcessResponse.
     *
     * @param cmd
     * @param dir the directory the command will be executed in
     * @return
     * @throws IOException
     */
    public ProcessResponse executeSynchronously(String cmd, File dir) throws IOException;

    /**
     * Executes a series of commands. Waits until the resulting process is
     * complete and then returns the normal stream and error stream as a
     * ProcessResponse.
     *
     * @param cmds
     * @param dir the directory the commands will be executed in
     * @return
     * @throws IOException
     */
    public ProcessResponse executeSynchronously(Iterable<String> cmds, File dir) throws IOException;
}