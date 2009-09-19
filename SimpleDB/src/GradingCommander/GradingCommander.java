package GradingCommander;

import cs015.tasupport.grading.projects.ProjectManager;
import cs015.tasupport.utils.Utils;
import emailer.EmailGUI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import javax.imageio.ImageIO;
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
     * FUNCTIONAL 9/18/09
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
     */
    public static void untar(JList assignmentList, JList studentList) {
        ArrayList<String> studentLogins = new ArrayList<String>();
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }
        ProjectManager.untar(ProjectManager.getProjectFromString((String) assignmentList.getSelectedValue()), studentLogins);
    }

    /**
     * Compiles each student's code for the given project
     * @param project
     * @param studentList
     */
    public static void compileAll(String project, JList studentList) {
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            compileStudentProject(project, (String) studentList.getModel().getElementAt(i));
        }
    }

    /**
     * This method prints the code of all students the TA has been assigned to grade for
     * the current project.  It opens a pop-up window that allows the TA to select which printer should
     * be used (only allows bw3, bw4, and bw5).
     *
     * Fully functional (I think) as of 9/13/09 (excepting what's mentioned in printStudentProject(...))
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
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png");
        String printer = (String) JOptionPane.showInputDialog(new JFrame(), "Chose printer:", "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
        for (String sL : studentLogins) {
            printStudentProject(project, sL, printer);
        }
    }

    /**
     * This method prints the code of the student passed in as the second parameter for the project
     * passed in as the first parameter on the printer passed in as the third parameter
     *
     * //TODO: Should probably have some kind of error-checking (e.g., if cdCommand is not executed correctly)
     *
     * @param project
     * @param login
     * @param printer
     */
    public static void printStudentProject(String project, String login, String printer) {
        Runtime r = Runtime.getRuntime();
        File wd = new File("/home/");
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            Object[] printerChoices = {"bw3", "bw4", "bw5"};
            ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
            if (printer == null) {
                printer = (String) JOptionPane.showInputDialog(new JFrame(), "Chose printer:", "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
            }
            if ((printer != null) && (printer.length() > 0)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
                String cdCommand = new String("cd " + login + "/course/cs015/" + project);
                String printCommand = new String("cs015_gradingPrint " + printer + " *.java");
                System.out.println("print command is: " + printCommand);
                out.println(cdCommand);
                out.println(printCommand);
                out.println("exit");
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                    proc.waitFor();
                    in.close();
                    out.close();
                    proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Compiles the currently selected student's code for the currently selected project
     * @param project
     * @param login
     */
    public static void compileStudentProject(String project, String login) {
        ProjectManager.compile(ProjectManager.getProjectFromString(project), login);
    }

    /**
     * Runs the currently selected student's code for the currently selected project
     * @param project
     * @param login
     */
    public static void runStudentProject(String project, String login) {
        ProjectManager.execute(ProjectManager.getProjectFromString(project), login);
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
     */
    public static void openStudentProject(String project, String login) {

        //TODO: need to add option to open GFX code (or figure out based on whether project uses it)

        Runtime r = Runtime.getRuntime();
        File wd = new File("/home/");
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            String cdCommand = new String("cd " + login + "/course/cs015/" + project);
            out.println(cdCommand);
            out.println("kate *.java &");
            out.println("exit");
        }
    }

    //TODO!
    public static void gradeProject(String asgn, String student) {
        System.out.println("Opening rubric for project " + asgn + " for student " + student);
    }

    //TODO: Deal with need for input from cs015_pizzaTest
    public static void runTester(String asgn, String student) {
        if (asgn.equals("PizzaDex")) {
            String testCommand = "cs015_pizzaTest " + student;
            try {
                Process p = Runtime.getRuntime().exec(testCommand);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String testCommand = "cs015_gfxTest " + student;
            try {
                Process p = Runtime.getRuntime().exec(testCommand);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void notifyStudents(JList assignmentList, JList studentList) {
        ListModel m = studentList.getModel();
        String bccStringBuilder = "";
        for (int i = 0; i < m.getSize(); i++) {
            bccStringBuilder += ((String) m.getElementAt(i)).trim() + "@cs.brown.edu,";
        }
        EmailGUI eg = new EmailGUI(new String[0], new String[0], bccStringBuilder.split(","), "[cs015] " + (String) assignmentList.getSelectedValue() + " Graded", (String) assignmentList.getSelectedValue() + " has been graded and is available for pickup in the handback bin.");
        eg.setTitle(Utils.getUserLogin() + "@cs.brown.edu - Send Email");
//        try {
//            eg.setIconImage(ImageIO.read(getClass().getResource("/GradingCommander/icons/submit.png")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        eg.setVisible(true);
    }

    public static void printGRDFiles(String assignment) {
        System.out.println("called printGRDFiles");
        String printer = GradingCommander.getPrinter(null);
        Runtime r = Runtime.getRuntime();
        String printCommand = "lpr -P" + printer + " /course/cs015/admin/uta/grading/" + Utils.getUserLogin() + "/" + assignment + "/*.grd";
        try {
            System.out.println("printCommand is " + printCommand);
            r.exec(printCommand);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void submitXMLFiles() {
        System.out.println("called submitXMLFiles");
    }

    private static String getPrinter(String printer) {
        if (printer != null)
            return printer;
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        printer = (String) JOptionPane.showInputDialog(new JFrame(), "Chose printer:", "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
        return printer;
    }
}
