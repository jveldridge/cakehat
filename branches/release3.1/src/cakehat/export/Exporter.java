package cakehat.export;

/**
 * Exports information from the grading system.
 *
 * @author jak2
 */
public interface Exporter
{
    public void export() throws ExportException;
    public void export(java.io.File file) throws ExportException;
    public void cancelExport();
}