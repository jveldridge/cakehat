package support.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Utility methods for interacting with archive files with the following extensions:
 * <ul>
 * <li>zip</li>
 * <li>tar</li>
 * <li>tgz / tar.gz</li>
 * </ul>
 */
public interface ArchiveUtilities
{
    /**
     * Returns a listing of the files and directories in the archive file without extracting the file.
     *
     * @param archive
     *
     * @return the entries in the archive
     */
    public Set<ArchiveEntry> getArchiveContents(File archive) throws IOException;

    /**
     * Extracts an archive file. Each file and directory to be created will be checked against the {@code filter}. Only
     * if it accepts will the file be unarchived. Returns a set of the files and directory created.
     *
     * @param archive
     * @param dstDir
     * @param filter
     * @param groupOwner
     * @throws ArchiveExtractionException
     */
    public Set<File> extractArchive(File archive, File dstDir, FileFilter filter, String groupOwner)
            throws ArchiveExtractionException;

    /**
     * Returns a FileFilter which only accepts files that ArchiveUtilities is able to unarchive.
     * <br><br>
     * Supports: zip, tar, tgz/tar.gz
     *
     * @return
     */
    public FileFilter getSupportedFormatsFilter();
}