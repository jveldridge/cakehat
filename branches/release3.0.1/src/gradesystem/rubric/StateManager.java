package gradesystem.rubric;

import gradesystem.database.HandinStatus;
import javax.swing.JButton;

/**
 * Keeps track of the state of a rubric to determine if it has been edited.
 * Toggles the save button depending on whether there is anything to save.
 *
 * @author spoletto
 */
class StateManager {
    
    private boolean _rubricSaved = true;
    private boolean _statusSaved = true;
    private HandinStatus _handinStatus;
    private JButton _saveButton;
	
    public StateManager(JButton saveButton) {
        _saveButton = saveButton;
    }

    public void rubricChanged() {
        _rubricSaved = false;
        _saveButton.setEnabled(true);
    }

    public void rubricSaved() {
        _rubricSaved = true;
        _saveButton.setEnabled(!_statusSaved);
    }

    public boolean isRubricSaved() {
        return _rubricSaved;
    }

    public boolean isStatusSaved() {
        return _statusSaved;
    }

    public void setHandinStatus(HandinStatus status) {
        _handinStatus = status;
        _statusSaved = false;
        _saveButton.setEnabled(true);
    }

    public void statusSaved() {
        _statusSaved = true;
        _saveButton.setEnabled(!_rubricSaved);
    }

    public HandinStatus getHandinStatus() {
        return _handinStatus;
    }

    public boolean beenSaved() {
        return _rubricSaved && _statusSaved;
    }
    
}