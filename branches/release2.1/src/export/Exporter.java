package export;

/**
 * Exports information from the grading system.
 *
 * @author jak2
 */
public interface Exporter
{
    public void export();
    public void export(java.io.File file);
    public void cancelExport();
}