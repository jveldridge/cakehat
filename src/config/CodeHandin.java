package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import utils.Allocator;
import utils.ErrorView;
import utils.printing.PrintRequest;

/**
 *
 * @author jak2
 */
public abstract class CodeHandin extends HandinPart
{
    private HashMap<String, String> _runProperties = new HashMap<String, String>();
    private HashMap<String, String> _demoProperties = new HashMap<String, String>();
    private HashMap<String, String> _testerProperties = new HashMap<String, String>();
    protected String _runMode, _demoMode, _testerMode;


    protected CodeHandin(Assignment asgn, String name, int points)
    {
        super(asgn, name, points);
    }

    public void setRunProperty(String key, String value)
    {
        _runProperties.put(key, value);
    }

    protected boolean hasRunProperty(String key)
    {
        return _runProperties.containsKey(key);
    }

    protected String getRunProperty(String key)
    {
        if(this.hasRunProperty(key))
        {
            return _runProperties.get(key);
        }
        else
        {
            return "";
        }
    }

    public void setDemoProperty(String key, String value)
    {
        _demoProperties.put(key, value);
    }

    protected boolean hasDemoProperty(String key)
    {
        return _demoProperties.containsKey(key);
    }

    protected String getDemoProperty(String key)
    {
        if(this.hasDemoProperty(key))
        {
            return _demoProperties.get(key);
        }
        else
        {
            return "";
        }
    }

    public void setTesterProperty(String key, String value)
    {
        _testerProperties.put(key, value);
    }

    protected boolean hasTesterProperty(String key)
    {
        return _testerProperties.containsKey(key);
    }

    protected String getTesterProperty(String key)
    {
        if(this.hasTesterProperty(key))
        {
            return _testerProperties.get(key);
        }
        else
        {
            return "";
        }
    }

    public void setRunMode(String mode)
    {
        _runMode = mode;
    }

    public void setDemoMode(String mode)
    {
        _demoMode = mode;
    }

    public void setTesterMode(String mode)
    {
        _testerMode = mode;
    }

    public void printCode(String studentLogin, String printer)
    {
        Collection<File> sourceFiles = this.getSourceFiles(studentLogin);

        PrintRequest request = null;
        try
        {
            request = new PrintRequest(sourceFiles,Allocator.getGeneralUtilities().getUserLogin(), studentLogin);
        }
        catch (FileNotFoundException ex)
        {
            new ErrorView(ex);
        }

        Allocator.getLandscapePrinter().print(request, printer);
    }

    public void printCode(Iterable<String> studentLogins, String printer)
    {
        Vector<PrintRequest> requests = new Vector<PrintRequest>();

        for(String studentLogin : studentLogins)
        {
            Collection<File> sourceFiles = this.getSourceFiles(studentLogin);

            try
            {
                PrintRequest request = new PrintRequest(sourceFiles, Allocator.getGeneralUtilities().getUserLogin(), studentLogin);
                requests.add(request);
            }
            catch (FileNotFoundException ex)
            {
                new ErrorView(ex);
            }
        }

        Allocator.getLandscapePrinter().print(requests, printer);
    }

    private Collection<File> getSourceFiles(String studentLogin)
    {
        Collection<File> sourceFiles = new Vector<File>();

        for(String fileType : this.getSourceFileTypes())
        {
            Collection<File> files =
                    Allocator.getGeneralUtilities().getFiles(this.getStudentHandinDirectory(studentLogin), fileType);
            sourceFiles.addAll(files);
        }

        return sourceFiles;
    }

    public boolean hasCode()
    {
        return true;
    }

    protected abstract String[] getSourceFileTypes();
    
    protected abstract boolean isValid();
}