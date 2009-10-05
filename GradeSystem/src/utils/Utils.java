package utils;

import java.io.*;
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

public class Utils {

    public static boolean sendMail(String[] to, String[] cc, String[] bcc, String subject, String body, String[] attachmentNames) {
        try {
            String stringBuilder = "mutt -s \"" + subject + "\"";
            if (Arrays.toString(cc).length() > 2) {
                stringBuilder += " -c " + Arrays.toString(cc).replace(",", "").replace("[", "").replace("]", "");
            }
            if (Arrays.toString(bcc).length() > 2) {
                stringBuilder += " -b " + Arrays.toString(bcc).replace(",", "").replace("[", "").replace("]", "");
            }
            if (Arrays.toString(attachmentNames).length() > 2) {
                stringBuilder += " -a " + Arrays.toString(attachmentNames).replace(",", "").replace("[", "").replace("]", "");
            }
            stringBuilder += " -- " + Arrays.toString(to).replace(",", " ").replace("[", "").replace("]", "") + " <<< \"" + body + "\"";
            String[] cmd = {"/bin/sh", "-c", stringBuilder};
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            new ErrorView(e);
        }
        return false;
    }

    /**
     * Returns the user login.
     *
     * @return user login
     */
    public static String getUserLogin() {
        return System.getProperty("user.name");
    }

