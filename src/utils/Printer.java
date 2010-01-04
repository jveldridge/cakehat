package utils;

import java.util.Arrays;

/**
 *
 * @author jak2
 */
public abstract class Printer
{
    public void print(PrintRequest request, String printer)
    {
        this.print(Arrays.asList(new PrintRequest[]{ request }), printer);
    }
    
    public abstract void print(Iterable<PrintRequest> requests, String printer);
}