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

public class ArchiveUtilitiesImpl implements ArchiveUtilities
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
    private ArchiveInputStream getArchiveInputStream(File archive) throws IOException, ArchiveException
    {
        //Determine appropriate input stream and compression format
        InputStream is;
        String format;

        String lowerCaseSrcFile = archive.getAbsolutePath().toLowerCase();

        if(lowerCaseSrcFile.endsWith(".zip"))
        {
            is = new FileInputStream(archive);
            format = "zip";
        }
        else if(lowerCaseSrcFile.endsWith(".tar"))
        {
            is = new FileInputStream(archive);
            format = "tar";
        }
        else if(lowerCaseSrcFile.endsWith(".tgz") || lowerCaseSrcFile.endsWith(".tar.gz"))
        {
            is = new GZIPInputStream(new FileInputStream(archive));
            format = "tar";
        }
        else
        {
            throw new IOException("Unsupported file extension. Supported extensions are: zip, tar, tgz, & tar.gz");
        }

        return new ArchiveStreamFactory().createArchiveInputStream(format, is);
    }

    public Collection<String> getArchiveContents(File archive) throws IOException, ArchiveException
    {
        Vector<String> contents = new Vector<String>();

        ArchiveInputStream in = getArchiveInputStream(archive);
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

        return contents;
    }

    public void extractArchive(File archive, File dstDir) throws IOException, ArchiveException
    {
        ArchiveInputStream in = getArchiveInputStream(archive);
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
}