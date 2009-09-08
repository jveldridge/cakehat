package codesupport;

public class ProjectManager {

    private static final String ROOT_GRADING_DIR = "/course/cs015/admin/uta/grading/";
    private static final String CODE_DIR = ".code/";

    /**
     * Creates a directory for the code for this assignment.
     *
     * @param asgn
     */
    public static void createCodeDirectory(Project asgn) {
        Utils.makeDirectory(getCodeDirectory(asgn));
    }

    /**
     * Removes the directory created by createCodeDirectory(...)
     *
     * @param asgn
     */
    public static void removeCodeDirectory(Project asgn) {
        Utils.removeDirectory(getCodeDirectory(asgn));
    }

    /**
     * Untars all of the handins for the specified assignment and
     * student logins.
     *
     * @param asgn
     * @param studentLogins
     */
    public static void untar(Project asgn, Iterable<String> studentLogins) {
        for (String login : studentLogins) {
            untar(asgn, login);
        }
    }

    /**
     * Untars a student's handin for a given assignment.
     *
     * @param asgn
     * @param studentLogin
     */
    private static void untar(Project asgn, String studentLogin) {
        //Create an empty folder for grading compiled student code
        String compileDir = getStudentSpecificDirectory(asgn, studentLogin);
        Utils.makeDirectory(compileDir);

        //untar student handin
        Utils.untar(asgn.getHandin(studentLogin).getAbsolutePath(), compileDir);
    }

    /**
     * Compiles a student assignment. Do not run until untar has been
     * run on it.
     *
     * @param asgn
     * @param studentLogin
     */
    public static void compile(Project asgn, String studentLogin) {
        String compileDir = getStudentSpecificDirectory(asgn, studentLogin);
        Utils.compile(compileDir);
    }

    /**
     * Executes a student assignment. Do not run until after compile(...)
     * has been run on it.
     *
     * @param asgn
     * @param studentLogin
     */
    public static void execute(Project asgn, String studentLogin) {
        String compileDir = getStudentSpecificDirectory(asgn, studentLogin);
        Utils.execute(compileDir, asgn.getName() + ".App");
    }

    private static String getStudentSpecificDirectory(Project asgn, String studentLogin) {
        return getCodeDirectory(asgn) + studentLogin + "/";
    }

    private static String getCodeDirectory(Project asgn) {
        return getUserGradingDirectory() + asgn.getName() + "/" + CODE_DIR;
    }

    private static String getUserGradingDirectory() {
        return ROOT_GRADING_DIR + Utils.getUserLogin() + "/";
    }
}
