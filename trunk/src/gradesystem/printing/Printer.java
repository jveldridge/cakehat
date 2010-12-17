package gradesystem.printing;

import java.util.Arrays;

/**
 * Prints requests to a specified printer.
 *
 * @author jak2
 */
public abstract class Printer
{
    /**
     * Prints the request to the specified printer.
     *
     * @param request
     * @param printer a CIT printer (e.g. bw3)
     */
    public void print(PrintRequest request, String printer)
    {
        this.print(Arrays.asList(new PrintRequest[]{ request }), printer);
    }

    /**
     * Prints the requests to the specified printer.
     *
     * @param requests
     * @param printer a CIT printer (e.g. bw3)
     */
    public abstract void print(Iterable<PrintRequest> requests, String printer);
}