package utils.printing;

import gradesystem.GradeSystemApp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import utils.Allocator;
import utils.BashConsole;

/**
 * Uses enscript to print the PrintRequest in landscape with a small font, and
 * two columns per page.
 *
 * @author jak2
 */
public class EnscriptPrinter extends Printer {
    //So that it prints currentPage/totalNumberOfPages - e.g. 2/4 for page 2 of 4
    private static final String PAGE_NUMBER_FORMATTING = "$%/$=";

    public void print(Iterable<PrintRequest> requests, String printer) {
        //Set to '--no-job-header' after first request to prevent cover sheet printing after the first handin
        String dontPrintCoverSheet = "";

        String fullCommand = "";

        for(PrintRequest request : requests) {
            //Convert request to one text file
            File tmpFile = convertRequest(request);

            //Build command
            String cmd = String.format("enscript %s --header='student: %s |ta: %s |%s' --header-font=Courier8 -q -P%s -2 -r --ps-level=1 %s; ",
                    dontPrintCoverSheet, request.getStudentLogin(), request.getTALogin(), PAGE_NUMBER_FORMATTING, printer, tmpFile.getAbsolutePath());

            if (GradeSystemApp.inTestMode()) {
                System.out.println("enscript Command:");
                System.out.println(cmd);
            }

            //Execute command
            fullCommand = fullCommand + cmd;

            dontPrintCoverSheet = "--no-job-header ";  //prevent future jobs in the batch from having a cover sheet
        }

        BashConsole.writeThreaded(fullCommand);
    }

    /**
     * Converts a request into a text file that combines all files of the request.
     *
     * @param request
     * @return the file created
     */
    private File convertRequest(PrintRequest request)
    {
        //Create temp file that combines the entire request into one file
        String tmpFilePath =
                Allocator.getGradingUtilities().getUserGradingDirectory() +
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

    /**
     * Combines the entire PrintRequest into one String with each file having
     * a header before it.
     *
     * @param request
     * @return
     */
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

    /**
     * Creates a header for a file. Based on the filepath of file, but removes
     * everything up to the student login directory that their code exists in.
     *
     * @param file
     * @param studentLogin
     * @return
     */
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