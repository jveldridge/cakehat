package GradingCommander;

import codesupport.Project;
import codesupport.ProjectManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;


public class GradingCommander {

    public static void demoProject(String project) {
		Runtime r = Runtime.getRuntime();
        try {
            r.exec("cs015_runDemo " + project);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static void compileAll(String project, String[] studentLogins) {
		for (String sL : studentLogins) {
			compileStudentProject(project, sL);
		}
	}
	
	public static void printAll(String project, JList assignmentList) {
        Vector<String> studentLogins = new Vector<String>();
        int size = assignmentList.getModel().getSize();
        for (int i=0; i<size; i++) {
            studentLogins.add((String)assignmentList.getModel().getElementAt(i));
        }
		for (String sL : studentLogins) {
			System.out.println(sL);
            //printStudentProject(project, sL);
		}
	}

	public static void compileStudentProject(String project, String login) {
        ProjectManager.compile(new Project(project), login);
	}
	
	public static void runStudentProject(String project, String login) {
		System.out.println("Running project " + project + " for student " + login);
        ProjectManager.execute(new Project(project), login);
	}
	
	public static void printStudentProject(String project, String login) {
        Runtime r = Runtime.getRuntime();
		File wd = new File("/home/");  
		Process proc = null; 
		try { 
		   proc = Runtime.getRuntime().exec("/bin/bash", null, wd); 
		} 
		catch (IOException e) { 
		   e.printStackTrace(); 
		} 
		if (proc != null) { 
		   Object[] printerChoices = {"bw2", "bw3", "bw4", "bw5"};
           ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
           String printer = (String)JOptionPane.showInputDialog(new JFrame(),"Chose printer:", "Select Printer", JOptionPane.PLAIN_MESSAGE,icon,printerChoices,"bw3");
           if ((printer != null) && (printer.length() > 0)) {
               BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
               PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
               String cdCommand = new String("cd " + login + "/course/cs015/" + project);
               String printCommand = new String("lpr -P" + printer + " *.java");
               System.out.println("printCommand is " + printCommand);
               out.println(cdCommand);
               //out.println(printCommand);
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
               }
               catch (Exception e) {
                  e.printStackTrace();
               }
           }
		}
	}
	
	/**
	 * Opens the student's code in Kate
	 * 
	 * @param project - the project name that should be opened
	 * @param login - the login of the student whose project should be opened
	 * @throws IOException
	 */
	public static void openStudentProject(String project, String login) {

		//@TODO: need to add option to option GFX code (or figure out based on whether project uses it)
		
		Runtime r = Runtime.getRuntime();
		File wd = new File("/home/");  
		Process proc = null; 
		try { 
		   proc = Runtime.getRuntime().exec("/bin/bash", null, wd); 
		} 
		catch (IOException e) { 
		   e.printStackTrace(); 
		}
		if (proc != null) { 
		   BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
		   PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true); 
		   String cdCommand = new String("cd " + login + "/course/cs015/" + project);
		   out.println(cdCommand); 
		   out.println("kate *.java &");
		   out.println("exit");
		}
	}

    public static boolean isTesterAvailable(String project) {
        return false;
    }

    static void gradeProject(String asgn, String student) {
        System.out.println("Opening rubric for project " + asgn + " for student " + student);
    }

    static void runTester(String asgn, String student) {
        
    }

}
