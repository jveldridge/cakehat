package frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import utils.BashConsole;
import utils.Constants;
import utils.EmailView;
import utils.Project;
import utils.ProjectManager;
import utils.Utils;

/**
 * This class provides functionality for the buttons in the standard and final project
 * frontend graders.
 *
 * @author jeldridg
 */

public class FrontendUtils {

    /**
     * This method runs a project demo. Directly executes the jar.
     * @param project- the project whose demo should be run
     */
    public static void demoProject(String project) {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        
        String cmd = "java -Djava.library.path=" + Constants.LIBRARY_PATH + " -jar " + Constants.DEMO_DIR + project + "/" + project + ".jar";

        System.out.println(cmd);

        Collection<String> output = BashConsole.write(cmd);

        //print out any messages output by the BashConsole's execution
        for(String line : output){
            System.out.print("Output: " + line);
        }
    }

    /**
     * Untars each student's code for the currently selected project
     * from the handin directory to the appropriate .code directory
     *
     * @param project- currently selected project
     * @param studentList- list of students whose code must be untarred
     */
    public static void untar(String project, JList studentList) {
        ArrayList<String> studentLogins = new ArrayList<String>();
        int size = studentList.getModel().getSize();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        ProjectManager.untar(Project.getInstance(project), studentLogins);
    }

    /**
     * This method prints the code of all students the TA has been assigned to grade for
     * the current project.  It opens a pop-up window that allows the TA to select which printer should
     * be used (only allows bw3, bw4, and bw5).
     *
     * @param project
     * @param studentList
     */
    public static void printAll(String project, JList studentList) {
       if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        Vector<String> studentLogins = new Vector<String>();
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        String printer = FrontendUtils.getPrinter("Choose printer on which to print all students' code");
        if (printer != null) {
            boolean print_cover_sheet = true;           //want to print cover sheet for first student's code
            for (String sL : studentLogins) {
                printStudentProject(project, sL, printer, print_cover_sheet);
                print_cover_sheet = false;              //but not for subsequent students
            }
        }
    }

    /**
     * to be removed (gfx code should always be printed--print everything in subdirectories)--will fix soon!
     * @param project
     * @param studentList
     */
    public static void printAllGFX(String project, JList studentList) {
        Vector<String> studentLogins = new Vector<String>();
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        String printer = FrontendUtils.getPrinter("Choose printer on which to print all students' code");
        if (printer != null) {
            boolean first = true;
            for (String sL : studentLogins) {
                printStudentProjectGFX(project, sL, printer, first);
                first = false;
            }
        }
    }

    /**
     * Print particular student's code for a particular project on a particular printer
     *
     * @param project- project for which the student's code should be printed
     * @param login- login of student whose code should be printed
     * @param printer- printer on which code should be printed
     */
    public static void printStudentProject(String project, String studentLogin, String printer, boolean first) {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        if (printer == null) {
            printer = FrontendUtils.getPrinter("Choose printer on which to print student code");
        }
        String printCommand = new String("cs015_gradingPrint " + printer + " " + first + " " + studentLogin + " " + ProjectManager.getStudentProjectDirectory(Project.getInstance(project), studentLogin) + "*.java");
        Collection<String> ss = BashConsole.write(printCommand);
    }

    /**
     * to be removed (gfx code should always be printed--print everything in subdirectories)--will fix soon!
     * @param project
     * @param studentLogin
     * @param printer
     * @param first
     */
    public static void printStudentProjectGFX(String project,
                                              String studentLogin,
                                              String printer,
                                              boolean first)
    {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        if (printer == null) {
            printer = FrontendUtils.getPrinter("Choose printer on which to print student code");
        }
        String printCommand = new String("cs015_gradingPrint "
                                          + printer + " "
                                          + first + " "
                                          + studentLogin + " "
                                          + ProjectManager.getStudentGFXDirectory(Project.getInstance(project),
                                                                                  studentLogin)
                                          + "*.java");
        Collection<String> ss = BashConsole.write(printCommand);
    }

    /**
     * Compiles the currently selected student's code for the currently selected project
     * @param project- project to compile
     * @param login- login of student whose code will be compiled
     */
    public static void compileStudentProject(String project, String login) {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        //remove old class files
        ProjectManager.deleteClassFiles(Project.getInstance(project), login);

        //compile possibly modified java files
        ProjectManager.compile(Project.getInstance(project), login);
    }

