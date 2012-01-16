package cakehat.newdatabase;

/**
 * Represents a piece of data that is or will be stored in the database.
 * 
 * @author jak2
 * @author jeldridg
 */
public abstract class DbDataItem
{   
    private volatile Integer _id;
    
    DbDataItem(Integer id)
    {
        _id = id;
    }
    
    void setId(Integer id)
    {
        _id = id;
    }
    
    public Integer getId()
    {
        return _id;
    }
}