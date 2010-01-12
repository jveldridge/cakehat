package utils;

import com.ice.tar.TarArchive;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.*;

/**
 * Utilities that are useful for any course.
 */
public class GeneralUtilities {

    /**
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourceFile
     * @param destFile
     * @return success of copying file
     */
    public boolean copyFile(File sourceFile, File destFile) {
        if(!sourceFile.exists()){
            return false;
        }
        try {
             if(!destFile.exists()) {
                destFile.createNewFile();
             }

             FileChannel source = null;
             FileChannel destination = null;
             try {
                destination = new FileOutputStream(destFile).getChannel();
                source = new FileInputStream(sourceFile).getChannel();
                destination.transferFrom(source, 0, source.size());
             }
             finally {
                 if(source != null) {
                    source.close();
                 }
                 if(destination != null) {
                    destination.close();
                 }
            }
        }
        catch(IOException e){
            return false;
        }

        return true;
    }

    /**
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourcePath
     * @param destPath
     * @return success of copying file
     */
    public boolean copyFile(String sourcePath, String destPath) {
         return copyFile(new File(sourcePath), new File(destPath));
    }

    /**
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourceFile
     * @param destPath
     * @return success of copying file
     */
    public boolean copyFile(File sourceFile, String destPath) {
        return copyFile(sourceFile, new File(destPath));
    }

