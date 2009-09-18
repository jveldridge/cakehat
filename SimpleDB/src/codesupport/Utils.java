package codesupport;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Utils {

    /**
     * Sends an email.  Returns true if success, false if no success.
     * @param senderEmail
     * @param recipientEmail
     * @param subject
     * @param body
     * @return
     */
    public static boolean sendMail(String[] to, String[] cc, String[] bcc, String subject, String body, String[] attachmentNames) {

        try {

            String stringBuilder = "mutt -s \"" + subject + "\"";

            if (cc.length > 2) {
                System.out.println(Arrays.toString(cc));
                stringBuilder += " -c " + Arrays.toString(cc).replace(",", "").replace("[", "").replace("]", "");
            }
            if (bcc.length > 2) {
                stringBuilder += " -b " + Arrays.toString(bcc).replace(",", "").replace("[", "").replace("]", "");
            }
            if (attachmentNames.length > 2) {
                stringBuilder += " -a " + Arrays.toString(attachmentNames).replace(",", "").replace("[", "").replace("]", "");
            }
            stringBuilder += " -- " + Arrays.toString(to).replace(",", " ").replace("[", "").replace("]", "") + " <<< \"" + body + "\"";
            String[] cmd = {"/bin/sh", "-c", stringBuilder};
            Runtime.getRuntime().exec(cmd);
             System.out.println(cmd[2]);
        //"uuencode histogram_1.png | mailx -s \"test\" \"psastras\""};

        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return false;
    //        String host = "localhost";//"mail-relay.brown.edu";
//
//        Properties props = new Properties();
////        System.setProperty("javax.net.ssl.trustStore", "browncs-ca.crt");
//        props.setProperty("mail.transport.protocol", "smtp");
////        props.setProperty("mail.smtp.starttls.enable","true");
////
//        props.setProperty("mail.smtp.host", host);
//////        props.setProperty("mail.smtp.port", "465");
////        String[] test = {"smtps.cs.brown.edu:465"};
////        try{InstallCert.main(test);}
////        catch(Exception e){e.printStackTrace();}
////        System.out.println("ASD");
//
//        Session session = Session.getDefaultInstance(props);
//        session.setDebug(true);
//        try {
//            MimeMessage msg = new MimeMessage(session);
//            msg.setFrom(new InternetAddress(senderEmail));
//            InternetAddress[] address = new InternetAddress[recipientEmail.length];
//            for (int i = 0; i < recipientEmail.length; i++) {
//                address[i] = new InternetAddress(recipientEmail[i]);
//            }
//            msg.setRecipients(Message.RecipientType.TO, address);
//            msg.setSubject(subject);
//            msg.setSentDate(new Date());
//
//            Multipart mp = new MimeMultipart();
//
//            BodyPart htmlPart = new MimeBodyPart();
//            htmlPart.setContent(body, "text/html");
//            mp.addBodyPart(htmlPart);
//            for(int i = 0; i< images.length; i++) {
//                BodyPart imagePart = new MimeBodyPart();
//                FileDataSource ds = new FileDataSource(images[i]);
//                imagePart.setDataHandler(new DataHandler(ds));
//                imagePart.setFileName(images[i].getName());
//                imagePart.setHeader("Content-ID", "<" + imageContentIDs[i] + ">");
//                mp.addBodyPart(imagePart);
//            }
//            msg.setContent(mp);
//            Transport t = session.getTransport();
//            t.connect();
//            t.sendMessage(msg, address);
//            t.close();
//            return true;
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return false;
//        }
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
        String classPath = getClassPath() + ":" + dirPath;
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
     * Returns all files in a directory, recursing into subdirectories, that
     * contain files with the specified extension.
     *
     * @param dirPath starting directory
     * @param extension the file extension, e.g. java or class
     * @return the files found with the specified extension
     */
    public static Collection<File> getFiles(String dirPath, String extension) {
        Vector<File> files = new Vector<File>();

        if (dirPath == null) {
            System.out.println("aaack!");
        }

        File dir = new File(dirPath);

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
