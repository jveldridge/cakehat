package gradesystem.rubric;

import javax.swing.JButton;

/**
 * Keeps track of the state of a rubric to determine if it has been edited.
 * Toggles the save button depending on whether there is anything to save.
 *
 * @author spoletto
 */
class StateManager
{	
    private boolean _saved;
    private JButton _saveButton;
	
    public StateManager(JButton saveButton, boolean saved)
    {
        _saved = saved;
        _saveButton = saveButton;
    }
	
    public void rubricSaved()
    {
        _saved = true;
        _saveButton.setEnabled(false);
    }
	
    public void rubricChanged()
    {
        _saved = false;
        _saveButton.setEnabled(true);
    }
	
    public boolean beenSaved()
    {
        return _saved;
    }
}