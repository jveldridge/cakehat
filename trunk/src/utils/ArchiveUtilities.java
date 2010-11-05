package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility methods for interacting with archive files with the following
 * extensions:
 *  - zip
 *  - tar
 *  - tgz / tar.gz
 */
public class ArchiveUtilities
{
    /**
     * Gets the appropriate stream depending the file extension of
     * <code>archivePath</code>.
     * <br><br>
     * Supported extensions: zip, tar, tgz, tar.gz. Unsupported extensions will
     * result in an extension being throw.
     *
     * @param archivePath
     * @return
     */
    private ArchiveInputStream getArchiveInputStream(String archivePath) throws IOException, ArchiveException
    {
        //Determine appropriate input stream and compression format
        InputStream is;
        String format;

        String lowerCaseSrcFile = archivePath.toLowerCase();

        if(lowerCaseSrcFile.endsWith(".zip"))
        {
            is = new FileInputStream(archivePath);
            format = "zip";
        }
        else if(lowerCaseSrcFile.endsWith(".tar"))
        {
            is = new FileInputStream(archivePath);
            format = "tar";
        }
        else if(lowerCaseSrcFile.endsWith(".tgz") || archivePath.toLowerCase().endsWith(".tar.gz"))
        {
            is = new GZIPInputStream(new FileInputStream(new File(archivePath)));
            format = "tar";
        }
        else
        {
            throw new IOException("Unsupported file extension. Supported extensions are: zip, tar, tgz, & tar.gz");
        }

        return new ArchiveStreamFactory().createArchiveInputStream(format, is);
    }

    /**
     * Returns a listing of the files and directories in the archive file
     * without extracting the file.
     * <br><br>
     * Supports: zip, tar, tgz/tar.gz
     *
     * @param archivePath
     *
     * @return collection of Strings with the paths of files and directories in the archive
     */
    public Collection<String> getArchiveContents(String archivePath)
    {
        Vector<String> contents = new Vector<String>();

        try
        {
            ArchiveInputStream in = getArchiveInputStream(archivePath);
            while(true)
            {
                ArchiveEntry entry = in.getNextEntry();
                if(entry == null)
                {
                    break;
                }

                contents.add(entry.getName());
            }
            in.close();
        }
        catch(IOException e)
        {
            new ErrorView(e);
        }
        catch (ArchiveException e)
        {
            new ErrorView(e);
        }

        return contents;
    }

    /**
     * Extracts an archive file. Supported extensions: zip, tar, tgz, tar.gz
     *
     * @param archivePath the absolute path of the archive file
     * @param dstDir the directory the archive file will be expanded into
     *
     * @boolean success of extracing archive
     */
    public boolean extractArchive(String archivePath, String dstDir)
    {
        try
        {
            ArchiveInputStream in = getArchiveInputStream(archivePath);
            while(true)
            {
                ArchiveEntry entry = in.getNextEntry();
                if(entry == null)
                {
                    break;
                }

                File file = new File(dstDir, entry.getName());
                if(entry.isDirectory())
                {
                    file.mkdirs();
                }
                else
                {
                    OutputStream out = new FileOutputStream(file);
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
            in.close();
        }
        catch(IOException e)
        {
            new ErrorView(e);

            return false;
        }
        catch (ArchiveException e)
        {
            new ErrorView(e);

            return false;
        }

        return true;
    }
}