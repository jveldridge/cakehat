package support.ui;

/**
 * A listener for changes in selection.
 * 
 * @author jak2
 */
public interface SelectionListener<E>
{
    public static final class SelectionAction
    {
        private boolean _cancel = false;
        
        public void cancel()
        {
            _cancel = true;
        }
        
        public boolean isCancelled()
        {
            return _cancel;
        }
    }
    
    public void selectionPerformed(E currValue, E newValue, SelectionAction action);
}