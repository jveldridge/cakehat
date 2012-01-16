package cakehat.newdatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A single value stored in the database's property table.
 *
 * @author jak2
 */
public class DbPropertyValue<T> extends DbDataItem
{
    private volatile T _value;
    
    public DbPropertyValue(T value)
    {
        super(null);
        
        _value = value;
    }
    
    DbPropertyValue(int id, T value)
    {
        super(id);
        
        _value = value;
    }
    
    public void setValue( T value)
    {
        _value = value;
    }
    
    public T getValue()
    {
        return _value;
    }
    
    /**
     * A key for the specific property values recognized by the database. Allows for setting and getting data in a type
     * safe manner.
     * 
     * @param <T> the data type of the value associated with this key
     */
    public abstract static class DbPropertyKey<T>
    {
        /**
         * The name of the key. This value is stored in the database.
         */
        private final String _name;
        
        /**
         * The type of the data associated with this key. This value exists at run time; it is not removed by type
         * erasure.
         */
        private final Class<T> _type;
        
        private DbPropertyKey(String name, Class<T> type)
        {
            _name = name;
            _type = type;
        }
        
        /**
         * The name of the key.
         * 
         * @return 
         */
        String getName()
        {
            return _name;
        }
        
        /**
         * Returns a useful string for debugging purposes.
         * 
         * @return 
         */
        @Override
        public String toString()
        {
           return "[" + DbPropertyKey.class.getSimpleName() + " name=" + _name + ", type=" +
                   _type.getCanonicalName() + "]";
        }
        
        /**
         * Retrieves the value associated with this key in a type safe manner by wrapping around the SQL method that
         * retrieves the value from the result set.
         * 
         * @param rs
         * @param columnLabel
         * @return
         * @throws SQLException 
         */
        abstract T getValue(ResultSet rs, String columnLabel) throws SQLException;
        
        /**
         * Sets the value associated with this key in a type safe manner by wrapping around the SQL method that sets
         * the value in the prepared statement.
         * 
         * @param ps
         * @param parameterIndex
         * @param value
         * @throws SQLException 
         */
        abstract void setValue(PreparedStatement ps, int parameterIndex, T value) throws SQLException;
        
        
        // Versions for specific data types
        
        
        private static class DbStringPropertyKey extends DbPropertyKey<String>
        {
            private DbStringPropertyKey(String name)
            {
                super(name, String.class);
            }

            @Override
            String getValue(ResultSet result, String columnLabel) throws SQLException
            {
                return result.getString(columnLabel);
            }

            @Override
            void setValue(PreparedStatement ps, int parameterIndex, String value) throws SQLException
            {
                ps.setString(parameterIndex, value);
            }
        }
        
        private static class DbBooleanPropertyKey extends DbPropertyKey<Boolean>
        {
            private DbBooleanPropertyKey(String name)
            {
                super(name, Boolean.class);
            }

            @Override
            Boolean getValue(ResultSet result, String columnLabel) throws SQLException
            {
                return result.getBoolean(columnLabel);
            }

            @Override
            void setValue(PreparedStatement ps, int parameterIndex, Boolean value) throws SQLException
            {
                ps.setBoolean(parameterIndex, value);
            }
        }
        
        
        // Instances of this class - one for each supported key
        
        
        public static final DbPropertyKey<String> EMAIL_ACCOUNT = new DbStringPropertyKey("email_account");
        public static final DbPropertyKey<String> EMAIL_PASSWORD = new DbStringPropertyKey("email_password");
        public static final DbPropertyKey<Boolean> ATTACH_GRADING_SHEET = new DbBooleanPropertyKey("attach_grading_sheet");
    }
}