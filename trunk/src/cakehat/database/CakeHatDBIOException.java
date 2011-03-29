/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cakehat.database;

/**
 *
 * @author alexku
 */
public class CakeHatDBIOException extends Exception {

    /**
     * Creates a new instance of <code>CakeHatDBIOException</code> without detail message.
     */
    public CakeHatDBIOException() {
        super();
    }


    /**
     * Constructs an instance of <code>CakeHatDBIOException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CakeHatDBIOException(String msg) {
        super(msg);
    }

    public CakeHatDBIOException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
