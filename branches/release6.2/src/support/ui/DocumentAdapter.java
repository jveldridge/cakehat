package support.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author jak2
 */
public abstract class DocumentAdapter implements DocumentListener
{
    @Override
    public void insertUpdate(DocumentEvent de)
    {
        this.modificationOccurred(de);
    }

    @Override
    public void removeUpdate(DocumentEvent de)
    {
        this.modificationOccurred(de);
    }

    @Override
    public void changedUpdate(DocumentEvent de)
    {
        this.modificationOccurred(de);
    }
    
    public abstract void modificationOccurred(DocumentEvent de);
}