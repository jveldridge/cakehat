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
     * Executes the given cmd in a separate visible terminal.  Upon the command's
     * completion, the terminal remains open and can be used interactively.
     *
     * @param title
     * @param cmd
     * @throws IOException
     */
    public void executeInVisibleTerminal(String title, String cmd) throws IOException;

    /**
     * Executes a command. Does not wait for the response.
     *
     * @param cmd
     * @throws IOException
     */
    public void executeAsynchronously(String cmd) throws IOException;

    /**
     * Executes a series of commands. Does not wait for the response.
     *
     * @param cmds
     * @throws IOException
     */
    public void executeAsynchronously(Iterable<String> cmds) throws IOException;

    /**
     * Executes a command. Waits until the resulting process is complete and
     * then returns the normal streams and error streams as a ProcessResponse.
     *
     * @param cmd
     * @return
     * @throws IOException
     */
    public ProcessResponse executeSynchronously(String cmd) throws IOException;

    /**
     * Executes a series of commands. Waits until the resulting process is
     * complete and then returns the normal streams and error streams as a
     * ProcessResponse.
     *
     * @param cmds
     * @return
     * @throws IOException
     */
    public ProcessResponse executeSynchronously(Iterable<String> cmds) throws IOException;

    /**
     * Opens the specified file in kpdf.
     *
     * @param file
     * @throws IOException
     */
    public void kpdf(File file) throws IOException;

    /**
     * Opens the specified files in kate.
     *
     * @param files
     * @throws IOException
     */
    public void kate(Iterable<File> files) throws IOException;
}