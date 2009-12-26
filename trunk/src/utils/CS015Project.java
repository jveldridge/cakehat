package utils;

import utils.Allocator;
import utils.BashConsole;
import utils.Project;

/**
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
    public static final String CLASSPATH =
        ":/course/cs015/lib/cs015.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dcore.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dutils.jar:" +
        "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/vecmath.jar:";

    //Library path - normally this wouldn't matter - but for Java3D it does (grr... I hate it!)
    //Necessary for Java3D. The fourth line is where the Java3D part is.
    public static final String LIBRARY_PATH = "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386/server:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/../lib/i386::" +
                                              "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/i386:" +
                                              "/usr/java/packages/lib/i386:/lib:/usr/lib";


    CS015Project(String name){
        super(name);
    }

    public void runDemo(){
       String cmd = "java -Djava.library.path=" + LIBRARY_PATH +
                     " -jar " + Allocator.getConstants().getDemoDir()
                              + this.getName() + "/" + this.getName() + ".jar";


        BashConsole.writeThreaded(cmd);
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
     * Compiles a student project. Do not run until untar has been
     * run on it.
     *
     * @param studentLogin
     */
    public void compile(String studentLogin) {
        String compileDir = this.getStudentCodeDirectory(studentLogin);
        Allocator.getGeneralUtilities().compileJava(compileDir, CLASSPATH);
    }

    /**
     * Executes a student project. Do not run until after compile(...)
     * has been run on it.
     *
     * @param studentLogin
     */
    private void execute(String studentLogin) {
        String compileDir = this.getStudentCodeDirectory(studentLogin);
        Allocator.getGeneralUtilities().executeJavaInVisibleTerminal(compileDir, this.getName() + ".App",
                                                                     CLASSPATH, LIBRARY_PATH,
                                                                     studentLogin + "'s " + this.getName());
    }
}
