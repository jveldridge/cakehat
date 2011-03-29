package support.utils;

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
     * Information about if a string is valid to be used in a terminal.
     */
    public static final class TerminalStringValidity
    {
        private final boolean _singleQuotedProperly, _doubleQuotedProperly,
                _terminatedProperly;

        public TerminalStringValidity(boolean singleQuotedProperly,
                boolean doubleQuotedProperly, boolean terminatedProperly)
        {
            _singleQuotedProperly = singleQuotedProperly;
            _doubleQuotedProperly = doubleQuotedProperly;
            _terminatedProperly = terminatedProperly;
        }

        /**
         * If single quote marks are used properly. Each opening single quote
         * that is not escaped or not inside of double quote marks must be
         * matched by a closing single quote mark.
         *
         * @return
         */
        public boolean isSingleQuotedProperly()
        {
            return _singleQuotedProperly;
        }

        /**
         * If double quote marks are used properly. Each opening double quote
         * that is not escaped or not inside of single quote marks must be
         * matched by a closing double quote mark.
         *
         * @return
         */
        public boolean isDoubleQuotedProperly()
        {
            return _doubleQuotedProperly;
        }

        /**
         * If the string terminates properly. To terminate properly the string
         * must not end with a <code>\</code> that is not escaped.
         *
         * @return
         */
        public boolean isTerminatedProperly()
        {
            return _terminatedProperly;
        }

        /**
         * If the string is valid for terminal use. The result of anding
         * together {@link #isSingleQuotedProperly()},
         * {@link #isDoubleQuotedProperly()}, and
         * {@link #isTerminatedProperly()}.
         *
         * @return
         */
        public boolean isValid()
        {
            return _singleQuotedProperly && _doubleQuotedProperly &&
                    _terminatedProperly;
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

     /**
      * Checks the validity of a string to be used in a terminal. Technically
      * speaking, any string is valid in a terminal, but some sequences will
      * result in the terminal waiting for more input. This is intended to allow
      * for a user to provide the input over multiple actual lines. However, in
      * cakehat this will result in the command never executing as more input
      * will not be provided. The external call in that case will not raise an
      * exception, it will just not do anything.
      *
      * @param str
      * @return
      */
    public TerminalStringValidity checkTerminalValidity(String str);
}