package cs015.tasupport.grading.grader;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import cs015.tasupport.grading.Constants;
import cs015.tasupport.grading.rubric.Rubric;
import cs015.tasupport.grading.rubric.RubricManager;

public class Grader extends JFrame
{	
	public Grader(String asgn, String graderAcct, String studentAcct)
	{
		super("Grading " + studentAcct + "'s " + asgn);
		
		//Get grading rubric
		final String XMLFilePath = Constants.GRADER_PATH + graderAcct + "/" + asgn + "/" + studentAcct + ".xml";
		final Rubric rubric = RubricManager.processXML(XMLFilePath);
		
		//Configure basic properties
		this.setLayout(new BorderLayout());
		JButton saveButton = new JButton("Save Changes");
		final StateManager stateManager = new StateManager(saveButton, true);
		saveButton.setEnabled(false);
		MainPanel mp = new MainPanel(rubric, stateManager);
		ScrollPane mainPane = new ScrollPane(mp);
		mainPane.getVerticalScrollBar().setUnitIncrement(16);
		this.add(mainPane, BorderLayout.CENTER);
		saveButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				RubricManager.writeToXML(rubric, XMLFilePath);
				stateManager.rubricSaved();
			}
		});
		JPanel savePanel = new JPanel();
		savePanel.add(saveButton);
		this.add(savePanel, BorderLayout.SOUTH);
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//Open up a dialog on window close to save rubric data
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if(stateManager.beenSaved())
				{
					Grader.this.dispose();
				}
				else 
				{
					int chosenOption = JOptionPane.showConfirmDialog(Grader.this, "Would you like to save before exiting?");
					if(chosenOption == JOptionPane.YES_OPTION)
					{
						RubricManager.writeToXML(rubric, XMLFilePath);
						Grader.this.dispose();
					}
					if(chosenOption == JOptionPane.NO_OPTION)
					{
						Grader.this.dispose();
					}
					if(chosenOption == JOptionPane.CANCEL_OPTION)
					{
						
					}	
				}
			}
		});
		
		Vector<Component> tabOrder = mp.getTabOrder();
		//tabOrder.add(saveButton);
		this.setFocusTraversalPolicy(new MyOwnFocusTraversalPolicy(tabOrder, mainPane.getVerticalScrollBar()));
		
		//add enter as forward tab key
		Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
		Set<AWTKeyStroke> newForwardKeys = new HashSet(forwardKeys);
		newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);
		
		
	}
}
