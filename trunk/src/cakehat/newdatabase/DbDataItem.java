package cakehat.newdatabase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a piece of data that is or will be stored in the database.  This class abstracts out the locking required
 * to ensure that object fields are not changed while the database is reading them.
 * 
 * @author jak2
 * @author jeldridg
 */
public abstract class DbDataItem
{
    private final Lock _dbLock = new ReentrantLock();
    private volatile boolean _matchesDb;
    
    private volatile Integer _id;
    
    
    DbDataItem(boolean matchesDb, Integer id)
    {
        _matchesDb = matchesDb;
        _id = id;
    }
        
    /**
     * Returns true if all fields are identical to the corresponding records in the database, false otherwise.
     * 
     * @return 
     */
    public boolean matchesDatabase()
    {
        return _matchesDb;
    }
    
    /**
     * Locks the object so that it can be written to the database.  While locked, all public mutator methods 
     * called by the configuration manager will block so that the object's state is not changed during the database
     * write.
     */
    void dbStart()
    {
        _dbLock.lock();
    }
    
    /**
     * Called to indicate that the object was successfully added to or updated in the database.
     * 
     * @param id
     */
    void dbSucceed(int id)
    {
        _id = id;
        _matchesDb = true;
    }
    
    /**
     * <strong>Must</strong> be called in the finally block of every database method that calls {@link #dbStart()}.
     * This releases the lock on the object so that its fields may again be mutated.
     */
    void dbStop()
    {   
        _dbLock.unlock();
    }
    
    public Integer getId()
    {
        return _id;
    }
    
    /**
     * Runs the given {@link Runnable} with this DbDataItem in a locked state.
     * 
     * @param block 
     */
    void updateUnderLock(Runnable block)
    {
        _dbLock.lock();
        try
        {
            block.run();
            _matchesDb = false;
        }
        finally
        {
            _dbLock.unlock();
        }
    }
}