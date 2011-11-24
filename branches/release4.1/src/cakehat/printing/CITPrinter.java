package cakehat.printing;

/**
 *
 * @author jak2
 */
public enum CITPrinter implements PhysicalPrinter
{
    bw1("bw1"), bw2("bw2"), bw3("bw3"), bw4("bw4"), bw5("bw5");

    private final String _printerName;

    private CITPrinter(String printerName)
    {
        _printerName = printerName;
    }

    @Override
    public String getPrinterName()
    {
        return _printerName;
    }
}