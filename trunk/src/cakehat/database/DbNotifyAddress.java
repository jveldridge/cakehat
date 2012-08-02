package cakehat.database;

import java.util.Collections;

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
    
    @Override
    void setParentNull() {
        throw new UnsupportedOperationException("This data item type has no parent.");
    }
    
    @Override
    Iterable<? extends DbDataItem> getChildren() {
        return Collections.emptyList();
    }
}
