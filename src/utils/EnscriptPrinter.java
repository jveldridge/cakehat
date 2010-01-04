package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author jak2
 */
public class EnscriptPrinter extends Printer
{
    //So that it prints currentPage/totalNumberOfPages - e.g. 2/4 for page 2 of 4
    private static final String PAGE_NUMBER_FORMATTING = "$%/$=";

    public void print(Iterable<PrintRequest> requests, String printer)
    {
        //TODO: Figure out how to supress cover sheet for all requests beyond the first
        for(PrintRequest request : requests)
        {
            //Convert request to one text file
            File tmpFile = convertRequest(request);

            //Build command
            String cmd = "enscript --header=" + request.getStudentLogin() + "|" +
                         request.getTALogin() + "|" + PAGE_NUMBER_FORMATTING +
		         " --header-font=Courier5 -q -P" + printer + " -2 -r --ps-level=1 " +
                         tmpFile.getAbsolutePath();


            System.out.println("enscript Command:");
            System.out.println(cmd);

            //Execute command
            BashConsole.writeThreaded(cmd);
        }
    }

    /**
     *
     * @param request
     * @return the temporary file created
     */
    private File convertRequest(PrintRequest request)
    {
        //Create temp file that combines the entire request into one file
        String tmpFilePath =
                Allocator.getGeneralUtilities().getUserGradingDirectory() +
                ".print_temp_"+System.currentTimeMillis()+".tmp";
        File tmpFile = new File(tmpFilePath);

        //Check if we screwed up by using a file name that already exists
        if(tmpFile.exists())
        {
            System.err.println("Error in printing, temp name conflict: " + tmpFilePath);
            return null;
        }

        //Get entire request as a string
        String text = this.getFilesAsCombinedString(request);

        //Write combined text to file
        try
        {
            tmpFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            Writer output = new BufferedWriter(new FileWriter(tmpFile));
            output.write(text);
            output.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return tmpFile;
    }

    private String getFilesAsCombinedString(PrintRequest request)
    {
        String text = "";

        for(File file : request.getFiles())
        {
            text += getHeader(file, request.getStudentLogin());
            text += "\n";
            text += "\n";

            text += fileToString(file);
            text += "\n";
            text += "\n";
        }

        return text;
    }

    private String getHeader(File file, String studentLogin)
    {
        String filePath = file.getAbsolutePath();
        int index = filePath.indexOf(studentLogin);
        filePath = filePath.substring(index + studentLogin.length());

        String header = "";
        header += "/------------------------------------------------------ \n";
        header += "| FILE: " + filePath + "\n";
        header += "\\------------------------------------------------------";

        return header;
    }

    /**
     * Reads a text file into a string.
     *
     * @param filePath
     * @return
     */
    private String fileToString(File file)
    {
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            // repeat until all lines are read
            while ((text = reader.readLine()) != null)
            {
                contents.append(text).append(System.getProperty("line.separator"));
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return contents.toString();
    }

}