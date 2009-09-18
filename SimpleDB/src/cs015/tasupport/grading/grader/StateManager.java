package cs015.tasupport.grading.grader;

import javax.swing.JButton;

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
