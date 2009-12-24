package utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.*;

public class Utils {

    /**
     * sends email through the cs department smtps server
     * attaches a list of files to the email
     *
     * @author aunger 12/10/09
     *
     * @param from
     * @param to array of addresses
     * @param cc array of addresses
     * @param bcc array of addresses
     * @param subject
     * @param body
     * @param attachmentNames files paths to the attachments
     */
    public static void sendMail(String from, String[] to, String[] cc, String[] bcc, String subject, String body, String[] attachmentNames) {
        System.setProperty("javax.net.ssl.trustStore", Constants.EMAIL_CERT_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", Constants.EMAIL_CERT_PASSWORD);
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", Constants.EMAIL_HOST);
        props.put("mail.smtps.user", Constants.EMAIL_ACCOUNT);
        props.put("mail.smtp.host", Constants.EMAIL_HOST);
        props.put("mail.smtp.port", Constants.EMAIL_PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.socketFactory.port", Constants.EMAIL_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        try {
            Authenticator auth = new javax.mail.Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Constants.EMAIL_ACCOUNT, Constants.EMAIL_PASSWORD);
                }
            };
            Session session = Session.getInstance(props, auth);
            //session.setDebug(true);

            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(from));

            if (Arrays.toString(to).length() > 2) { //checks that "to" array is not empty
                for (String s : to) {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(s));
                }
            }
            if (Arrays.toString(cc).length() > 2) {
                for (String s : cc) {
                    msg.addRecipient(Message.RecipientType.CC, new InternetAddress(s));
                }
            }
            if (Arrays.toString(bcc).length() > 2) {
                for (String s : bcc) {
                    msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(s));
                }
            }

            // multi part message to add parts to
            Multipart multipart = new MimeMultipart();

            // add message text
            MimeBodyPart mainTextPart = new MimeBodyPart();
            mainTextPart.setContent("<html>" + body + "</html>", "text/html");
            multipart.addBodyPart(mainTextPart);

            //for each file to attach
            if (Arrays.toString(attachmentNames).length() > 2) { //if not empty
                for (String s : attachmentNames) {
                    // add attachments
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(s);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(s.substring(s.lastIndexOf("/")+1));
                    multipart.addBodyPart(attachmentPart);
                }
            }
            // Put parts in message
            msg.setContent(multipart);

            // send message
            Transport.send(msg);
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    public static String readFile(File aFile) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    text.append(line);
                    text.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return text.toString();
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
        String d = Constants.COURSE_DIR + "/asgn/" + p.getName() + "/grade/";
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
    public static void main(String[] args) {
    }

    /**
     * Turns a calendar into a String. Returned in the format as
     * YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return date and time formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     */
    public static String getCalendarAsString(Calendar entry) {
        if (entry == null) {
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
    private static String ensureLeadingZero(int number) {
        String numberS = number + "";

        if (numberS.length() != 2) {
            return "0" + numberS;
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
    public static Calendar getCalendarFromString(String timestamp) {
        String year, month, day, time = "";

        //Try to split date from time
        String[] parts = timestamp.split(" ");

        //Date parts
        String[] dateParts = parts[0].split("-");
        year = dateParts[0];
        month = dateParts[1];
        day = dateParts[2];

        //If it has a time part
        if (parts.length == 2) {
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
    public static Calendar getCalendar(String year, String month, String day, String time) {
        Calendar cal = new GregorianCalendar();

        //Try to convert all of the entries
        int yearI = 0, monthI = 0, dayI = 0, hourI = 0, minuteI = 0, secondI = 0;
        try {
            if (year != null && year.length() != 0) {
                yearI = Integer.valueOf(year);
            }
            if (month != null && month.length() != 0) {
                monthI = Integer.valueOf(month);
            }
            if (day != null && day.length() != 0) {
                dayI = Integer.valueOf(day);
            }

            if (time != null) {
                String[] timeParts = time.split(":");
                if (timeParts.length == 3) {
                    hourI = Integer.valueOf(timeParts[0]);
                    minuteI = Integer.valueOf(timeParts[1]);
                    secondI = Integer.valueOf(timeParts[2]);
                }
            }
        } catch (Exception e) {
        }

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

        File destFolder = new File(destPath);
        File subFolders[] = destFolder.listFiles(); // list all subfolders of .code/<studentLogin>/
        for (File folder : subFolders) {
            FilenameFilter filter = new FilenameFilter() { // make a filter for .*.java and .*.java~ files
              public boolean accept(File dir, String name) {
                  return (name.startsWith(".") && (name.endsWith(".java")  || name.endsWith(".java~")));
              }
            };
            File toDelete[] = folder.listFiles(filter); // list all the files to delete
            for (File delete : toDelete) {
                delete.delete(); // remove the file
            }
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
        classPath = classPath.replace("/home/" + Utils.getUserLogin() + "/course/cs015", "");

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

        //Build command to call xterm to run the code
        String javaLoc = "/usr/bin/java";
        String javaLibrary = " -Djava.library.path=" + Constants.LIBRARY_PATH;
        String javaClassPath = " -classpath " + classPath;
        String javaCmd = javaLoc + javaLibrary + javaClassPath + " " + javaArg;
        String terminalCmd = "/usr/bin/xterm -title " + "\"" + termName + "\"" + " -e " + "\"" + javaCmd + "; read" + "\"";

        //Execute the command in a seperate thread
        BashConsole.writeThreaded(terminalCmd);
    }

    /**
     * NOT STABLE - DOES NOT SUPPORT ADVENTURE.
     * KNOWN ISSUE: ON CLOSE OF STUDENT CODE - CLOSES GRADING PROGRAM.
     *
     * Executes the java code in a separate thread.
     *
     * If you were attempting to execute TASafeHouse and the main class
     * was located at /course/cs015/demos/TASafeHouse/App.class then
     * pathToPackage = /course/cs015/demos and javaArg = TASafeHouse.App
     *
     * @param dirPath - the path to the package
     * @param javaArg - the part to come after java (ex. java TASafeHouse.App)
     */
    public static void executeWithClassLoader(String dirPath, String javaArg) {
        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();

        //System.setProperty("java.library.path", Constants.LIBRARY_PATH);
        System.load("/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/i386/libj3dcore-ogl.so");

        // Create the class loader by using the given URL
        // Use prevCl as parent to maintain current visibility
        ClassLoader urlCl = null;

        URL[] locations = null;
        try {
            locations = new URL[]
            {   new URL("file:" + dirPath),
                new URL("jar:file:///course/cs015/lib/cs015.jar!/"),
                new URL("jar:file:///pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dcore.jar!/"),
                new URL("jar:file:///pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dutils.jar!/"),
                new URL("jar:file:///pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/vecmath.jar!/")
            };
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        urlCl = URLClassLoader.newInstance(locations, prevCl);

        try {
            // Save the class loader so that you can restore it later
            Thread.currentThread().setContextClassLoader(urlCl);

            //Invoke main
            Class[] argTypes = new Class[] { String[].class };
            Method main = urlCl.loadClass(javaArg).getDeclaredMethod("main", argTypes);
            main.invoke(null, (Object)null);

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
           Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            // Restore
            Thread.currentThread().setContextClassLoader(prevCl);
        }
    }
    /*
    public static void executeInVisibleTerminal(String dirPath, String javaArg, String termName) {
        //Get the existing classpath, add dirPath to the classpath
        String classPath = dirPath + ":" + getClassPath();

        //Build command to call xterm to run the code
        String javaLoc = "/usr/bin/java";
        String javaCmd = javaLoc + " -classpath " + classPath + " " + javaArg;
        String terminalCmd = "/usr/bin/xterm -title " + "\"" + termName + "\"" + " -e " + "\"" + javaCmd + "; read" + "\"";

        //Execute the command in a seperate thread
        BashConsole.writeThreaded(terminalCmd);
    }
     */



    public static String doubleToString(double value) {
        double roundedVal = Utils.round(value, 2);
        return Double.toString(roundedVal);
    }

    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Returns the current java class path.
     *
     * @return classPath
     */
    public static String getClassPath() {
        //I give up, Netbeans sucks at this, just return the class path we need
        return Constants.CLASSPATH;

        //When not running in Eclipse, only the line of code below is needed
        //return System.getProperty("java.class.path");


        //Hack to make this work properly with Eclipse/Netbeans
        /*
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
        */
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
    public static boolean deleteClassFiles(String dirPath) {
        boolean success = true;

        for (File file : getClassFiles(dirPath)) {
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
        if (dir == null || !dir.exists()) {
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
