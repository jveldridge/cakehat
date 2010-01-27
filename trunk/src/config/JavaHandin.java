package config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import utils.Allocator;
import utils.BashConsole;
import utils.ErrorView;

/**
 * A Java subclass of CodePart.
 *
 * Below are the following modes and properties. ( ) indicate the arguement is
 * optional.
 * 
 * RUN
 *     MODE
 *         specify-main
 *             main
 *             (classpath)
 *             (librarypath)
 *         find-main
 *             (classpath)
 *             (librarypath)
 * DEMO
 *     MODE
 *         jar
 *             jar-loc
 *             (classpath)
 *             (librarypath)
 *         class
 *             code-loc
 *             main
 *             (classpath)
 *             (librarypath)
 *
 * TESTER
 *     MODE
 *         compile-with
 *             tester-loc
 *             package
 *
 * @author jak2
 */
class JavaHandin extends CodeHandin
{
    //Valid RUN, DEMO, & TESTER values
    private static final String
    SPECIFY_MAIN="specify-main", MAIN="main", CLASSPATH="classpath",
    LIBRARY_PATH="librarypath", FIND_MAIN="find-main", JAR="jar",
    JAR_LOC="jar-loc", CLASS="class", CODE_LOC="code-loc",
    COMPILE_WITH="compile-with", TESTER_LOC="tester-loc", PACKAGE="package";

    //Run modes
    private static final LanguageSpecification.Mode
    RUN_SPECIFY_MAIN_MODE = new LanguageSpecification.Mode(SPECIFY_MAIN,
                            new LanguageSpecification.Property(MAIN, true),
                            new LanguageSpecification.Property(CLASSPATH, false),
                            new LanguageSpecification.Property(LIBRARY_PATH, false)),
    RUN_FIND_MAIN_MODE = new LanguageSpecification.Mode(FIND_MAIN,
                         new LanguageSpecification.Property(CLASSPATH, false),
                         new LanguageSpecification.Property(LIBRARY_PATH, false)),
    //Demo modes
    DEMO_JAR_MODE = new LanguageSpecification.Mode(JAR,
                    new LanguageSpecification.Property(JAR_LOC, true),
                    new LanguageSpecification.Property(CLASSPATH, false),
                    new LanguageSpecification.Property(LIBRARY_PATH, false)),
    DEMO_CLASS_MODE = new LanguageSpecification.Mode(CLASS,
                      new LanguageSpecification.Property(CODE_LOC, true),
                      new LanguageSpecification.Property(MAIN, true),
                      new LanguageSpecification.Property(CLASSPATH, false),
                      new LanguageSpecification.Property(LIBRARY_PATH, false)),
    //Tester modes
    TESTER_COMPILE_WITH_MODE = new LanguageSpecification.Mode(COMPILE_WITH,
                               new LanguageSpecification.Property(TESTER_LOC, true),
                               new LanguageSpecification.Property(PACKAGE, true));

    
    //The specification of how this handin can be configured
    public static final LanguageSpecification SPECIFICATION =
    new LanguageSpecification("Java",
                              new LanguageSpecification.Mode[]{ RUN_SPECIFY_MAIN_MODE, RUN_FIND_MAIN_MODE},
                              new LanguageSpecification.Mode[]{ DEMO_JAR_MODE, DEMO_CLASS_MODE},
                              new LanguageSpecification.Mode[]{ TESTER_COMPILE_WITH_MODE } );
    

    public JavaHandin(Assignment asgn, String name, int points)
    {
        super(asgn, name, points);
    }

    @Override
    public void run(String studentLogin)
    {
        //Untar if necesary
        this.untar(studentLogin);

        //Remove any class files around
        this.deleteCompiledFiles(studentLogin);

        //Compile code, if sucessful execute code
        if(this.compile(studentLogin))
        {
            this.execute(studentLogin);
        }
    }

