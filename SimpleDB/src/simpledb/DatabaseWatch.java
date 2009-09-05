/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledb;

import java.io.File;
import java.util.TimerTask;

/**
 *
 * @author Paul
 */
public abstract class DatabaseWatch extends TimerTask {

    private long timeStamp;
    private File file;

    public DatabaseWatch(File file) {
        this.file = file;
        this.timeStamp = file.lastModified();
    }

    public final void run() {
        long timeStamp = file.lastModified();
        if (this.timeStamp != timeStamp) {
            this.timeStamp = timeStamp;
            onChange(file);
        }
    }

    protected abstract void onChange(File file);
}
