package cakehat;

/**
 * Cakehat Exception class.
 *
 * @author jeldridg
 */
public class CakehatException extends Exception {

    public CakehatException(String msg) {
        super(msg);
    }

    public CakehatException(Throwable cause) {
        super(cause);
    }

    public CakehatException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
