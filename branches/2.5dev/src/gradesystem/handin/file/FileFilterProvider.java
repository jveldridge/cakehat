package gradesystem.handin.file;

import java.io.File;
import java.io.FileFilter;

/**
 * Generates FileFilters which accepts files that exactly match the path as
 * specified by the configuration file. The actual FileFilter created will be
 * with respect to the directory the handin was unarchived into.
 *
 * @author jak2
 */
public class FileFilterProvider implements FilterProvider
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

    public boolean areFilteredFilesPresent(File unarchivedDir, StringBuffer buffer)
    {
        File absolutePath = new File(unarchivedDir, _relativePath);
        
        boolean exists = absolutePath.exists();
        if(!exists)
        {
            buffer.append("File: " + absolutePath.getAbsolutePath() + "\n");
        }

        return exists;
    }
}