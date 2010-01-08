package utils;

import utils.printing.PrintRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @deprecated To be replaced by config.JavaHandin
 * Subclass of Project specific to CS015
 *
 * @author jak2 (Joshua Kaplan)
 */
public class CS015Project extends Project {

    //TODO: consider putting classpath and library path in config file
    //      or some other non-compiled place because these will likely
    //      need to change every so often as new versions of Java and
    //      Java3D come out

    //Classpath
    //First line is the cs015 support jar
    //The next three lines are used by Java3D
    private static final String CLASSPATH =
        "/course/cs015/lib/cs015.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dcore.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dutils.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/vecmath.jar:";

    //Library path - normally this wouldn't matter - but for Java3D it does (grr... I hate it!)
    //Necessary for Java3D. The fourth line is where the Java3D part is.
    private static final String LIBRARY_PATH = "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386/server:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/../lib/i386::" +
                                              "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/i386:" +
                                              "/usr/java/packages/lib/i386:/lib:/usr/lib";

    CS015Project(String name){
        super(name);
    }

    //Name of the tester file it is looking for
    private static final String TESTER_NAME = "Tester.java";
    //Representation of tester file
    private File _tester = null;
    //If the tester file has been looked for yet, do not want to waste time
    //traversing the file system each time we need access to the tester
    private boolean _lookedForTester = false;

    /**
     * @author jak2 (Joshua Kaplan)
     * @date 12/26/2009
     *
     * Looks for this project's tester. If it exists it returns the File
     * representing this tester. If no tester was found then null is returned.
     *
     * @return tester
     */
    private File getTester(){
        //If the tester has already been looked for, look for what has been stored
        if(_lookedForTester){
            return _tester;
        }
        _lookedForTester = true;

        String testerDir = Allocator.getConstants().getTesterDir() + this.getName() + "/";

        //If a tester directory for this project exists
        if(new File(testerDir).exists()){
            //Get all files in directory and subdirectories recursively
            Collection<File> files = Allocator.getGeneralUtilities().getFiles(testerDir, "java");
            //Look for a file named with the specified tester name, if found return it
            for(File file : files){
                if(file.getName().equals(TESTER_NAME)){
                    _tester = file;
                    return file;
                }
            }
        }

        //No tester was found
        return null;
    }

    /**
     * @author jak2 (Joshua Kaplan)
     * @date 12/26/2009
     *
     * Whether or not this project has a tester.
     *
     * @return tester's existance
     */
    public boolean hasTester(){
        return (getTester() != null);
    }

    /**
     * @author jak2 (Joshua Kaplan)
     * @data 12/26/2009
     * 
     * Runs the tester on the specified student's handin of this project.
     * Results will be displayed in a terminal.
     *
     * @param studentLogin
     */
    public void runTester(String studentLogin){
        //Print error and bail if there is no tester to run
        if(!hasTester()){
            System.err.println(this.getName() + " does not have a tester.");
            return;
        }

        File tester = getTester();

        //Build path to where the tester file should be copied
        String testerPath = tester.getAbsolutePath();
        String testerDir = Allocator.getConstants().getTesterDir() + this.getName() + "/";
        String relativePath = testerPath.replace(testerDir, "");
        String copyPath = this.getStudentCodeDirectory(studentLogin) + relativePath;

        //Copy file into student's code directory, print error and bail if copy fails
        if(!Allocator.getGeneralUtilities().copyFile(tester, copyPath)){
            System.err.println("Could not test " + studentLogin + "'s " + this.getName());
            System.err.println("Error in copying " + testerPath + " to " + copyPath);
            return;
        }

        //Compile
        this.compile(studentLogin);

        //Delete tester source file as it is no longer needed
        new File(copyPath).delete();

        //Run tester
        String compileDir = this.getStudentCodeDirectory(studentLogin);
        String testerName = relativePath.replace("/", ".").replace(".java","");
        executeJavaInVisibleTerminal(compileDir, testerName,
                                                                     CLASSPATH, "Testing " + studentLogin + "'s " + this.getName());
    }

    public void runDemo(){
       String cmd = "java -Djava.library.path=" + LIBRARY_PATH +
                     " -jar " + Allocator.getConstants().getDemoDir()
                              + this.getName() + "/" + this.getName() + ".jar";


        BashConsole.writeThreaded(cmd);
    }

    public void print(String studentLogin, String printer){
        Collection<File> sourceFiles = Allocator.getGeneralUtilities().getFiles(
                             this.getStudentCodeDirectory(studentLogin), "java");

        PrintRequest request = null;
        try {
            request = new PrintRequest(sourceFiles,
                      Allocator.getGeneralUtilities().getUserLogin(), studentLogin);
        }
        catch (FileNotFoundException ex) {
            new ErrorView(ex);
        }

        Allocator.getLandscapePrinter().print(request, printer);
    }

