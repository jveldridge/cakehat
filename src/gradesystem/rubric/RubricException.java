package gradesystem.rubric;

/**
 * An exception that occurs on rubric errors.
 *
 * @author jeldridg
 */
public class RubricException extends Exception
{
    public RubricException() {
        super();
    }

    public RubricException(String msg) {
        super(msg);
    }

    public RubricException(String msg, Throwable cause) {
        super(msg, cause);
    }
}