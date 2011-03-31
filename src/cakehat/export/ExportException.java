package cakehat.export;

/**
 * Exception resulting from an error exporting grades.
 * 
 * @author jeldridg
 */
public class ExportException extends Exception {

    public ExportException(String msg) {
        super(msg);
    }

    public ExportException(String msg, Throwable cause) {
        super(msg, cause);
    }
}