    /**
     * Deletes all compiled files in a student's project. It is safe to run
     * even if there are no compiled files in the project.
     *
     * @param studentLogin
     * @return success of deletion operation
     */
    private boolean deleteCompiledFiles(String studentLogin)
    {
        //Get all compiled files for this project for the specified student
        Collection<File> compiledFiles = Allocator.getGeneralUtilities().getFiles
                                            (this.getStudentHandinDirectory(studentLogin), "class");

        //Keep track of success of deleting all of the files
        boolean success = true;

        for (File file : compiledFiles)
        {
            success &= file.delete();
        }

        return success;
    }

    /**
     * Compiles a student project. Do not run until untar has been
     * run on it.
     *
     * @param studentLogin
     */
    private boolean compile(String studentLogin)
    {
        return compileJava(this.getStudentHandinDirectory(studentLogin), this.getRunProperty(CLASSPATH));
    }

    /**
     * Executes a student project.
     *
     * @param studentLogin
     */
    private void execute(String studentLogin)
    {
        String main = null;

        if(_runMode.equalsIgnoreCase(SPECIFY_MAIN))
        {
            main = this.getRunProperty(MAIN);
        }
        else if(_runMode.equalsIgnoreCase(FIND_MAIN))
        {
            main = this.findMain(studentLogin);
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
            return;
        }

        //If there is no main either specified or found
        if(main == null || main.isEmpty())
        {
            JOptionPane.showMessageDialog(null,
                                          "No main available to execute code",
                                          "Cannot run",
                                          JOptionPane.ERROR_MESSAGE,
                                          null
                                         );
        }
        //Execute with specified classpath and librarypath
        else
        {
            executeJavaInVisibleTerminal(this.getStudentHandinDirectory(studentLogin), main,
                                        this.getRunProperty(CLASSPATH), this.getRunProperty(LIBRARY_PATH),
                                        studentLogin + "'s " + this.getAssignment().getName());
        }
    }

    /**
     * Finds the mainlines in a Java project. If multiple are found, prompts the
     * user via a pop-up box to choose one. If none are found, null is returned.
     *
     * @param studentLogin
     * @return fully qualified path of the class with the mainline
     */
    private String findMain(String studentLogin)
    {
        String directory = this.getStudentHandinDirectory(studentLogin);

        ClassLoader urlCl = null;
        try
        {
            //Places to look for code
            Vector<URL> locations = new Vector<URL>();

            //Directory where the code was turned in
            locations.add(new URL("file:"+directory));

            //Supporting classes in the classpath
            String[] paths = this.getRunProperty(CLASSPATH).split(":");
            for(String path : paths)
            {
                locations.add(new URL("file:"+path));
            }

            //Load
            urlCl = URLClassLoader.newInstance(locations.toArray(new URL[0]));
        }
        catch (MalformedURLException e)
        {
            new ErrorView(e);
        }
        try
        {
            //Full qualified path of classes with mainline
            Vector<String> entryPoints = new Vector<String>();

            //Get files
            Collection<File> files = Allocator.getGeneralUtilities().getFiles(directory,"class");

            //Load classes
            for(File file : files)
            {
                //Take file path and turn it into a fully qualified class name
                String className = file.getAbsolutePath();
                className = className.replace(directory, "");
                className = className.replace(".class","");
                className = className.replace("/", ".");

                //Load the class
                Class<?> classLoaded = urlCl.loadClass(className);

                //Attempt to get the main method from this class
                Class[] argTypes = new Class[] { String[].class };
                try
                {
                    Method main = classLoaded.getDeclaredMethod("main", argTypes);
                    if(main != null)
                    {
                     entryPoints.add(classLoaded.getName());
                    }
                }
                catch(Exception e) { }
            }

            //No main method found
            if(entryPoints.size() == 0)
            {
                return null;
            }
            //Exactly one found
            else if(entryPoints.size() == 1)
            {
                return entryPoints.firstElement();
            }
            //If more than one entry, pop up a dialog letting the user pick one
            else
            {
                Object[] possibilities = entryPoints.toArray();
                String result = (String) JOptionPane.showInputDialog(
                                    null,
                                    "Choose an entry point:",
                                    "Entry point",
                                    JOptionPane.OK_OPTION,
                                    new ImageIcon(getClass().getResource("/gradesystem/resources/icons/32x32/go-next.png")),
                                    possibilities,
                                    possibilities[0]);

                //If a string was returned
                if((result != null) && !result.isEmpty())
                {
                    return result;
                }
            }
        }
        catch (Exception e)
        {
            new ErrorView(e);
        }

        return null;
    }

