package utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 * Utility methods for interacting with archive files with the following
 * extensions:
 *  - zip
 *  - tar
 *  - tgz / tar.gz
 */
public interface ArchiveUtilities
{
    /**
     * Returns a listing of the files and directories in the archive file
     * without extracting the file.
     * <br><br>
     * Supports: zip, tar, tgz/tar.gz
     *
     * @param archive
     *
     * @return the entries in the archive
     */
    public Collection<ArchiveEntry> getArchiveContents(File archive) throws ArchiveException;

    /**
     * Extracts an archive file. Each file and directory to be created will be
     * checked against the <code>filter</code>. Only if it accepts will the file
     * be unarchived.
     * <br/><br/>
     * Supported extensions: zip, tar, tgz, tar.gz
     *
     * @param archive
     * @param dstDir
     * @param filter
     * @throws ArchiveException
     */
    public void extractArchive(File archive, File dstDir, FileFilter filter) throws ArchiveException;

    /**
     * Returns a FileFilter which only accepts files that ArchiveUtilities
     * is able to unarchive.
     *
     * @return
     */
    public FileFilter getSupportedFormatsFilter();
}