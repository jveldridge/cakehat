package cakehat.config.handin.file;

import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter which always accepts.
 *
 * @author jak2
 */
@Deprecated
public class AlwaysAcceptingFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        return true;
    }
}