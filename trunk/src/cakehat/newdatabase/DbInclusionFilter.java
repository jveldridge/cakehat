package cakehat.newdatabase;

/**
 * Represents an inclusion filter for a part as it is represented in the database and configuration manager.
 * 
 * @author jak2
 * @author jeldridg
 */
public class DbInclusionFilter extends DbDataItem
{
    public static enum FilterType {FILE, DIRECTORY};

    private final DbPart _part;
    private volatile FilterType _type;
    private volatile String _path;
    
    /**
     * Constructor to be used by the configuration manager to create a inclusion filter for a part.
     * 
     * @param part
     */
    public DbInclusionFilter(DbPart part)
    {
        super(null);
        
        _part = part;
    }

    /**
     * Constructor to be used by the database to load inclusion filter data into memory.
     * 
     * @param part
     * @param id
     * @param type
     * @param path 
     */
    DbInclusionFilter(DbPart part, int id, FilterType type, String path)
    {
        super(id);
        
        _part = part;
        _type = type;
        _path = path;
    }
    
    public void setType(FilterType type)
    {
        _type = type;
    }
    
    public FilterType getType()
    {
        return _type;
    }
    
    public void setPath(String path)
    {
        _path = path;
    }
    
    public String getPath()
    {
        return _path;
    }
    
    DbPart getPart()
    {
        return _part;
    }
}