    /**
     * Gets a user's name.
     *
     * @param login the user's login
     * @return user name
     */
    public static String getUserName(String login) {
        Vector<String> toExecute = new Vector<String>();

        toExecute.add("snoop " + login);

        Collection<String> output = BashConsole.write(toExecute);

        for (String line : output) {
            if (line.startsWith("Name")) {
                String name = line.substring(line.indexOf(":") + 2, line.length());
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
    public static String[] getCS015Students() {
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
    public static boolean isUserCS015HTA() {
        String login = getUserLogin();

        for (String htaLogin : getMembers("cs015hta")) {
            if (htaLogin.equals(login)) {
                return true;
            }
        }

        return false;
    }

    public static String getProjectDirectory(Project p) {
        String d = Constants.COURSE_DIR + "/asgn/"+ p.getName() + "/grade/";
        return d;
    }

    /**
     * Returns the current year
     *
     * @return
     */
    public static int getCurrentYear() {
       // return 2008; //For testing purposes only
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Returns all members of a given group
     * @param group
     * @return
     */
    private static String[] getMembers(String group) {
        Vector<String> toExecute = new Vector<String>();

        toExecute.add("members " + group);

        Collection<String> output = BashConsole.write(toExecute);

        String result = output.iterator().next();

        String[] logins = result.split(" ");

        return logins;
    }

    //For testing purposes only
    public static void main(String[] args)
    {
    }

    /**
     * Turns a calendar into a String. Returned in the format as
     * YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return date and time formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     */
    public static String getCalendarAsString(Calendar entry)
    {
        if (entry == null)
        {
            return "";
        }

        String date = entry.get(Calendar.YEAR) +
                      //Add to month as it is zero indexed
                      "-" + ensureLeadingZero((entry.get(Calendar.MONTH) + 1)) +
                      "-" + ensureLeadingZero(entry.get(Calendar.DAY_OF_MONTH));
        String time = ensureLeadingZero(entry.get(Calendar.HOUR_OF_DAY)) +
                      ":" + ensureLeadingZero(entry.get(Calendar.MINUTE)) +
                      ":" + ensureLeadingZero(entry.get(Calendar.SECOND));

        return date + " " + time;
    }

    /**
     * Helper method for getCalendarAsString(...) to ensure that a 1 digit
     * number is returned with a leading zero when turned into a String.
     *
     * @param number
     * @return
     */
    private static String ensureLeadingZero(int number)
    {
        String numberS = number + "";

        if(numberS.length() != 2)
        {
            return  "0" + numberS;
        }

        return numberS;
    }

    /**
     * Converts a string formatted as either YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     * or YEAR-MONTH-DAY into a Calendar.
     *
     * @param timestamp formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND or YEAR-MONTH-DAY
     * @return a calendar
     */
    public static Calendar getCalendarFromString(String timestamp)
    {
        String year, month, day, time = "";

        //Try to split date from time
        String[] parts = timestamp.split(" ");

        //Date parts
        String[] dateParts = parts[0].split("-");
        year = dateParts[0];
        month = dateParts[1];
        day = dateParts[2];

        //If it has a time part
        if(parts.length == 2)
        {
            time = parts[1];
        }

        return getCalendar(year, month, day, time);
    }

    /**
     * Returns a Calendar from the Strings passed in.
     *
     * @param year
     * @param month
     * @param day
     * @param time formated as HOUR:MINUTE:SECOND
     * @return
     */
    public static Calendar getCalendar(String year, String month, String day, String time)
	{
		Calendar cal = new GregorianCalendar();

		//Try to convert all of the entries
		int yearI = 0, monthI = 0, dayI = 0, hourI = 0, minuteI = 0, secondI = 0;
		try
		{
			if(year != null && year.length() != 0)
			{
				yearI = Integer.valueOf(year);
			}
			if(month != null && month.length() != 0)
			{
				monthI = Integer.valueOf(month);
			}
			if(day != null && day.length() != 0)
			{
				dayI = Integer.valueOf(day);
			}

			if(time != null)
			{
				String[] timeParts = time.split(":");
				if(timeParts.length == 3)
				{
					hourI = Integer.valueOf(timeParts[0]);
					minuteI = Integer.valueOf(timeParts[1]);
					secondI = Integer.valueOf(timeParts[2]);
				}
			}
		}
		catch(Exception e) { }

		//Set fields
		monthI--; //Because months are zero indexed
		cal.set(yearI, monthI, dayI, hourI, minuteI, secondI);

		return cal;
	}
    
    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file specified by the file path.
     *
     * @param filePath
     * @return last modified date
     */
    public static Calendar getModifiedDate(String filePath) {
        return getModifiedDate(new File(filePath));
    }

    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file.
     *
     * @param file
     * @return last modified date
     */
    public static Calendar getModifiedDate(File file) {
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
    public static boolean isBeforeDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency) {
        deadline = ((Calendar) deadline.clone());
        deadline.add(Calendar.MINUTE, minutesOfLeniency);

        return toCheck.before(deadline);
    }

    /**
     * Extracts a tar file.
     *
     * @param tarPath the absolute path of the tar file
     * @param destPath the directory the tar file will be expanded into
     */
    public static void untar(String tarPath, String destPath) {
        String cmd = "tar -xf " + tarPath + " -C " + destPath;
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
        } catch (Exception e) {
        }
    }

    /**
     * Makes a directory.
     *
     * @param dirPath
     * @return successful creation of directory
     */
    public static boolean makeDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }

        return true;
    }

    /**
     * Removes a directory and all of its files and subdirectories.
     *
     * @param dirPath
     */
    public static void removeDirectory(String dirPath) {
        String cmd = "rm -rf " + dirPath;

        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
        }
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
    public static boolean execute(String dirPath, String javaArg) {
        //Get the existing classpath, add dirPath to the classpath
        String classPath = dirPath + ":" + getClassPath();

        //TODO: Find a better way of creating the classpath
        classPath = classPath.replace("/home/"+ Utils.getUserLogin() + "/course/cs015", "");

        ProcessBuilder pb = new ProcessBuilder("java", "-classpath", classPath, javaArg);

        //Attempt to execute code
        try {
            pb.start();
        } catch (IOException e) {
            return false;
        }

        return true;
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
     * @param termName - what the title bar of the terminal will display
     */
    public static void executeInVisibleTerminal(String dirPath, String javaArg, String termName) {
        //Get the existing classpath, add dirPath to the classpath
        String classPath = dirPath + ":" + getClassPath();

        //TODO: Find a better way of creating the classpath
        classPath = classPath.replace("/home/"+ Utils.getUserLogin() + "/course/cs015", "");

        //Build command to call xterm to run the code
        String javaLoc = "/usr/bin/java";
        String javaCmd = javaLoc + " -classpath " + classPath + " " + javaArg;
        String terminalCmd =  "/usr/bin/xterm -title " + "\"" + termName + "\""
                                    + " -e " + "\"" + javaCmd + "; read" + "\"";
        
        //Execute the command in a seperate thread
        BashConsole.writeThreaded(terminalCmd);
    }

    public static String doubleToString(double value)
	{
		String text = Double.toString(value);
		String prettyText = "";

		char[] chars = text.toCharArray();
		int dotIndex = text.indexOf(".");

		int truncateLocation = dotIndex+2;
		int end = chars.length-1;

		if(truncateLocation < end)
		{
			int roundValue = Integer.valueOf(Character.toString(chars[truncateLocation+1]));
			if (roundValue >= 5)
			{
				int oldInt = Integer.valueOf(Character.toString(chars[truncateLocation]));
				int newInt = oldInt + 1;
				chars[truncateLocation] = Integer.toString(newInt).charAt(0);
			}
		}

		end = Math.min(end, truncateLocation);

		for( ; end>=0; end--) {
			if (chars[end] == '.') {
				end--;
				break;
			}
			if (chars[end] != '0') {
				break;
			}
		}
		for(int i=0; i<=end; i++) {
			prettyText += chars[i];
		}
		//split at the decimal if it has a decimal
		//then remove trailing zeros after the decimal

		return prettyText;
	}

    /**
     * Returns the current java class path.
     *
     * @return classPath
     */
    public static String getClassPath() {
        //When not running in Eclipse, only the line of code below is needed
        //return System.getProperty("java.class.path");

        //Hack to make this work properly with Eclipse
        String classPath = System.getProperty("java.class.path");

        if (classPath.contains("cs015.jar")) {
            return classPath;
        }

        Vector<String> toExecute = new Vector<String>();

        toExecute.add("echo $CLASSPATH");

        Collection<String> output = BashConsole.write(toExecute);

        if (output.size() > 0) {
            return output.iterator().next();
        } else {
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
    public static boolean compile(String dirPath) {
        //Get java compiler and file manager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //Set the class path to be the same as the one specified in CLASSPATH
        //That is, the one that would be used if a person used the terminal
        Collection<String> options = new Vector<String>();
        options.addAll(Arrays.asList("-classpath", getClassPath()));

        //Get all of the java files in dirPath
        Collection<File> files = getJavaFiles(dirPath);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        //Attempt to compile
        try {
            Boolean success = compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
            fileManager.close();

            if (success != null) {
                return success;
            } else {
                return false;
            }
        } catch (Exception e) {
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
    public static Collection<File> getJavaFiles(String dirPath) {
        return getFiles(dirPath, "java");
    }

    /**
     * Convenience method that uses getFiles(String dirPath, String extension)
     * to return all .class files in directory path passed in.
     *
     * @param dirPath
     * @return the .class files in the directory and subdirectories
     */
    public static Collection<File> getClassFiles(String dirPath) {
        return getFiles(dirPath, "class");
    }

    /**
     * Convience method that deletes all .class files in the
     * directory passed. Recurses into subdirectories.
     *
     * @param dirPath
     * @return success of deleting all files
     */
    public static boolean deleteClassFiles(String dirPath){
        boolean success = true;

        for(File file : getClassFiles(dirPath)){
            success &= file.delete();
        }

        return success;
    }

    /**
     * Returns all files in a directory, recursing into subdirectories, that
     * contain files with the specified extension.
     *
     * @param dirPath starting directory
     * @param extension the file extension, e.g. java or class
     * @return the files found with the specified extension
     */
    public static Collection<File> getFiles(String dirPath, String extension) {
        Vector<File> files = new Vector<File>();

        File dir = new File(dirPath);
        if(dir == null || !dir.exists()){
            return files;
        }
        for (String name : dir.list()) {
            File entry = new File(dir.getAbsolutePath() + "/" + name);
            //If it is a directory, recursively explore and add files ending with the extension
            if (entry.isDirectory()) {
                files.addAll(getFiles(entry.getAbsolutePath(), extension));
            }
            //Add if this entry is a file ending with the extension
            if (entry.isFile() && name.endsWith("." + extension)) {
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
