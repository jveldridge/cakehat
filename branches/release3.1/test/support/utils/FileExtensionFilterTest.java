package support.utils;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Tests for {@link utils.FileExtensionFilter}.
 *
 * @author jak2
 */
public class FileExtensionFilterTest
{
    @Test
    public void testDoesNotExist()
    {
        //Mock a File that does not exist
        File file = createMock(File.class);
        expect(file.exists()).andReturn(false);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummy.ext");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("ext");
        
        //Should not accept a File that does not exist
        boolean accepted = filter.accept(file);
        assertEquals(false, accepted);
    }

    @Test
    public void testHidden()
    {
        //Mock a File that is hidden
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(true);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummy.ext");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("ext");
        
        //Should not accept a File that is hidden
        boolean accepted = filter.accept(file);
        assertEquals(false, accepted);
    }

    @Test
    public void testNotFile()
    {
        //Mock a File that is not a file
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(false);
        expect(file.getName()).andReturn("dummy.ext");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("ext");
        
        //Should not accept a File that is not a file
        boolean accepted = filter.accept(file);
        assertEquals(false, accepted);
    }

    @Test
    public void testNoExtensionFile()
    {
        //Mock a File that does not have an extension
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummyext");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("ext");
        
        //Should not accept a File that ends with the name of the extensions
        //but is not actually an extension
        boolean accepted = filter.accept(file);
        assertEquals(false, accepted);
    }

    @Test
    public void testSimpleExtensionFile()
    {
        //Mock a File that has an ordinary file extension
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummy.ext");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("ext");

        //Should accept
        boolean accepted = filter.accept(file);
        assertEquals(true, accepted);
    }

    @Test
    public void testCompoundExtensionFile()
    {
        //Mock a File with a compound extension
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummy.tar.gz");
        replay(file);

        FileExtensionFilter filter = new FileExtensionFilter("tar.gz");

        //Should accept
        boolean accepted = filter.accept(file);
        assertEquals(true, accepted);
    }

    @Test
    public void testExtensionFileMultiAccept()
    {
        //Mock a File that has an ordinary extension
        File file = createMock(File.class);
        expect(file.exists()).andReturn(true);
        expect(file.isHidden()).andReturn(false);
        expect(file.isFile()).andReturn(true);
        expect(file.getName()).andReturn("dummy.ext");
        replay(file);

        //Filter accepts multiple extensions
        FileExtensionFilter filter = new FileExtensionFilter("tar.gz", "ext", "java");

        //Should accept
        boolean accepted = filter.accept(file);
        assertEquals(true, accepted);
    }
}