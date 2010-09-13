package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import utils.Allocator;
import utils.BashConsole;
import utils.ErrorView;
import utils.printing.PrintRequest;

/**
 * A HANDIN that specifies a LANGUAGE.
 * May or may not have RUN, DEMO, & TESTER properties.
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

    /**
     * Every subclass must define:
     * public static final LanguageSpecification SPECIFICATION = ...;
     *
     * @param writer to write error messages to
     * @return LanguageSpecification
     */
    private LanguageSpecification getLanguageSpecification(StringWriter writer)
    {
        String errorStartMsg = "Cannot determine validity of " +
                               this.getAssignment().getName() + " - " + this.getName() +
                               " because " + this.getClass().getName();

        Field field = null;
        try
        {
            field = this.getClass().getDeclaredField("SPECIFICATION");
        }
        catch (Exception ex) { }

        if(field == null)
        {
            writer.append(errorStartMsg + " does not provide SPECIFICATION. \n");
        }
        else
        {
            //Check that its of the correct class
            LanguageSpecification spec = new LanguageSpecification();
            if(!field.getType().isInstance(spec))
            {
                writer.append(errorStartMsg + "has SPECIFICATION of type " + field.getType().getName() +
                              " but should have SPECIFICATION of type " + spec.getClass().getName() + ". \n");
            }

            //Check modifiers
            int modifiers = field.getModifiers();

            boolean isStatic = Modifier.isStatic(modifiers);
            boolean isFinal = Modifier.isFinal(modifiers);
            boolean isPublic = Modifier.isPublic(modifiers);
            if(!isPublic)
            {
                writer.append(errorStartMsg + " has non-public SPECIFICATION. \n");
            }
            if(!isStatic)
            {
                writer.append(errorStartMsg + " has non-static SPECIFICATION. \n");
            }
            if(!isFinal)
            {
                writer.append(errorStartMsg + " has non-final SPECIFICATION. \n");
            }
            if(isPublic && isStatic && isFinal)
            {
                try
                {
                    //If all of the criteria is met, return it
                    return (LanguageSpecification) field.get(null);
                }
                catch(Exception e)
                {
                    writer.append( "'s SPECIFICATION cannot be retrieved for an unknown reason. \n");
                }
            }
        }

        return null;
    }

    /**
     * Determines the validity of this CodeHandin. Checks that for each mode:
     * RUN, DEMO, & TESTER all of the required properties have been supplied.
     *
     * In order for this method to work each subclass must specify:
     * public static final LanguageSpecification SPECIFICATION = ...;
     *
     * This static field will be retrieved via reflection. If it is not provided
     * the validity will be returned as false, and errors will be written to the
     * writer provided.
     *
     * @param writer
     * @return
     */
    boolean checkValidity(StringWriter writer)
    {
        //Get specification
        LanguageSpecification specification = this.getLanguageSpecification(writer);

        //If no specification was given, automatically invalid
        if(specification == null)
        {
            return false;
        }

        boolean valid = true;

        //Run
        if(_runMode != null)
        {
            if(specification.hasRunMode(_runMode))
            {
                Collection<LanguageSpecification.Property> requiredProperties = specification.getRunMode(_runMode).getRequiredProperties();
                for(LanguageSpecification.Property property : requiredProperties)
                {
                    //Check if the property wasn't specificied
                    if(!this.hasRunProperty(property.getName()))
                    {
                        //Problem!
                        valid = false;

                        writer.append(this.getAssignment().getName() + " - " + this.getName() +
                                      "'s [" + _runMode + "] RUN mode requires property: [" +
                                      property.getName() + "] \n");
                    }
                }
            }
            else
            {
                writer.append(this.getAssignment().getName() + " - " + this.getName() +
                              " has specified an invalid RUN mode: [" + _runMode + "]. " +
                              "Valid modes: " + Arrays.toString(specification.getRunModes().toArray()) + ".");
                valid = false;
            }
        }

        //Demo
        if(_demoMode != null)
        {
            if(specification.hasDemoMode(_demoMode))
            {
                Collection<LanguageSpecification.Property> requiredProperties = specification.getDemoMode(_demoMode).getRequiredProperties();
                for(LanguageSpecification.Property property : requiredProperties)
                {
                    //Check if the property wasn't specificied
                    if(!this.hasDemoProperty(property.getName()))
                    {
                        //Problem!
                        valid = false;

                        writer.append(this.getAssignment().getName() + " - " + this.getName() +
                                      "'s [" + _demoMode + "] DEMO mode requires property: [" +
                                      property.getName() + "] \n");
                    }
                }
            }
            else
            {
                writer.append(this.getAssignment().getName() + " - " + this.getName() +
                              " has specified an invalid DEMO mode: [" + _demoMode + "]. " +
                              "Valid modes: " + Arrays.toString(specification.getDemoModes().toArray()) + ".");
                valid = false;
            }
        }

        //Tester
        if(_testerMode != null)
        {
            if(specification.hasTesterMode(_testerMode))
            {
                Collection<LanguageSpecification.Property> requiredProperties = specification.getTesterMode(_testerMode).getRequiredProperties();
                for(LanguageSpecification.Property property : requiredProperties)
                {
                    //Check if the property wasn't specificied
                    if(!this.hasTesterProperty(property.getName()))
                    {
                        //Problem!
                        valid = false;

                        writer.append(this.getAssignment().getName() + " - " + this.getName() +
                                      "'s [" + _testerMode + "] TESTER mode requires property: [" +
                                      property.getName() + "] \n");
                    }
                }
            }
            else
            {
                writer.append(this.getAssignment().getName() + " - " + this.getName() +
                              " has specified an invalid TESTER mode: [" + _testerMode + "]. " +
                              "Valid modes: " + Arrays.toString(specification.getTesterModes().toArray()) + ".");
                valid = false;
            }
        }

        return valid;
    }

    void setRunProperty(String key, String value)
    {
        _runProperties.put(key, value);
    }

    /**
     * If there is a run property with the specified key.
     *
     * @param key
     * @return
     */
    protected boolean hasRunProperty(String key)
    {
        return _runProperties.containsKey(key);
    }

    /**
     * Returns the run property specified by the key. If there is no property
     * specified by the key, returns the empty string.
     *
     * @param key
     * @return
     */
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

    void setDemoProperty(String key, String value)
    {
        _demoProperties.put(key, value);
    }

    /**
     * If there is a demo property with the specified key.
     *
     * @param key
     * @return
     */
    protected boolean hasDemoProperty(String key)
    {
        return _demoProperties.containsKey(key);
    }

    /**
     * Returns the demo property specified by the key. If there is no property
     * specified by the key, returns the empty string.
     *
     * @param key
     * @return
     */
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

    void setTesterProperty(String key, String value)
    {
        _testerProperties.put(key, value);
    }

    /**
     * If there is a tester property with the specified key.
     *
     * @param key
     * @return
     */
    protected boolean hasTesterProperty(String key)
    {
        return _testerProperties.containsKey(key);
    }

    /**
     * Returns the tester property specified by the key. If there is no property
     * specified by the key, returns the empty string.
     *
     * @param key
     * @return
     */
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

    void setRunMode(String mode)
    {
        _runMode = mode;
    }

    void setDemoMode(String mode)
    {
        _demoMode = mode;
    }

    void setTesterMode(String mode)
    {
        _testerMode = mode;
    }

    /**
     * Print code for the specified student on the specified printer.
     *
     * @param studentLogin
     * @param printer
     */
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

    /**
     * Print code for the specified students on the specified printer.
     *
     * @param studentLogins
     * @param printer
     */
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

    /**
     * Opens all of the student's code in Kate. Feel free to override this
     * method in subclasses to open the code in a different editor.
     *
     * @param studentLogin
     */
    public void openCode(String studentLogin)
    {
        //Build command to open all of the source files in Kate
        Collection<File> files = getSourceFiles(studentLogin);
        if(!files.isEmpty())
        {
            String cmd = "kate ";
            for(File file : files)
            {
                cmd += file.getAbsolutePath() + " ";
            }

            BashConsole.writeThreaded(cmd);
        }
    }

    /**
     * Retrieves all of the source files for the specified student. Uses
     * the abstract getSourceFileTypes() methods to determine the file
     * extensions of the source files.
     *
     * @param studentLogin
     * @return
     */
    private Collection<File> getSourceFiles(String studentLogin) {
        //make sure the student's code has been extracted from the tar before asking for the source files
        this.untar(studentLogin);

        Collection<File> sourceFiles = new Vector<File>();

        for(String fileType : this.getSourceFileTypes()) {
            Collection<File> files =
                    Allocator.getGeneralUtilities().getFiles(this.getStudentHandinDirectory(studentLogin), fileType);
            sourceFiles.addAll(files);
        }

        return sourceFiles;
    }


    @Override
    public boolean hasOpen()
    {
        return true;
    }

    @Override
    public boolean hasPrint()
    {
        return true;
    }

    @Override
    public boolean hasRun()
    {
        return (_runMode != null);
    }

    @Override
    public boolean hasDemo()
    {
        return (_demoMode != null);
    }

    @Override
    public boolean hasTester()
    {
        return (_testerMode != null);
    }

    protected abstract String[] getSourceFileTypes();
}