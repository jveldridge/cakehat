package gradesystem.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Accepts specific files.
 *
 * @author jak2
 */
public class SpecificFileFilter implements FileFilter
{
    private final HashSet<File> _acceptableFiles;

    public SpecificFileFilter(Collection<File> acceptableFiles)
    {
        _acceptableFiles = new HashSet<File>(acceptableFiles);
    }

    public SpecificFileFilter(File... acceptableFiles)
    {
        this(Arrays.asList(acceptableFiles));
    }

    public boolean accept(File file)
    {
        return _acceptableFiles.contains(file);
    }
}