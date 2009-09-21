package GradingCommander;

import cs015.tasupport.grading.Constants;
import cs015.tasupport.grading.projects.Project;
import cs015.tasupport.grading.projects.ProjectManager;
import cs015.tasupport.utils.BashConsole;
import cs015.tasupport.utils.Utils;
import emailer.EmailGUI;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

/* GradingCommander.java
 *
 * This class contains (static) methods that get called by the GradingCommanderGUI class
 * to actually provide the functionality its buttons promise.
 *
 */
public class GradingCommander {

    /**
     * This method runs a demo of the current project using the existing
     * 'cs015_runDemo' script
     *
     * STABLE 9/18/09
     *
     * @param project
     */
    public static void demoProject(String project) {
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("cs015_runGradeDemo " + project);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Untars each student's code for the given project
     * @param assignmentList
     * @param studentList
     *
     * STABLE 9/19/09
     */
    public static void untar(JList assignmentList, JList studentList) {
        ArrayList<String> studentLogins = new ArrayList<String>();
        int size = studentList.getModel().getSize();
        if(size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        ProjectManager.untar(Project.getInstance((String) assignmentList.getSelectedValue()), studentLogins);
    }

    /**
     * This method prints the code of all students the TA has been assigned to grade for
     * the current project.  It opens a pop-up window that allows the TA to select which printer should
     * be used (only allows bw3, bw4, and bw5).
     *
     * STABLE but non-idea, 9/19/09
     *
     * @param project
     * @param assignmentList
     */
    public static void printAll(String project, JList studentList) {
        Vector<String> studentLogins = new Vector<String>();
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        String printer = GradingCommander.getPrinter("Choose printer on which to print all students' code");
        if (printer != null) {
            for (String sL : studentLogins) {
                printStudentProject(project, sL, printer);
            }
        }
    }

    /**
     * This method prints the code of the student passed in as the second parameter for the project
     * passed in as the first parameter on the printer passed in as the third parameter
     *
     * //TODO: Rewrite to use Josh's bash console
     *
     * @param project
     * @param login
     * @param printer
     */
    public static void printStudentProject(String project, String studentLogin, String printer) {
        if (printer == null) {
                printer = GradingCommander.getPrinter("Choose printer on which to print student code");
        }
        String printCommand = new String("cs015_gradingPrint " + printer + " " + Constants.GRADER_PATH + Utils.getUserLogin() + "/" + project + "/.code/" + studentLogin + "/" + project + "/*.java");
        BashConsole.write(printCommand);
    }

    /**
     * Compiles the currently selected student's code for the currently selected project
     * @param project
     * @param login
     */
    public static void compileStudentProject(String project, String login) {
        //remove old class files
        ProjectManager.deleteClassFiles(Project.getInstance(project), login);
        
        //compile possibly modified java files
        ProjectManager.compile(Project.getInstance(project), login);
    }

    /**
     * Runs the currently selected student's code for the currently selected project
     * @param project
     * @param login
     */
    public static void runStudentProject(String project, String login) {
        ProjectManager.execute(Project.getInstance(project), login);
    }

    /** This method indicates whether there is a tester for the currently selected project
     *  This is used to control whether the GUI's runTesterButton should be enabled or disabled
     *
     * @param asgn
     * @return
     */
    public static boolean hasTester(String asgn) {
        // TODO: change to get from some kind of config file
        if (asgn.equals("Cartoon") || asgn.equals("Swarm") || asgn.equals("Tetris") || asgn.equals("PizzaDex")) {
            return true;
        }
        return false;
    }

    /**
     * Opens the student's code in Kate
     *
     * @param project - the project name that should be opened
     * @param login - the login of the student whose project should be opened
     * @throws IOException
     *
     * STABLE 9/19/09
     */
    public static void openStudentProject(String project, String login) {
        //TODO: need to add option to open GFX code (or figure out based on whether project uses it)
        String path = ProjectManager.getStudentSpecificDirectory(Project.getInstance(project), login) + project + "/";
        final String cmd = "kate " + path + "*.java";

        System.out.println("Open command: " + cmd);

        BashConsole.writeThreaded(cmd);
    }

    public static void runTester(String asgn, String student) {
        final String testCommand;
        if (asgn.equals("PizzaDex")) {
            testCommand = "cs015_pizzaTest " + student;
        }
        else {
            testCommand = "cs015_gfxTest " + student;
        }

        BashConsole.writeThreaded(testCommand);
    }

    public static void notifyStudents(JList assignmentList, JList studentList) {
        ListModel m = studentList.getModel();
        String bccStringBuilder = "";
        for (int i = 0; i < m.getSize(); i++) {
            bccStringBuilder += ((String) m.getElementAt(i)).trim() + "@cs.brown.edu,";
        }
        EmailGUI eg = new EmailGUI(new String[0], new String[0], bccStringBuilder.split(","), "[cs015] " + (String) assignmentList.getSelectedValue() + " Graded", (String) assignmentList.getSelectedValue() + " has been graded and is available for pickup in the handback bin.");
        eg.setTitle(Utils.getUserLogin() + "@cs.brown.edu - Send Email");
        eg.setTo(Constants.GRADES_TA); //need to notify grades TA when grading is complete
        //TODO: fix this (check with Paul)
//        try {
//            eg.setIconImage(ImageIO.read(getClass().getResource("/GradingCommander/icons/submit.png")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        eg.setVisible(true);
    }

    public static void printGRDFiles(String assignment) {
        String printer = GradingCommander.getPrinter("Select printer to print .GRD files");
        String printCommand = "lpr -P" + printer + " " + ProjectManager.getUserGradingDirectory() + assignment + "/*.grd";
       
        BashConsole.writeThreaded(printCommand);
    }

    public static void submitXMLFiles(String assignment) {
        Runtime r = Runtime.getRuntime();
        String copyCommand = "cp " + ProjectManager.getUserGradingDirectory() + assignment + "/*.xml " + Constants.GRADER_SUBMIT_PATH + assignment + "/" + Utils.getUserLogin() + "/";
        try {
            //TODO: add error-checking; needs to make directories when they don't exist
            r.exec(copyCommand);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void removeCodeDirectories(Vector<String> _selected) {
        for (String s: _selected) {
            ProjectManager.removeCodeDirectory(Project.getInstance(s));
        }
    }

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * @param message
     * @return
     */
    private static String getPrinter(String message) {
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }
}
