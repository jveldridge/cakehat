package utils;

import java.io.File;
import java.util.Comparator;

/**
 * Compares two {@link java.io.File}s based on their filenames without respect
 * to their absolute paths.
 *
 * @author jak2
 */
public class AlphabeticFileComparator implements Comparator<File>
{
    public int compare(File f1, File f2)
    {
        return f1.getName().compareTo(f2.getName());
    }
}