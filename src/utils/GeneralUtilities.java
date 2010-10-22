package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

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
     * Reads a text file into a String.
     *
     * @param the file to read
     * @return a String of the text in the file
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
            new ErrorView(ex, "Unable to read text of: " + file.getAbsolutePath());
        }
        return text.toString();
    }

    /**
     * Returns the current year
     *
     * @return
     */
    public int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Turns a calendar into a String. Returned in the format
     * YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return date and time formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     */
    public String getCalendarAsString(Calendar entry) {
        if (entry == null) {
            return "";
        }

        return this.getDateAsString(entry) + " " + this.getTimeAsString(entry);
     }

     /**
      * Turns a calendar into a String.  Returned in the format
      * HOUR:MINUTE MONTH-DAY-YEAR
      *
      * @param entry
      * @return date and time formatted as HOUR:MINUTE MONTH-DAY-YEAR
      */
    public String getCalendarAsHandinTime(Calendar entry) {
         if (entry == null) {
             return "";
         }

         return entry.get(Calendar.HOUR_OF_DAY)
                 + ":" + ensureLeadingZero(entry.get(Calendar.MINUTE))
                 + " " + (entry.get(Calendar.MONTH) + 1)
                 + "-" + entry.get(Calendar.DAY_OF_MONTH)
                 + "-" + entry.get(Calendar.YEAR);
     }


    /**
     * Turns a calendar into a String.  Returned in format YEAR-MONTH-DAY
     *
     * @param entry
     * @return date formatted as YEAR-MONTH-DAY
     */
    public String getDateAsString(Calendar entry) {
        if (entry == null) {
            return "";
        }
        return entry.get(Calendar.YEAR)
                + "-" + ensureLeadingZero(entry.get(Calendar.MONTH) + 1)
                + "-" + ensureLeadingZero(entry.get(Calendar.DAY_OF_MONTH));
    }


    /**
     * Turns the time from a Calendar into a String.
     * Returned in format HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return time formatted as HOUR:MINUTE:SECOND
     */
    public String getTimeAsString(Calendar entry) {
        if (entry == null) {
            return "";
        }

        return ensureLeadingZero(entry.get(Calendar.HOUR_OF_DAY))
                + ":" + ensureLeadingZero(entry.get(Calendar.MINUTE))
                + ":" + ensureLeadingZero(entry.get(Calendar.SECOND));
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
     * Gets the appropriate stream depending the file extension of
     * <code>archivePath</code>.
     * <br><br>
     * Supported extensions: zip, tar, tgz, tar.gz. Unsupported extensions will
     * result in an extension being throw.
     *
     * @param archivePath
     * @return
     */
    private ArchiveInputStream getArchiveInputStream(String archivePath) throws IOException, ArchiveException
    {
        //Determine appropriate input stream and compression format
        InputStream is;
        String format;

        String lowerCaseSrcFile = archivePath.toLowerCase();

        if(lowerCaseSrcFile.endsWith(".zip"))
        {
            is = new FileInputStream(archivePath);
            format = "zip";
        }
        else if(lowerCaseSrcFile.endsWith(".tar"))
        {
            is = new FileInputStream(archivePath);
            format = "tar";
        }
        else if(lowerCaseSrcFile.endsWith(".tgz") || archivePath.toLowerCase().endsWith(".tar.gz"))
        {
            is = new GZIPInputStream(new FileInputStream(new File(archivePath)));
            format = "tar";
        }
        else
        {
            throw new IOException("Unsupported file extension. Supported extensions are: zip, tar, tgz, & tar.gz");
        }

        return new ArchiveStreamFactory().createArchiveInputStream(format, is);
    }

    /**
     * Returns a listing of the files and directories in the archive file
     * without extracting the file.
     * <br><br>
     * Supports: zip, tar, tgz/tar.gz
     *
     * @param archivePath
     *
     * @return collection of Strings with the paths of files and directories in the archive
     */
    public Collection<String> getArchiveContents(String archivePath)
    {
        Vector<String> contents = new Vector<String>();

        try
        {
            ArchiveInputStream in = getArchiveInputStream(archivePath);
            while(true)
            {
                ArchiveEntry entry = in.getNextEntry();
                if(entry == null)
                {
                    break;
                }

                contents.add(entry.getName());
            }
            in.close();
        }
        catch(IOException e)
        {
            new ErrorView(e);
        }
        catch (ArchiveException e)
        {
            new ErrorView(e);
        }

        return contents;
    }

    /**
     * Extracts an archive file. Supported extensions: zip, tar, tgz, tar.gz
     *
     * @param archivePath the absolute path of the archive file
     * @param dstDir the directory the archive file will be expanded into
     *
     * @boolean success of extracing archive
     */
    public boolean extractArchive(String archivePath, String dstDir)
    {
        try
        {
            ArchiveInputStream in = getArchiveInputStream(archivePath);
            while(true)
            {
                ArchiveEntry entry = in.getNextEntry();
                if(entry == null)
                {
                    break;
                }

                File file = new File(dstDir, entry.getName());
                if(entry.isDirectory())
                {
                    file.mkdirs();
                }
                else
                {
                    OutputStream out = new FileOutputStream(file);
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
            in.close();
        }
        catch(IOException e)
        {
            new ErrorView(e);

            return false;
        }
        catch (ArchiveException e)
        {
            new ErrorView(e);

            return false;
        }

        return true;
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
     * TODO: Make this more efficient! Write the rounding code so that it
     * doesn't need to create a BigDecimal. This code gets called a lot.
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

    /*
     * Checks to see if any of col2 is in col1. If one or more are in both then it will return true.
     *
     * @param col1 collection to test membership in
     * @param col2 collection to test who's elements we are testing
     * @return true if there is overlap
     */
    public <T> boolean containsAny(Collection<T> col1, Collection<T> col2) {
        for (T item : col2) {
            if (col1.contains(item)) {
                return true;
            }
        }
        return false;
    }

}