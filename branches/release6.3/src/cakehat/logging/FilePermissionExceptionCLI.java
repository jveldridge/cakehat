package cakehat.logging;

import cakehat.Allocator;
import cakehat.email.EmailManager;
import java.io.File;
import java.util.Scanner;
import support.utils.FilePermissionException;

/**
 *
 * @author jak2
 */
class FilePermissionExceptionCLI
{
    synchronized static void display(FilePermissionException ex)
    {
        System.err.println("Your course has specified files with incorrect permissions");
        for(File file : ex.getFiles())
        {
            System.err.println("\t" + file.getAbsolutePath());
        }

        //Ask the user if they want to report the issue
        if(Allocator.getEmailManager().getEmailAccountStatus() == EmailManager.EmailAccountStatus.AVAILABLE)
        {
            System.out.println("\nWould you like to report this to your course's HTAs (y/n)?");

            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();

            if(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes"))
            {
                System.out.println("Sending...");
                FilePermissionExceptionReporter.sendEmail(ex.getFiles());
                System.out.println("Error report sent");
            }

            scanner.close();
        }
    }
}