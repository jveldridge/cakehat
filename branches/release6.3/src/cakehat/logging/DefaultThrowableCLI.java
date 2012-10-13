package cakehat.logging;

import cakehat.Allocator;
import cakehat.email.EmailManager;
import java.util.Scanner;

/**
 *
 * @author jak2
 */
class DefaultThrowableCLI
{
    private DefaultThrowableCLI() { }
    
    /**
     * Displays the error via system in and allows the user to interact via system out. This method is synchronized to
     * ensure that only one error is displayed to the user at the same time.
     * 
     * @param message
     * @param error 
     */
    synchronized static void display(String message, Throwable error)
    {
        System.err.println("cakehat has encountered an error");
         
        //Print the message, or if one does not exist - print the error's message
        if(message != null)
        {
            System.err.println(message);
        }
        else if(error != null && error.getMessage() != null)
        {
            System.err.println(error.getMessage());
        }

        //Print the error
        if(error != null)
        {
            error.printStackTrace(System.err);
        }


        //Ask the user if they want to report the exception
        if(Allocator.getEmailManager().getEmailAccountStatus() == EmailManager.EmailAccountStatus.AVAILABLE)
        {
            System.out.println("\nWould you like to report this error (y/n)?");

            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();

            if(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes"))
            {
                System.out.println("Comments:");
                String comments = scanner.nextLine();

                System.out.println("Sending...");
                DefaultThrowableReporter.emailErrorReport(message, error, comments);
                System.out.println("Error report sent");
            }

            scanner.close();
        }
    }
    
    public static void main(String[] args)
    {
        try
        {
            throw new RuntimeException("Mo' code, mo' problems");
        }
        catch(Exception e)
        {
            DefaultThrowableCLI.display("A message that is quite long because it is going to require wrapping due to " +
                    "its long length and that is just a good test of what is occurring.", e);
        }
    }
}