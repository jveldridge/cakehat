package cakehat.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * A FileFilter which requires that any of the FileFilters it is composed of
 * accept the File.
 *
 * @author jak2
 */
public class OrFileFilter implements FileFilter
{
    private final Iterable<FileFilter> _filters;

    public OrFileFilter(FileFilter... filters)
    {
        this(Arrays.asList(filters));
    }

    public OrFileFilter(Iterable<FileFilter> filters)
    {
        _filters = filters;
    }

    public boolean accept(File file)
    {
        for(FileFilter filter : _filters)
        {
            if(filter.accept(file))
            {
                return true;
            }
        }

        return false;
    }
}