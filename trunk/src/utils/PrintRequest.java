package utils;

/**
 * 
 *
 * @author jak2
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Vector;

public class PrintRequest
{
    private Vector<File> _files = new Vector<File>();
    private String _taLogin = "", _studentLogin = "";

    public PrintRequest() { }

    public PrintRequest(File file, String taLogin, String studentLogin)
                                                        throws FileNotFoundException
    {
        this.addFile(file);
        _taLogin = taLogin;
        _studentLogin = studentLogin;
    }

    public PrintRequest(Collection<File> files, String taLogin, String studentLogin)
                                                        throws FileNotFoundException
    {
        this(files);
        _taLogin = taLogin;
        _studentLogin = studentLogin;
    }

    public PrintRequest(Collection<File> files) throws FileNotFoundException
    {
        this.addFiles(files);
    }

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

    public Iterable<File> getFiles()
    {
        return _files;
    }
}