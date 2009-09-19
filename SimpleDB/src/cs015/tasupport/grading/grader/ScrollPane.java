
package cs015.tasupport.grading.grader;

import java.awt.Dimension;
import javax.swing.JScrollPane;

class ScrollPane extends JScrollPane
{
	public ScrollPane(MainPanel panel)
	{
		super(panel);
		
		Dimension size = new Dimension(panel.getPreferredSize().width + 30, 800);
		this.setPreferredSize(size);
		this.setSize(size);
			
	}
}