    @Override
    public void runDemo()
    {
        if(_demoMode.equalsIgnoreCase(JAR))
        {
            this.runJarDemo();
        }
        else if(_demoMode.equals(CLASS))
        {
            this.runClassDemo();
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this demo mode: " + _demoMode);
        }
    }

    /**
     * Runs a demo that is compiled class files. Supports running the files with
     * both a classpath and a library path.
     */
    private void runClassDemo()
    {
        //Build command
        String cmd = "java ";

        //Add java.library.path if it has it
        if(this.hasDemoProperty(LIBRARY_PATH))
        {
            cmd += " -Djava.library.path=" + this.getDemoProperty(LIBRARY_PATH);
        }

        //Add classpath
        String classpath = this.getDemoProperty(CODE_LOC) + ":" + this.getDemoProperty(CLASSPATH);
        cmd += " -classpath " + classpath;

        //Add fully qualified path of main class
        cmd += " " + this.getDemoProperty(MAIN);

        //Execute command
        BashConsole.writeThreaded(cmd);
    }

    /**
     * Runs a demo that is in jar format. Supports running the jar with both a
     * classpath and library path.
     */
    private void runJarDemo()
    {
        //Build command
        String cmd = "java ";

        //Add java.library.path component if an arguement was passed in
        if(this.hasDemoProperty(LIBRARY_PATH))
        {
            cmd += " -Djava.library.path=" + this.getDemoProperty(LIBRARY_PATH);
        }

        //Add classpath if it has it
        if(this.hasDemoProperty(CLASSPATH))
        {
            cmd += " -classpath " + this.getDemoProperty(CLASSPATH);
        }

        cmd += " -jar " + this.getDemoProperty(JAR_LOC);

        BashConsole.writeThreaded(cmd);
    }

    @Override
    public void runTester(String studentLogin)
    {
        //Untar if necesary
        this.untar(studentLogin);

        if(_testerMode.equalsIgnoreCase(COMPILE_WITH))
        {
            runCompileWithTester(studentLogin);
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this tester mode: " + _testerMode);
        }
    }

    private void runCompileWithTester(String studentLogin)
    {
        //Get name of tester file from path to tester
        String testerPath = this.getTesterProperty(TESTER_LOC);
        String testerName = testerPath.substring(testerPath.lastIndexOf("/")+1);

        String copyPath = this.getStudentHandinDirectory(studentLogin) +
                          this.getTesterProperty(PACKAGE).replace(".", "/") + "/" + testerName;

        //Copy file into student's code directory, print error and bail if copy fails
        if(!Allocator.getGeneralUtilities().copyFile(testerPath, copyPath))
        {
            System.err.println("Could not test " + studentLogin + "'s " + this.getName());
            System.err.println("Error in copying " + testerPath + " to " + copyPath);
            return;
        }

        //Compile
        boolean compilationSuccess = this.compile(studentLogin);
        
        //Delete the copied tester source file as it is no longer needed
        new File(copyPath).delete();

        //Run tester if compilation succeeded
        if(compilationSuccess)
        {
            String main = this.getTesterProperty(PACKAGE) + "." + testerName.replace(".java", "");
            executeJavaInVisibleTerminal(this.getStudentHandinDirectory(studentLogin),
                                         main,this.getRunProperty(CLASSPATH),this.getRunProperty(LIBRARY_PATH),
                                         "Testing " + studentLogin + "'s " + this.getAssignment().getName());
        }
    }

