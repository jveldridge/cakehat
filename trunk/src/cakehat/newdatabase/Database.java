package cakehat.newdatabase;

import java.sql.SQLException;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public interface Database
{
    
    public void addActionProperty(DbActionProperty property) throws SQLException;
    
    public void resetDatabase() throws SQLException;
    
}
