package utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
     * @return collection of Strings with the paths of files and directories in the archive
     */
    public Collection<String> getArchiveContents(File archive) throws IOException, ArchiveException;

    /**
     * Extracts an archive file. Supported extensions: zip, tar, tgz, tar.gz
     *
     * @param archive the archive file
     * @param dstDir the directory the archive file will be expanded into
     */
    public void extractArchive(File archive, File dstDir) throws IOException, ArchiveException;
}