    private static final String[] _sourceFileTypes = { "java" };
    
    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

    /**
     * Compiles java code, returns whether the code compiled successfully.
     * Pass in the top level directory, subdirectories containing
     * java files will also be compiled.
     *
     * Any compiler errors or other messages will be printed to the console
     * that this grading system program was executed from.
     *
     *
     * @param dirPath The directory and its subdirectories to look for Java files to compile
     * @param classpath The classpath to compile this code with, null or an empty String may be passed in
     * @return success of compilation
     */
    private boolean compileJava(String dirPath, String classpath)
    {
        //Get java compiler and file manager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //Tell the compiler to use the classpath if one was passed in
        Collection<String> options = null;
        if(classpath != null && !classpath.isEmpty())
        {
            options = new Vector<String>();
            options.addAll(Arrays.asList("-classpath", classpath));
        }

        //Listens to errors
        DiagnosticCollector collector = new DiagnosticCollector();

        //Get all of the java files in dirPath
        Collection<File> files = Allocator.getGeneralUtilities().getFiles(dirPath, "java");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        //Attempt to compile
        try
        {
            //Compile
            Boolean success = compiler.getTask(null, fileManager, collector, options, null, compilationUnits).call();

            //If not succesful, display compiler message
            if(success == null || !success)
            {
                //Display compiler information
                String msg = "Compiler output: \n\n";
                for(Object o : collector.getDiagnostics())
                {
                    msg += o.toString();

                    //If not the last entry, append a new line
                    if(o != collector.getDiagnostics().get(collector.getDiagnostics().size() - 1))
                    {
                        msg += "\n";
                    }
                }
                JOptionPane.showMessageDialog(null, msg, "Compilation Failed", JOptionPane.ERROR_MESSAGE, null);

                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            new ErrorView(e);
        }
        finally
        {
            try
            {
                //Close file manager
                fileManager.close();
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
        
        return false;
    }

    /**
     * Executes Java code in a separate visible terminal.
     *
     * If you were attempting to execute TASafeHouse and the main class
     * was located at /course/cs015/demos/TASafeHouse/App.class then
     * dirPath = /course/cs015/demos and javaArg = TASafeHouse.App
     *
     *
     * @param dirPath the path to the package
     * @param javaArg the part to come after java (ex. java TASafeHouse.App)
     * @param classpath the classpath to run this code with respect to, can be an empty string or null
     * @param libraryPath the library path to run this code with respect to, can be an empty string or null
     * @param termName what the title bar of the terminal will display
     */
    private void executeJavaInVisibleTerminal(String dirPath, String javaArg,
                                             String classpath, String libraryPath,
                                             String termName)
    {
        //Adds the dirPath to the classpath
        if(classpath == null)
        {
            classpath = "";
        }
        classpath = dirPath + ":" +  classpath;

        //Build command to call xterm to run the code
        //Location of java
        String javaLoc = "/usr/bin/java";
        //Add java.library.path component if an arguement was passed in
        String javaLibrary = "";
        if(libraryPath != null && !libraryPath.isEmpty())
        {
            javaLibrary= " -Djava.library.path=" + libraryPath;
        }
        //Add classpath
        String javaClassPath = " -classpath " + classpath;
        //Put together entire java comand
        String javaCmd = javaLoc + javaLibrary + javaClassPath + " " + javaArg;

        //Combine java command into command to launch an xterm window
        String terminalCmd = "/usr/bin/xterm -title " + "\"" + termName + "\"" + " -e " + "\"" + javaCmd + "; read" + "\"";

        //Execute the command in a seperate thread
        BashConsole.writeThreaded(terminalCmd);
    }

}