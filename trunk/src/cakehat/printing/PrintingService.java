package cakehat.printing;

import java.io.IOException;
import java.util.Arrays;

/**
 * Prints requests to a specified physical printer.
 *
 * @author jak2
 */
public abstract class PrintingService
{
    /**
     * Prints the request to the specified printer.
     *
     * @param request
     * @param printer
     */
    public void print(PrintRequest request, PhysicalPrinter printer) throws IOException
    {
        this.print(Arrays.asList(new PrintRequest[]{ request }), printer);
    }

    /**
     * Prints the requests to the specified printer.
     *
     * @param requests
     * @param printer
     */
    public abstract void print(Iterable<PrintRequest> requests, PhysicalPrinter printer) throws IOException;
}