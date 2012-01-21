package cakehat.assignment;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Generates FileFilters which accepts files that exactly match the path as specified by the configuration file. The
 * actual FileFilter created will be with respect to the directory the handin was unarchived into.
 *
 * @author jak2
 */
class FileFilterProvider implements FilterProvider
{
    private final String _relativePath;

    /**
     *
     * @param relativePath the PATH value from the configuration file
     */
    public FileFilterProvider(String relativePath)
    {
        _relativePath = relativePath;
    }

    @Override
    public FileFilter getFileFilter(File unarchivedDir)
    {
        final File absolutePath = new File(unarchivedDir, _relativePath);

        FileFilter filter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return absolutePath.equals(file);
            }
        };

        return filter;
    }

    @Override
    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuilder builder)
    {
        boolean matches = false;

        for(ArchiveEntry entry : archiveContents)
        {
            if(!entry.isDirectory())
            {
                //Entry's path does not start with a /
                //However, the specified relative path may, if it does, trim it
                String filePath = _relativePath;
                if(filePath.startsWith("/"))
                {
                    filePath = filePath.substring(1);
                }

                if(entry.getName().equals(filePath))
                {
                    matches = true;
                    break;
                }
            }
        }

        if(!matches)
        {
            builder.append("File: ");
            builder.append(_relativePath);
            builder.append("\n");
        }

        return matches;
    }
}