package cs015.tasupport.grading.grader;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

import cs015.tasupport.grading.rubric.Entry;
import cs015.tasupport.grading.rubric.Section;

class TextField extends JTextArea
{	
	private StateManager _manager;
	
	private TextField(Vector<Entry> text)
	{
		//Display stored text
		String storedText = "";
		for(Entry entry : text)
		{
			storedText += entry.Text;
			if(entry != text.lastElement())
			{
				storedText += "\n";
			}
		}
		this.setText(storedText);
	}
	
	public static TextField getAsCommentField(final Section section, StateManager manager)
	{
		final TextField field = new TextField(section.Comments);
		field._manager = manager;
		
		//Set appearance
		field.setRows(4);
		field.setLineWrap(true);
		field.setWrapStyleWord(true);
		field.setBorder(BorderFactory.createEtchedBorder());
		
		//Add listener to update rubric with data as it is typed
		field.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent e)
			{
				field._manager.rubricChanged();
				//Split on each new line
				String[] lines = field.getText().split("\n");
				
				Vector<Entry> comments = new Vector<Entry>();
				for(String line : lines)
				{
					Entry entry = new Entry();
					entry.Text = line;
					comments.add(entry);
				}
				section.Comments = comments;
			}
		});
		
		return field;
	}
	
	public static TextField getAsNotesField(Section section)
	{
		TextField field = new TextField(section.Notes);
		
		//Set appearance and properties
		field.setLineWrap(true);
		field.setWrapStyleWord(true);
		field.setEditable(false);		
		
		return field;
	}
}
