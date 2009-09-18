package cs015.tasupport.utils;

import cs015.tasupport.grading.Constants;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Utils
{	
	/**
	 * Returns the user login.
	 * 
	 * @return user login
	 */
	public static String getUserLogin()
	{
		return System.getProperty("user.name");
	}
	
	/**
	 * Gets a user's name.
	 * 
	 * @param login the user's login
	 * @return user name
	 */
	public static String getUserName(String login)
	{
		Vector<String> toExecute = new Vector<String>();
		
		toExecute.add("snoop " + login);
		
		Collection<String> output = BashConsole.write(toExecute);
		
		for(String line : output)
		{
			if(line.startsWith("Name"))
			{
				String name = line.substring(line.indexOf(":")+2, line.length());
				return name;
			}
		}
		return "UNKNOWN_LOGIN";
	}
	
	/**
	 * Returns the logins of all CS015 students.
	 * 
	 * @return
	 */
	public static String[] getCS015Students()
	{
        List<String> l = Arrays.asList(getMembers("cs015student"));
        Vector<String> v = new Vector<String>(l);
        v.remove(Constants.TEST_ACCOUNT);
		return v.toArray(new String[0]);
	}
	
	/**
	 * Returns if the current user is a CS015 HTA.
	 * 
	 * @return
	 */
	public static boolean isUserCS015HTA()
	{
		String login = getUserLogin();
		
		for(String htaLogin : getMembers("cs015hta"))
		{
			if(htaLogin.equals(login))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the current year
	 * 
	 * @return
	 */
	public static int getCurrentYear()
	{
		return 2008; //For testing purposes only
		//return Calendar.getInstance().get(Calendar.YEAR);
	}

	/**
	 * Returns all members of a given group
	 * @param group
	 * @return
	 */
	private static String[] getMembers(String group)
	{
		Vector<String> toExecute = new Vector<String>();
		
		toExecute.add("members " + group);
		
		Collection<String> output = BashConsole.write(toExecute);
		
		String result = output.iterator().next();
		
		String[] logins = result.split(" ");
		
		return logins;
	}
	
	//Test
	public static void main(String[] args)
	{
	}
		
	public static String getCalendarAsString(Calendar entry)
	{
		if(entry == null)
		{
			return "";
		}
		
		String date = entry.get(Calendar.YEAR) + "-" + (entry.get(Calendar.MONTH)+1) + //Add to month as it is zero indexed
		"-" + entry.get(Calendar.DAY_OF_MONTH);
		String time = entry.get(Calendar.HOUR_OF_DAY) + ":" + entry.get(Calendar.MINUTE) +
		":" + entry.get(Calendar.SECOND);
		
		return date + " " + time;
	}
	
	/**
	 * Returns an object that represents the last modified date and time
	 * of the file specified by the file path.
	 * 
	 * @param filePath
	 * @return last modified date
	 */
	public static Calendar getModifiedDate(String filePath)
	{
		return getModifiedDate(new File(filePath));
	}
	
	/**
	 * Returns an object that represents the last modified date and time
	 * of the file.
	 * 
	 * @param file
	 * @return last modified date
	 */
	public static Calendar getModifiedDate(File file)
	{	
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(file.lastModified());
		
		return calendar;
	}

	/**
	 * Determines if a calendar, given a certain amount of leniency, is
	 * before the deadline.
	 * 
	 * @param toCheck the calendar to check if it is before the deadline
	 * @param deadline the deadline
	 * @param minutesOfLeniency the amount of leniency in minutes to be granted after the deadline
	 * @return
	 */
	public static boolean isBeforeDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency)
	{
		deadline = ((Calendar)deadline.clone());
		deadline.add(Calendar.MINUTE, minutesOfLeniency);
		
		return toCheck.before(deadline);
	}
	
	/**
	 * Extracts a tar file.
	 * 
	 * @param tarPath the absolute path of the tar file
	 * @param destPath the directory the tar file will be expanded into
	 */
	public static void untar(String tarPath, String destPath)
	{
		String cmd = "tar -xf " + tarPath + " -C " + destPath;
		try
		{
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
		}
		catch (Exception e) { }
	}
	
	/**
	 * Makes a directory.
	 * 
	 * @param dirPath
	 * @return successful creation of directory
	 */
	public static boolean makeDirectory(String dirPath)
	{		
		File dir = new File(dirPath);
		if(!dir.exists())
		{
			return dir.mkdirs();
		}
		
		return true;
	}
	
	/**
	 * Removes a directory and all of its files and subdirectories.
	 * 
	 * @param dirPath
	 */
	public static void removeDirectory(String dirPath)
	{
		String cmd = "rm -rf " + dirPath;
		
		try
		{
			Runtime.getRuntime().exec(cmd);
		}
		catch (IOException e) {	}
	}
	
	/**
	 * Executes the java code in a separate thread.
	 * 
	 * If you were attempting to execute TASafeHouse and the main class
	 * was located at /course/cs015/demos/TASafeHouse/App.class then
	 * pathToPackage = /course/cs015/demos and javaArg = TASafeHouse.App
	 * 
	 * @param dirPath - the path to the package
	 * @param javaArg - the part to come after java (ex. java TASafeHouse.App)
	 * @return whether the code was successfully executed
	 */
	public static boolean execute(String dirPath, String javaArg)
	{
		//Get the existing classpath, add dirPath to the classpath
		String classPath = getClassPath() + ":" + dirPath;
		ProcessBuilder pb = new ProcessBuilder("java", "-classpath", classPath, javaArg);
		
		//Attempt to execute code
		try
		{
			pb.start();
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the current java class path.
	 * 
	 * @return classPath
	 */
	public static String getClassPath()
	{
		//When not running in Eclipse, only the line of code below is needed
		//return System.getProperty("java.class.path");
		
		//Hack to make this work properly with Eclipse
		String classPath = System.getProperty("java.class.path");
		
		if(classPath.contains("cs015.jar"))
		{
			return classPath;
		}
		
		Vector<String> toExecute = new Vector<String>();
		
		toExecute.add("echo $CLASSPATH");
		
		Collection<String> output = BashConsole.write(toExecute);
		
		if(output.size() > 0)
		{
			return output.iterator().next();
		}
		else
		{
			return "";
		}
	}
		
	/**
	 * Compiles code, returns whether the code compiled successfully.
	 * Pass in the top level directory, subdirectories containing
	 * java files will also be compiled.
	 * 
	 * @param dirPath
	 * @return success of compilation
	 */
	public static boolean compile(String dirPath)
	{
		//Get java compiler and file manager
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
       
		//Set the class path to be the same as the one specified in CLASSPATH
		//That is, the one that would be used if a person used the terminal
		Collection<String> options = new Vector<String>();
		options.addAll(Arrays.asList("-classpath",getClassPath())); 
       
		//Get all of the java files in dirPath
		Collection<File> files = getJavaFiles(dirPath);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
	   
		//Attempt to compile
		try
		{
			Boolean success = compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
			fileManager.close();
			
			if(success != null)
			{
				return success;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Convenience method that uses getFiles(String dirPath, String extension)
	 * to return all .java files in directory path passed in.
	 * 
	 * @param dirPath
	 * @return the .java files in the directory and subdirectories
	 */
	public static Collection<File> getJavaFiles(String dirPath)
	{
		return getFiles(dirPath, "java");
	}
	
	/**
	 * Returns all files in a directory, recursing into subdirectories, that
	 * contain files with the specified extension.
	 * 
	 * @param dirPath starting directory
	 * @param extension the file extension, e.g. java or class
	 * @return the files found with the specified extension
	 */
	public static Collection<File> getFiles(String dirPath, String extension)
	{
		Vector<File> files = new Vector<File>();
		
		File dir = new File(dirPath);
		for(String name : dir.list())
		{
			File entry = new File(dir.getAbsolutePath() + "/" + name);
			//If it is a directory, recursively explore and add files ending with the extension
			if(entry.isDirectory())
			{
				files.addAll(getFiles(entry.getAbsolutePath(), extension));
			}
			//Add if this entry is a file ending with the extension
			if(entry.isFile() && name.endsWith("."+extension))
			{
				files.add(entry);
			}
		}
		
		return files;
	}
	

	
	
	/*
	public static void printProperties()
	{
		for(Object key : System.getProperties().keySet())
		{
			System.out.println(key + " - " + System.getProperties().get(key));
		}
	}
	*/
}
