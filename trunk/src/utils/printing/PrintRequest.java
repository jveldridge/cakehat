package utils.printing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Vector;

/**
 * A request to be submitted to a Printer. Each file added to the request should
 * be a plain text file.
 *
 * Can have an associated TA Login and Student Login that may or may not be used
 * by the printer that the PrintRequest is submitted to.
 *
 * @author jak2
 */
public class PrintRequest
{
    private Vector<File> _files = new Vector<File>();
    private String _taLogin = "", _studentLogin = "";

    /**
     * A request of text files.
     *
     * @param file
     * @param taLogin
     * @param studentLogin
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(File file, String taLogin, String studentLogin)
                                                        throws FileNotFoundException
    {
        this.addFile(file);
        _taLogin = taLogin;
        _studentLogin = studentLogin;
    }

    /**
     * A request of text files.
     *
     * @param files
     * @param taLogin
     * @param studentLogin
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Collection<File> files, String taLogin, String studentLogin)
                                                        throws FileNotFoundException
    {
        this(files);
        _taLogin = taLogin;
        _studentLogin = studentLogin;
    }

    /**
     * A request of text files.
     *
     * @param files
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Collection<File> files) throws FileNotFoundException
    {
        this.addFiles(files);
    }

    /**
     * Adds a file to the request.
     *
     * @param file
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public void addFile(File file) throws FileNotFoundException
    {
        if(!file.exists())
        {
            throw new FileNotFoundException(file.getAbsolutePath() + " cannot be found");
        }
        else
        {
            _files.add(file);
        }
    }

    /**
     * Adds files to the request.
     *
     * @param files
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public void addFiles(Iterable<File> files) throws FileNotFoundException
    {
        for(File file : files)
        {
            this.addFile(file);
        }
    }

    public void setTALogin(String taLogin)
    {
        _taLogin = taLogin;
    }

    public String getTALogin()
    {
        return _taLogin;
    }

    public void setStudentLogin(String studentLogin)
    {
        _studentLogin = studentLogin;
    }

    public String getStudentLogin()
    {
        return _studentLogin;
    }

    /**
     * The files in this PrintRequest.
     *
     * @return
     */
    Iterable<File> getFiles()
    {
        return _files;
    }
}