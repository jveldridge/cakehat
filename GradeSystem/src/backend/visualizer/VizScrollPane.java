
package backend.visualizer;

import java.awt.Dimension;
import javax.swing.JScrollPane;

class VizScrollPane extends JScrollPane
{
	public VizScrollPane(VisualPanel panel)
	{
		super(panel);
		
		Dimension size = new Dimension(panel.getPreferredSize().width + 30, 800);
		this.setPreferredSize(size);
		this.setSize(size);
			
	}
}