    public void print(Iterable<String> studentLogins, String printer){
        Vector<PrintRequest> requests = new Vector<PrintRequest>();

        for(String studentLogin : studentLogins) {
            Collection<File> sourceFiles = Allocator.getGeneralUtilities().getFiles(
                                this.getStudentCodeDirectory(studentLogin), "java");

            try {
                PrintRequest request = new PrintRequest(sourceFiles,
                                           Allocator.getGeneralUtilities().getUserLogin(), studentLogin);
                requests.add(request);
             }
            catch (FileNotFoundException ex) {
                new ErrorView(ex);
            }
        }

        Allocator.getLandscapePrinter().print(requests, printer);
    }

    /**
     * Runs a Java project by deleting any pre-existing compiled files,
     * compiling the code, and then running in the code with a visible
     * terminal.
     *
     * @param studentLogin
     */
    public void run(String studentLogin){
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
    private boolean deleteCompiledFiles(String studentLogin){
        //Get all compiled files for this project for the specified student
        String compileDir = getStudentCodeDirectory(studentLogin);
        Collection<File> compiledFiles = Allocator.getGeneralUtilities().getFiles(compileDir, "class");

        //Keep track of success of deleting all of the files
        boolean success = true;

        for (File file : compiledFiles) {
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
    private void compile(String studentLogin) {
        String compileDir = this.getStudentCodeDirectory(studentLogin);
        compileJava(compileDir, CLASSPATH);
    }

    /**
     * Executes a student project. Do not run until after compile(...)
     * has been run on it.
     *
     * @param studentLogin
     */
    private void execute(String studentLogin) {
        String compileDir = this.getStudentCodeDirectory(studentLogin);
        executeJavaInVisibleTerminal(compileDir, this.getName() + ".App",
                                                                     CLASSPATH, LIBRARY_PATH,
                                                                     studentLogin + "'s " + this.getName());
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
    public boolean compileJava(String dirPath, String classpath) {
        //Get java compiler and file manager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //Tell the compiler to use the classpath if one was passed in
        Collection<String> options = null;
        if(classpath != null && !classpath.isEmpty()) {
            options = new Vector<String>();
            options.addAll(Arrays.asList("-classpath", classpath));
        }

        //Get all of the java files in dirPath
        Collection<File> files = Allocator.getGeneralUtilities().getFiles(dirPath, "java");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        //Attempt to compile
        try {
            Boolean success = compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
            fileManager.close();

            if (success != null) {
                return success;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Executes Java code in a separate visible terminal.
     *
     * If you were attempting to execute TASafeHouse and the main class
     * was located at /course/cs015/demos/TASafeHouse/App.class then
     * dirPath = /course/cs015/demos and javaArg = TASafeHouse.App
     *
     * @param dirPath the path to the package
     * @param javaArg the part to come after java (ex. java TASafeHouse.App)
     * @param termName what the title bar of the terminal will display
     */
    public void executeJavaInVisibleTerminal(String dirPath, String javaArg,
                                             String termName) {
        this.executeJavaInVisibleTerminal(dirPath, javaArg, "", termName);
    }

    /**
     * Executes Java code in a separate visible terminal.
     *
     * If you were attempting to execute TASafeHouse and the main class
     * was located at /course/cs015/demos/TASafeHouse/App.class then
     * dirPath = /course/cs015/demos and javaArg = TASafeHouse.App
     *
     * @param dirPath the path to the package
     * @param javaArg the part to come after java (ex. java TASafeHouse.App)
     * @param classpath the classpath to run this code with respect to
     * @param termName what the title bar of the terminal will display
     */
    public void executeJavaInVisibleTerminal(String dirPath, String javaArg,
                                             String classpath, String termName) {
        this.executeJavaInVisibleTerminal(dirPath, javaArg, classpath, null, termName);
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
     * @param classpath the classpath to run this code with respect to
     * @param libraryPath the library path to run this code with respect to
     * @param termName what the title bar of the terminal will display
     */
    public void executeJavaInVisibleTerminal(String dirPath, String javaArg,
                                             String classpath, String libraryPath,
                                             String termName) {
        //Adds the dirPath to the classpath
        classpath = dirPath + ":" +  classpath;

        //Build command to call xterm to run the code
        //Location of java
        String javaLoc = "/usr/bin/java";
        //Add java.library.path component if an arguement was passed in
        String javaLibrary = "";
        if(libraryPath != null && !libraryPath.isEmpty()){
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