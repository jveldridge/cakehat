package frontend.grader.rubric;

import java.io.File;

public class TesterMain 
{
	
	 private final static String SAVE_PATH = "/course/cs015/admin/uta/grading/";
	
	 private static String getGRDFilePath(Rubric rubric) 
	 {
		 String dir = SAVE_PATH + rubric.Grader.Acct + "/" + rubric.Name;
		 boolean dirExists = new File(dir).exists();
		 if(!dirExists) 
		 {
			 new File(dir).mkdirs();
		 }
		 String fileName = dir + "/" + rubric.Student.Acct + ".grd";
		 return fileName;
	 }
	 
	 public static void main(String[] args)
	    {
	    	//if (args.length == 1) {
	    	//	if(args[0].endsWith(".xml")) {
	    		/* 
	    		 * If XML file is specified, read in the XML.
	    		 */
	    		Rubric rubric = RubricManager.processXML("/course/cs015/admin/uta/grading/spoletto/Clock/csstudent.xml");
	    		//RubricManager.writeToXML(rubric, "/course/cs015/admin/uta/grading/spoletto/Clock/csstudent.xml");
	    		RubricManager.convertToGRD("/course/cs015/admin/uta/grading/spoletto/Clock/csstudent.xml", getGRDFilePath(rubric));
	    		//RubricManager.writeToGRD(rubric, getGRDFilePath(rubric));
	    	//	}
	    		
	    	//	else {
	        //		System.out.println("Usage: XMLReadMain <xmlfile>");
	    	//		System.exit(0);
	    	//	}
	    	//}
	    	
	    	//else {
	    	//	System.out.println("Usage: XMLReadMain <xmlfile>");
			//	System.exit(0);
			//}
	    }
}