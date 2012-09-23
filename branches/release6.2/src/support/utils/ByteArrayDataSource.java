package support.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * Constructs a data source backed by a byte array. Only allows reading from via {@link #getInputStream()}, attempts to
 * write via {@link #getOutputStream()} will fail.
 *
 * @author jak2
 */
public class ByteArrayDataSource implements DataSource
{
    private final String _name;
    private final String _type;
    private final byte[] _array;
    
    /**
     * Constructs the data source.
     * 
     * @param name the name of this data, will become the file name
     * @param type the MIME type of the data represented by the {@code array} 
     * @param array the data, will not be copied for efficiency and memory reasons - do <strong>not<strong> modify this
     * array after constructing this object
     */
    public ByteArrayDataSource(String name, String type, byte[] array)
    {
        _name = name;
        _type = type;
        _array = array;
    }
    
    @Override
    public InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream(_array);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {   
        throw new IOException("Writing to this data source is not supported");
    }

    @Override
    public String getContentType()
    {
        return _type;
    }

    @Override
    public String getName()
    {
        return _name;
    }
}