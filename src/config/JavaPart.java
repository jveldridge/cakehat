package config;

import java.io.File;
import java.util.Collection;
import utils.Allocator;
import utils.BashConsole;

/**
 * A Java subclass of CodePart.
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
class JavaPart extends CodePart
{
    public JavaPart(Assignment asgn, String name, int points)
    {
        super(asgn, name, points);
    }

    @Override
    public void openCode(String studentLogin)
    {
        //Untar if necesary
        this.untar(studentLogin);

        //Open in Kate
        
        //additional */ is to open code in all directories handin in
        String path = this.getStudentCodeDirectory(studentLogin) + "*/";

        String cmd = "kate " + path + "*.java";

        BashConsole.writeThreaded(cmd);
    }

    @Override
    public void run(String studentLogin)
    {
        //Untar if necesary
        this.untar(studentLogin);

        //Remove any class files around, compile the code, then execute it
        this.deleteCompiledFiles(studentLogin);
        this.compile(studentLogin);
        this.execute(studentLogin);
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
                                            (this.getStudentCodeDirectory(studentLogin), "class");

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
    private void compile(String studentLogin)
    {
        Allocator.getGeneralUtilities().compileJava
                (this.getStudentCodeDirectory(studentLogin), this.getRunProperty("classpath"));
    }

    /**
     * Executes a student project.
     *
     * @param studentLogin
     */
    private void execute(String studentLogin)
    {
        String main = "";

        if(_runMode.equalsIgnoreCase("specify-main"))
        {
            main = this.getRunProperty("main");
        }
        else if(_runMode.equalsIgnoreCase("find-main"))
        {
            main = this.findMain(studentLogin);
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
            return;
        }

        Allocator.getGeneralUtilities().executeJavaInVisibleTerminal
                (this.getStudentCodeDirectory(studentLogin), main,
                 this.getRunProperty("classpath"), this.getRunProperty("librarypath"),
                 studentLogin + "'s " + this.getAssignment().getName());
    }

    /**
     * TODO: Write this method.
     *
     * Finds the mainlines in a Java project. If multiple are found, prompts the
     * user via a pop-up box to choose one.
     *
     * @param studentLogin
     * @return
     */
    private String findMain(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasDemo()
    {
        return (_demoMode != null);
    }

    @Override
    public void runDemo()
    {
        if(_demoMode.equalsIgnoreCase("jar"))
        {
            this.runJarDemo();
        }
        else if(_demoMode.equals("class"))
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
     * TODO: Write this method!
     *
     * Runs a demo that is compiled class files. Supports running the files with
     * both a classpath and a library path.
     */
    private void runClassDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        if(this.hasDemoProperty("librarypath"))
        {
            cmd += " -Djava.library.path=" + this.getDemoProperty("librarypath");
        }

        //Add classpath if it has it
        if(this.hasDemoProperty("classpath"))
        {
            cmd += " -classpath " + this.getDemoProperty("classpath");
        }

        cmd += " -jar " + this.getDemoProperty("jar-loc");

        BashConsole.writeThreaded(cmd);
    }

    @Override
    public boolean hasTester()
    {
        return (_testerMode != null);
    }

    @Override
    public void runTester(String studentLogin)
    {
        //Untar if necesary
        this.untar(studentLogin);


        if(_testerMode.equalsIgnoreCase("compile-with"))
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
        String testerPath = this.getTesterProperty("tester-loc");
        String testerName = testerPath.substring(testerPath.lastIndexOf("/")+1);

        String copyPath = this.getStudentCodeDirectory(studentLogin) +
                          this.getTesterProperty("package").replace(".", "/") + "/" + testerName;

        //Copy file into student's code directory, print error and bail if copy fails
        if(!Allocator.getGeneralUtilities().copyFile(testerPath, copyPath))
        {
            System.err.println("Could not test " + studentLogin + "'s " + this.getName());
            System.err.println("Error in copying " + testerPath + " to " + copyPath);
            return;
        }

        //Compile
        this.compile(studentLogin);
        
        //Delete the copied tester source file as it is no longer needed
        new File(copyPath).delete();

        //Run tester
        String main = this.getTesterProperty("package") + "." + testerName.replace(".java", "");
        Allocator.getGeneralUtilities().executeJavaInVisibleTerminal
                                         (this.getStudentCodeDirectory(studentLogin),
                                          main,
                                          this.getRunProperty("classpath"),
                                          "Testing " + studentLogin + "'s " + this.getAssignment().getName());
    }

    /**
     * TODO: Come up with validity checks.
     * 
     * @return
     */
    @Override
    public boolean isValid()
    {
        return true;
    }

    private static String[] _sourceFileTypes = { "java" };
    
    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

}