    /**
     * Runs the currently selected student's code for the currently selected project
     * @param project- project to run
     * @param login- login of student whose code will be run
     */
    public static void runStudentProject(String project, String login) {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        ProjectManager.execute(Project.getInstance(project), login);
    }

    /** This method indicates whether there is a tester for the currently selected project
     *  This is used to control whether the GUI's runTesterButton should be enabled or disabled
     *
     * @param asgn
     * @return true if has tester, false otherwise
     */
    public static boolean hasTester(String asgn) {
        // TODO: change to get from some kind of config file
        if (asgn.equals("Cartoon") || asgn.equals("Swarm") || asgn.equals("Tetris") || asgn.equals("PizzaDex")) {
            return true;
        }
        return false;
    }

    /**
     * Opens the current student's project code (not including GFX) in Kate
     *
     * @param project - the project name that should be opened
     * @param login - the login of the student whose project should be opened
     * @author jeldridg
     */
    public static void openStudentProject(String project, String login) {
        if (project.equals("TASafehouse")) {
            project = "TASafeHouse";
        }
        
        //additional */ is to open code in all directories handin in
        String path = ProjectManager.getCodeStudentDirectory(Project.getInstance(project), login) + "*/";
        
        final String cmd = "kate " + path + "*.java";
        BashConsole.writeThreaded(cmd);
    }


    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settigns:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      grades TA & grades HTA
     *  BCC:     all students the user TA is assigned to grade for this assignment
     *  SUBJECT: "[cs015] <Asgn> Graded"
     *  MESSAGE: "<Asgn> has been graded and is available for pickup in the handback bin."
     * 
     * @param project
     * @param studentList
     */
    public static void notifyStudents(String project, JList studentList) {
        ListModel m = studentList.getModel();
        String bccStringBuilder = "";
        for (int i = 0; i < m.getSize(); i++) {
            bccStringBuilder += ((String) m.getElementAt(i)).trim() + "@cs.brown.edu,";
        }
        EmailView eg = new EmailView(new String[] {Utils.getUserLogin() + "@" + Constants.EMAIL_DOMAIN}, new String[] {Constants.GRADES_TA + "@" + Constants.EMAIL_DOMAIN, Constants.GRADES_HTA + "@" + Constants.EMAIL_DOMAIN}, bccStringBuilder.split(","), "[cs015] " + project + " Graded", project + " has been graded and is available for pickup in the handback bin.");
        eg.setTitle(Utils.getUserLogin() + "@cs.brown.edu - Send Email");
        eg.setVisible(true);
    }

    /**
     * Prints .GRD files for each student the user TA has graded
     * Calls getPrinter(...) and then prints using lpr
     * 
     * @param assignment- assignment for which .GRD files should be printed
     */
    public static void printGRDFiles(String assignment) {
        String printer = FrontendUtils.getPrinter("Select printer to print .GRD files");
        if (assignment.equals("TASafehouse")) {
            assignment = "TASafeHouse";
        }
        
        String printCommand = "lpr -P" + printer + " " + ProjectManager.getGRDFilePath(assignment);
        BashConsole.writeThreaded(printCommand);
    }

    /**
     * NEEDS REVISION--will comment after fixed
     * @param assignment
     */
    public static void submitXMLFiles(String assignment) {
        if (assignment.equals("TASafehouse")) {
            assignment = "TASafeHouse";
        }
        String dirPath = Constants.GRADER_SUBMIT_PATH + assignment + "/" + Utils.getUserLogin() + "/";
        Utils.makeDirectory(dirPath);
        String copyCommand = "cp " + ProjectManager.getUserGradingDirectory() + assignment + "/*.xml " + dirPath;
        BashConsole.write(copyCommand);
    }

    /**
     * Removes all code directories created during this run of the frontend grading interface
     *
     * @param _selected- vector of projects for which code directories have been created
     */
    public static void removeCodeDirectories(Vector<String> _selected) {
        for (String s : _selected) {
            ProjectManager.removeCodeDirectory(Project.getInstance(s));
        }
    }

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * @param message
     * @return the name of the printer selected
     */
    private static String getPrinter(String message) {
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }
}