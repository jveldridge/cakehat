package gradesystem.config;

import gradesystem.utils.Allocator;
import gradesystem.utils.BashConsole;

/**
 * A Java/C++ (make) subclass of CodePart.
 *
 * * Below are the following modes and properties. ( ) indicate the arguement is
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
 * @author aunger
 */
class MakeHandin extends JavaHandin {
    //Valid RUN, DEMO, & TESTER values

    //Valid RUN, DEMO, & TESTER values
    private static final String MAKE = "make", MAIN = "main", CLASSPATH = "classpath",
            LIBRARY_PATH = "librarypath", JAR = "jar", COMMAND = "command",
            JAR_LOC = "jar-loc", CLASS = "class", CODE_LOC = "code-loc", SCRIPT = "script";
    private static final LanguageSpecification.Mode //Run modes
            RUN_ANT_MODE = new LanguageSpecification.Mode(MAKE,
            new LanguageSpecification.Property(COMMAND, true)),
            //Demo modes
            DEMO_JAR_MODE = new LanguageSpecification.Mode(JAR,
            new LanguageSpecification.Property(JAR_LOC, true),
            new LanguageSpecification.Property(CLASSPATH, false),
            new LanguageSpecification.Property(LIBRARY_PATH, false)),
            DEMO_SCRIPT_MODE = new LanguageSpecification.Mode(SCRIPT,
            new LanguageSpecification.Property(COMMAND, true)),
            DEMO_CLASS_MODE = new LanguageSpecification.Mode(CLASS,
            new LanguageSpecification.Property(CODE_LOC, true),
            new LanguageSpecification.Property(MAIN, true),
            new LanguageSpecification.Property(CLASSPATH, false),
            new LanguageSpecification.Property(LIBRARY_PATH, false));
    //The specification of how this handin can be configured
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("Java",
            new LanguageSpecification.Mode[]{RUN_ANT_MODE},
            new LanguageSpecification.Mode[]{DEMO_JAR_MODE, DEMO_SCRIPT_MODE, DEMO_CLASS_MODE},
            new LanguageSpecification.Mode[]{});

    public MakeHandin(Assignment asgn, String name, int points) {
        super(asgn, name, points);
    }

    @Override
    public void run(String studentLogin) {
        //Untar if necesary
        this.untar(studentLogin);

        //Calls ant
        this.runMake(studentLogin);
    }

    /**
     * Executes a student project.
     *
     * @param studentLogin
     */
    private void runMake(String studentLogin) {
        executeMakeInVisibleTerminal(this.getStudentHandinDirectory(studentLogin), studentLogin,
                studentLogin + "'s " + this.getAssignment().getName());
    }

    @Override
    public void runTester(String studentLogin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runDemo() {
        if (_demoMode.equalsIgnoreCase(SCRIPT)) {
            BashConsole.writeThreaded(this.getDemoProperty(COMMAND));
        }
    }

    /**
     * Executes ant command in a separate visible terminal.
     *
     *
     * @param dirPath the path to the package
     * @param termName what the title bar of the terminal will display
     */
    private void executeMakeInVisibleTerminal(String dirPath, String studentLogin,
            String termName) {

        //Build command to call xterm to run the code
        String runCmd = "cd " + dirPath + studentLogin + "/projects/" + this.getAssignment().getName() + "; make run";
        Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(termName, runCmd);
    }

    @Override
    public boolean hasPrint() {
        return false;
    }

    private static final String[] _sourceFileTypes = { "java", "c", "cpp", "h" };

    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }
}
