package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author jak2
 */
public class FileSystemUtilitiesTest
{
    @Test
    public void testGetAllFiles_AcceptAll() throws IOException
    {
        File subFile1 = createMock(File.class);
        expect(subFile1.isDirectory()).andReturn(false);
        replay(subFile1);

        File subFile2 = createMock(File.class);
        expect(subFile2.isDirectory()).andReturn(false);
        replay(subFile2);

        File rootFile = createMock(File.class);
        expect(rootFile.listFiles()).andReturn(new File[] { subFile1, subFile2 });
        expect(rootFile.isDirectory()).andReturn(true);
        replay(rootFile);

        FileFilter acceptAllFilter = new FileFilter()
        {
            public boolean accept(File file) { return true; }
        };
        Collection<File> response =
                new FileSystemUtilitiesImpl().getFiles(rootFile, acceptAllFilter);

        //Check all of the files are returned and there are no duplicates
        assertEquals("Should contain exactly 3 Files", 3, response.size());
        assertTrue(response.contains(rootFile));
        assertTrue(response.contains(subFile1));
        assertTrue(response.contains(subFile2));
    }
}