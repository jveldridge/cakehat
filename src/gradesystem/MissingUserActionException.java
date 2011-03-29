package gradesystem;

/**
 * Represents that an action the user should have taken, like setting up Groups
 * for an assignment with groups, has not yet been taken.  This exception being
 * thrown does <i>not</i> mean that an error has occurred within cakehat code.
 *
 * @author jeldridg
 */
public class MissingUserActionException extends CakehatException {

    public MissingUserActionException(String msg) {
        super(msg);
    }

    public MissingUserActionException(Throwable cause) {
        super(cause);
    }

    public MissingUserActionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
