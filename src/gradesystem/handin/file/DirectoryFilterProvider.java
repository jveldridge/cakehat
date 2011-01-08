package gradesystem.handin.file;

import java.io.File;
import java.io.FileFilter;

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

    public boolean areFilteredFilesPresent(File unarchivedDir, StringBuffer buffer)
    {
        File absolutePath = new File(unarchivedDir, _relativePath);

        boolean exists = absolutePath.exists();
        if(!exists)
        {
            buffer.append("Directory: " + absolutePath.getAbsolutePath() + "\n");
        }

        return exists;
    }
}