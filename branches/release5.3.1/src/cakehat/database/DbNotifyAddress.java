package cakehat.database;

/**
 *
 * @author jak2
 */
public class DbNotifyAddress extends DbDataItem
{
    private volatile String _address;
    
    public DbNotifyAddress(String address)
    {
        super(null);
        
        _address = address;
    }
    
    DbNotifyAddress(int id, String address)
    {
        super(id);
        
        _address = address;
    }
    
    public void setAddress(final String address)
    {
        _address = address;
    }
    
    public String getAddress()
    {
        return _address;
    }
}
