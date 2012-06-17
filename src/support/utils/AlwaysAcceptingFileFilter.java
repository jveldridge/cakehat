package support.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter which always accepts.
 *
 * @author jak2
 */
public class AlwaysAcceptingFileFilter implements FileFilter
{
    @Override
    public boolean accept(File file)
    {
        return true;
    }
}