package gradesystem.printing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import gradesystem.Allocator;

/**
 * Uses enscript to print the PrintRequest in landscape with a small font, and
 * two columns per page.
 *
 * @author jak2
 */
public class EnscriptPrinter extends Printer {
    //So that it prints currentPage/totalNumberOfPages - e.g. 2/4 for page 2 of 4
    private static final String PAGE_NUMBER_FORMATTING = "$%/$=";

    public void print(Iterable<PrintRequest> requests, String printer) throws IOException {
        //Set to '--no-job-header' after first request to prevent cover sheet printing after the first handin
        String dontPrintCoverSheet = "";

        String fullCommand = "";

        for(PrintRequest request : requests) {
            //Convert request to one text file
            File tmpFile = convertRequest(request);

            //Build command
            String cmd = String.format("enscript %s --header='student: %s |ta: %s |%s' --header-font=Courier8 -q -P%s -2 -r --ps-level=1 %s; ",
                    dontPrintCoverSheet, request.getStudentLogin(), request.getTALogin(), PAGE_NUMBER_FORMATTING, printer, tmpFile.getAbsolutePath());

            //Execute command
            fullCommand = fullCommand + cmd;

            dontPrintCoverSheet = "--no-job-header ";  //prevent future jobs in the batch from having a cover sheet
        }

        File workspace = Allocator.getPathServices().getUserWorkspaceDir();
        Allocator.getExternalProcessesUtilities().executeAsynchronously(fullCommand, workspace);
    }

    /**
     * Converts a request into a text file that combines all files of the request.
     *
     * @param request
     * @return the file created
     */
    private File convertRequest(PrintRequest request) throws IOException
    {
        //Create temp file that combines the entire request into one file
        File tmpFile = File.createTempFile("request", null, Allocator.getPathServices().getUserWorkspaceDir());

        //Confirm the temporary file was created
        if(tmpFile == null)
        {
            throw new IOException("Cannot print files: unable to create " +
                    "temporary file used for printing.");
        }

        //Get entire request as a string
        String text = this.getFilesAsCombinedString(request);

        //Write combined text to file
        Writer output = new BufferedWriter(new FileWriter(tmpFile));
        output.write(text);
        output.close();

        return tmpFile;
    }

    /**
     * Combines the entire PrintRequest into one String with each file having
     * a header before it.
     *
     * @param request
     * @return
     */
    private String getFilesAsCombinedString(PrintRequest request) throws IOException
    {
        String text = "";

        for(File file : request.getFiles())
        {
            text += getHeader(file, request.getStudentLogin());
            text += "\n";
            text += "\n";

            text += Allocator.getFileSystemUtilities().readFile(file);
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
}