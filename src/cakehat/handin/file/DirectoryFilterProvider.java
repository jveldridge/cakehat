package cakehat.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Generates FileFilters which accept files that are the directory or are in
 * the directory specified by the path property in the configuration file. The
 * actual FileFilter created will be with respect to the directory the handin
 * was unarchived into.
 *
 * @author jak2
 */
public class DirectoryFilterProvider implements FilterProvider
{
    private final String _relativePath;

    /**
     * 
     * @param relativePath the PATH value from the configuration file
     */
    public DirectoryFilterProvider(String relativePath)
    {
        _relativePath = relativePath;
    }

    public FileFilter getFileFilter(File unarchivedDir)
    {
        final String absolutePath = new File(unarchivedDir, _relativePath).getAbsolutePath();

        FileFilter filter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.getAbsolutePath().startsWith(absolutePath);
            }
        };

        return filter;
    }

    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuffer buffer)
    {
        boolean matches = false;

        for(ArchiveEntry entry : archiveContents)
        {
            if(entry.isDirectory())
            {
                //Entry's path does not start with a /
                //However, the specified relative path may, if it does, trim it
                String dirPath = _relativePath;
                if(dirPath.startsWith("/"))
                {
                    dirPath = dirPath.substring(1);
                }
                //Entry's path ends with a /
                //However, the specified relative path may not, add it if necessary
                if(!dirPath.endsWith("/"))
                {
                    dirPath += "/";
                }

                if(entry.getName().equals(dirPath))
                {
                    matches = true;
                }
            }
        }

        if(!matches)
        {
            buffer.append("Directory: " + _relativePath + "\n");
        }

        return matches;
    }
}