    /**
     * @deprecated use config.EmailAccount's sendMail methods.
     *
     * Sends email through the cs department smtps server
     * Attaches a list of files to the email
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
    public void sendMail(String from, String[] to, String[] cc, String[] bcc, String subject, String body, String[] attachmentNames) {
        System.setProperty("javax.net.ssl.trustStore", Allocator.getConstants().getEmailCertPath());
        System.setProperty("javax.net.ssl.trustStorePassword", Allocator.getConstants().getEmailCertPassword());
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", Allocator.getConstants().getEmailHost());
        props.put("mail.smtps.user", Allocator.getConstants().getEmailAccount());
        props.put("mail.smtp.host", Allocator.getConstants().getEmailHost());
        props.put("mail.smtp.port", Allocator.getConstants().getEmailPort());
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.socketFactory.port", Allocator.getConstants().getEmailPort());
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        try {
            Authenticator auth = new javax.mail.Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Allocator.getConstants().getEmailAccount(),
                                                      Allocator.getConstants().getEmailPassword());
                }
            };
            Session session = Session.getInstance(props, auth);
            //session.setDebug(true);

            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(from));

            //TODO: Fix this to something less hacky
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

    /**
     * Reads a text file into a String.
     *
     * @param the file to read
     * @return a String of the text in teh file
     */
    public String readFile(File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
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
    public String getUserLogin() {
        return System.getProperty("user.name");
    }

    /**
     * TODO: See if there is a command that just returns user's name instead of
     *       having to parse snoop's output.
     *
     * Gets a user's name.
     *
     * @param login the user's login
     * @return user name
     */
    public String getUserName(String login) {
        Collection<String> output = BashConsole.write("snoop " + login);

        for (String line : output) {
            if (line.startsWith("Name")) {
                String name = line.substring(line.indexOf(":") + 2, line.length());
                return name;
            }
        }
        return "UNKNOWN_LOGIN";
    }

    /**
     * Returns the logins of all students in the class's student group.
     * Removes the test account login.
     *
     * @return
     */
    public Iterable<String> getStudentLogins() {
        //Get list of members to the student group
        List<String> list = Arrays.asList(getMembers(Allocator.getConstants().getStudentGroup()));
        //Remove test account from list
        list.remove(Allocator.getConstants().getTestAccount());
        
        return list;
    }

    /**
     * Returns all members of a given group
     *
     * @param group
     * @return array of all of the logins of a given group
     */
    private String[] getMembers(String group) {
        Collection<String> output = BashConsole.write("members " + group);

        String result = output.iterator().next();
        String[] logins = result.split(" ");

        return logins;
    }

    /**
     * Returns the current year
     *
     * @return
     */
    public int getCurrentYear() {
        return 2009; //For testing purposes only
        //return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Turns a calendar into a String. Returned in the format as
     * YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return date and time formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     */
    public String getCalendarAsString(Calendar entry) {
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
    private String ensureLeadingZero(int number) {
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
    public Calendar getCalendarFromString(String timestamp) {
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
    public Calendar getCalendar(String year, String month, String day, String time) {
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
        }
        catch (Exception e) { }

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
    public Calendar getModifiedDate(String filePath) {
        return getModifiedDate(new File(filePath));
    }

    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file.
     *
     * @param file
     * @return last modified date
     */
    public Calendar getModifiedDate(File file) {
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
    public boolean isBeforeDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency) {
        deadline = ((Calendar) deadline.clone());
        deadline.add(Calendar.MINUTE, minutesOfLeniency);

        return toCheck.before(deadline);
    }

    /**
     * Number of days, given a certain amount of leniency, that is after
     * the deadline.
     *
     * @param toCheck the calendar to check how many days after the deadline
     * @param deadline the deadline
     * @param minutesOfLeniency the amount of leniency in minutes to be granted after the deadline
     * @return number of days
     */
    public int daysAfterDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency) {
        deadline = ((Calendar) deadline.clone());
        deadline.add(Calendar.MINUTE, minutesOfLeniency);

        //If to check is before the deadline
        if(toCheck.before(deadline))
        {
            return 0;
        }

        int daysLate = 0;

        // Look ahead 1000 days, to prevent infinite looping if really far apart days are passed in
        for(int i = 0; i < 1000; i++)
        {
            if(toCheck.after(deadline))
            {
                daysLate++;
                deadline.add(Calendar.HOUR, 24);
            }
            else
            {
                break;
            }
        }

        return daysLate;
    }

    /**
     * Extracts a tar file.
     *
     * @param tarPath the absolute path of the tar file
     * @param destPath the directory the tar file will be expanded into
     *
     * @boolean success of untarring file
     */
    public boolean untar(String tarPath, String destPath) {
        try {
            TarArchive tar = new TarArchive(new FileInputStream(new File(tarPath)));
            tar.extractContents(new File(destPath));

            return true;
        }
        catch (Exception ex) {
            new ErrorView(ex);
            return false;
        }
        /*
          //Original code
          String cmd = "tar -xf " + tarPath + " -C " + destPath;
          try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
          }
          catch (Exception e) {
            new ErrorView(e);
          }
             */
    }

    /**
     * Makes a directory using the makeDirectoryHelper(...) method,
     * then changes the permissions to 770
     *
     * TODO: Check if changing the directory permission is working properly.
     *       In particular, check what it is considering the group (ugrad or TA group)
     *
     * @param dirPath- directory to be created
     * @return whether the directory creation was successful
     */
    public boolean makeDirectory(String dirPath) {
        //Make directory if necessary
        boolean madeDir = false;

        File dir = new File(dirPath);
        if(!dir.exists()){
            madeDir = dir.mkdirs();
        }

        //If directory was made, change permissions to be totally accessible by user and group
        if(madeDir){
            BashConsole.write("chmod 770 -R" + dirPath);
        }

        //Return if the directory now exists
        return dir.exists();
    }
    /**
     *
     * Removes a directory and all of its files and subdirectories.
     *
     * @author jak2
     * @date 1/8/2010
     *
     * @param dirPath
     * @return success of deletion
     */
    public boolean removeDirectory(String dirPath) {
        return removeDirectory(new File(dirPath));
    }

    /**
     *
     * Removes a directory and all of its files and subdirectories.
     *
     * @author jak2
     * @date 1/8/2010
     *
     * @param dirPath
     * @return success of deletion
     */
    public boolean removeDirectory(File dirPath) {
        //only proceed if directory exists
        if(dirPath.exists() && dirPath.isDirectory()) {
            //for each directory and file in directory
            for(File file : dirPath.listFiles()) {
                //if directory, recursively delete files and directories inside
                if(file.isDirectory()) {
                    removeDirectory(file);
                }
                //if file, just delete the file
                else {
                    file.delete();
                }
            }

            //return success of deleting directory
            return dirPath.delete();
        }

        //if the directory didn't exist, report failure
        return false;
    }


    /**
     * Takes a double and returns it as a String rounded to 2
     * decimal places.
     *
     * @param value
     * @return the double as a String rounded to 2 decimal places
     */
    public String doubleToString(double value) {
        double roundedVal = round(value, 2);
        return Double.toString(roundedVal);
    }

    /**
     * Rounds a double to the number of decimal places specified.
     *
     * @param d the double to round
     * @param decimalPlace the number of decimal places to round to
     * @return the rounded double
     */
    public double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Returns all files in a directory, recursing into subdirectories, that
     * contain files with the specified extension.
     *
     * @param dirPath starting directory
     * @param extension the file extension, e.g. java or class
     * @return the files found with the specified extension
     */
    public Collection<File> getFiles(String dirPath, String extension) {
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
            //Add if this entry is a file ending with the extension and not a hidden file
            if (entry.isFile() && name.endsWith("." + extension) && !name.startsWith(".")) {
                files.add(entry);
            }
        }

        return files;
    }
}