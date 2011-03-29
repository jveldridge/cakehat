package cakehat.services;

/**
 * Exception to be thrown when calls to Services are unsuccessful.
 *
 * @author jeldridg
 */
public class ServicesException extends Exception {

    public ServicesException() {
        super();
    }

    public ServicesException(Throwable cause) {
        super(cause);
    }

    public ServicesException(String msg) {
        super(msg);
    }

    public ServicesException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
