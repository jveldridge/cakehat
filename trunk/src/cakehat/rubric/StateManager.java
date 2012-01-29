package cakehat.rubric;

import cakehat.config.TA;
import cakehat.database.HandinStatus;
import javax.swing.JButton;

/**
 * Keeps track of the state of a rubric to determine if it has been edited.
 * Toggles the save button depending on whether there is anything to save.
 *
 * @author spoletto
 */
@Deprecated
class StateManager {
    
    private boolean _rubricSaved = true;
    private boolean _statusSaved = true;
    private boolean _graderSaved = true;
    private HandinStatus _handinStatus;
    private TA _grader;
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
        _saveButton.setEnabled(!_statusSaved || !_graderSaved);
    }

    public boolean isRubricSaved() {
        return _rubricSaved;
    }

    public void setHandinStatus(HandinStatus status) {
        _handinStatus = status;
        _statusSaved = false;
        _saveButton.setEnabled(true);
    }

    public HandinStatus getHandinStatus() {
        return _handinStatus;
    }

    public void statusSaved() {
        _statusSaved = true;
        _saveButton.setEnabled(!_rubricSaved || !_graderSaved);
    }

    public boolean isStatusSaved() {
        return _statusSaved;
    }

    public void setGrader(TA grader) {
        _grader = grader;
        _graderSaved = false;
        _saveButton.setEnabled(true);
    }

    public TA getGrader() {
        return _grader;
    }

    public void graderSaved() {
        _graderSaved = true;
        _saveButton.setEnabled(!_rubricSaved || !_statusSaved);
    }

    public boolean isGraderSaved() {
        return _graderSaved;
    }

    public boolean beenSaved() {
        return _rubricSaved && _statusSaved && _graderSaved;
    }
}