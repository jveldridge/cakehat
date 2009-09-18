package grader;

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

public class ScrollPane extends JScrollPane
{
	public ScrollPane(MainPanel panel)
	{
		super(panel);
		
		Dimension size = new Dimension(panel.getPreferredSize().width + 30, 800);
		this.setPreferredSize(size);
		this.setSize(size);
			
	}

}
