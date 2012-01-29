package cakehat.views.config;

import cakehat.newdatabase.DbDataItem;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * A runnable which defines equality based on the {@link DbDataItem} it is operating on.
 *
 * @author jak2
 */
abstract class DbRunnable implements Runnable
{
    private final DbDataItem _item;
    private final UniqueElementSingleThreadWorker _worker;
    
    DbRunnable(UniqueElementSingleThreadWorker worker, DbDataItem item)
    {
        if(worker == null)
        {
            throw new NullPointerException("worker may not be null");
        }
        if(item == null)
        {
            throw new NullPointerException("item may not be null");
        }
        
        _worker = worker;
        _item = item;
    }
    
    @Override
    public void run()
    {
        //Try the database call(s)
        try
        {
            dbCall();
        }
        //If the database call(s) fails
        catch(SQLException e)
        {
            //If the worker is still taking submissions - if it's not that means the configuration manager is closing
            //If this update task has been placed on the queue again then the update will be tried again so there's no
            //need to do anything
            if(_worker.canSubmit() && !_worker.contains(this))
            {   
                try
                {
                    //Invoke the recovery method on the AWT thread while pausing on the database interaction thread
                    //This temporarily makes the configuration manager single threaded
                    EventQueue.invokeAndWait(new Runnable()
                    {
                        public void run()
                        {
                            onDbCallFailure();
                        }
                    });
                }
                //If unable to wait on the database thread
                catch(InterruptedException ex)
                {
                    onFinalFailureNow();

                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            onFinalFailureLater();
                        }
                    });
                }
                //If a runtime exception was thrown by onDbCallFailure()
                catch(InvocationTargetException ex)
                {
                    onFinalFailureNow();

                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            onFinalFailureLater();
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Invoked on the calling thread - in practice this is the database worker thread.
     * 
     * @throws SQLException 
     */
    public abstract void dbCall() throws SQLException;
    
    /**
     * Invoked on the AWT thread while pausing the calling thread.
     * <br/><br/>
     * This should respond to the database call failing.
     */
    public abstract void onDbCallFailure();
    
    /**
     * Invoked on the calling thread - in practice this is the database worker thread.
     * <br/><br/>
     * This should respond to the calling thread (in practice the database thread) being interrupted while waiting for
     * {@link #onDbCallFailure()} to finish execution.
     */
    public abstract void onFinalFailureNow();
    
    /**
     * Invoked on the AWT thread while not pausing the calling thread.
     * <br/><br/>
     * This should respond to the database thread being interrupted while waiting for {@link #onDbCallFailure()} to
     * finish execution.
     */
    public abstract void onFinalFailureLater();
    
    @Override
    public boolean equals(Object other)
    {
        boolean equal = false;
        if(other instanceof DbRunnable)
        {
            equal = (_item == ((DbRunnable)other)._item);
        }
        
        return equal;
    }
    
    @Override
    public int hashCode()
    {
        return _item.hashCode();
    }
}