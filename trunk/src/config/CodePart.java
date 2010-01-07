package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import utils.Allocator;
import utils.ErrorView;
import utils.printing.PrintRequest;

/**
 *
 * @author jak2
 */
public abstract class CodePart extends Part
{
    private HashMap<String, String> _runProperties = new HashMap<String, String>();
    private HashMap<String, String> _demoProperties = new HashMap<String, String>();
    private HashMap<String, String> _testerProperties = new HashMap<String, String>();
    protected String _runMode, _demoMode, _testerMode;
    private TimeInformation _timeInfo;
    private Collection<File> _handins = null;
    protected HashSet<String> _untarredStudents = new HashSet<String>();

    protected CodePart(Assignment asgn, String name, int points)
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

    public void setTimeInfo(TimeInformation info)
    {
        _timeInfo = info;
    }

    public TimeInformation getTimeInformation()
    {
        return _timeInfo;
    }

    /**
     * It returns a student's handin if it exists otherwise it returns null.
     *
     * @param studentLogin
     * @return a student's handin for this assignment.
     */
    protected File getHandin(String studentLogin)
    {
        for (File handin : this.getHandins())
        {
            if (handin.getName().equals(studentLogin + ".tar"))
            {
                return handin;
            }
        }

        return null;
    }

    /**
     * Returns the Files for each handin for this project. If this method has
     * not been called before it will load all of the handins.
     *
     * @return handins
     */
    private Collection<File> getHandins()
    {
        //If handins have not been requested yet, load them
        if(_handins == null)
        {
            _handins = Allocator.getGeneralUtilities().getFiles(this.getHandinPath(), "tar");
        }
        return _handins;
    }

    /**
     * Helper method to generate handin path. Relies upon the handin directory
     * specified by constants, the name of this project, and the current year.
     *
     * @return handin path
     */
    private String getHandinPath()
    {
        String path = Allocator.getConstants().getHandinDir()
                      + this.getAssignment().getName() + "/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
        return path;
    }

    /**
     * Checks whether or not a student login has a handin for this project.
     *
     * @param studentLogin
     * @return if student has handin for this project
     */
    public boolean hasHandin(String studentLogin)
    {
        return (this.getHandin(studentLogin) != null);
    }

    /**
     * Returns all student logins that have a handin
     *
     * @return all student logins that have a handin
     */
    public Collection<String> getHandinLogins()
    {
        Vector<String> logins = new Vector<String>();

        for (File handin : this.getHandins())
        {
            //Split at the . in the filename
            //So if handin is "jak2.tar", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }
        return logins;
    }

    /**
     * Code directory is:
     *
     * /course/<course>/grading/.<ta login>/<assignment name>/<student login>/
     *
     */
    protected String getStudentCodeDirectory(String studentLogin)
    {
        return Allocator.getGeneralUtilities().getUserGradingDirectory() +
               this.getAssignment().getName() + "/" + studentLogin + "/";
    }

    /**
     * Untars a student's handin.
     *
     * @param studentLogin
     */
    protected void untar(String studentLogin)
    {
        if(!_untarredStudents.contains(studentLogin))
        {
            //Create an empty folder for grading compiled student code
            String compileDir = this.getStudentCodeDirectory(studentLogin);
            Allocator.getGeneralUtilities().makeDirectory(compileDir);

            //untar student handin
            Allocator.getGeneralUtilities().untar(this.getHandin(studentLogin).getAbsolutePath(), compileDir);

            //record that student's code has been untarred
            _untarredStudents.add(studentLogin);
        }
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
                    Allocator.getGeneralUtilities().getFiles(this.getStudentCodeDirectory(studentLogin), fileType);
            sourceFiles.addAll(files);
        }

        return sourceFiles;
    }

    protected abstract String[] getSourceFileTypes();
    
    public abstract void openCode(String studentLogin);

    public abstract void run(String studentLogin);

    public abstract boolean hasDemo();

    public abstract void runDemo();

    public abstract boolean hasTester();

    public abstract void runTester(String studentLogin);

    public abstract boolean isValid();
}