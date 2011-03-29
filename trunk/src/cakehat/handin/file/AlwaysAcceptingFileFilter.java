package cakehat.handin.file;

import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter which always accepts.
 *
 * @author jak2
 */
public class AlwaysAcceptingFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        return true;
    }
}