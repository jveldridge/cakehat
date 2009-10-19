
package frontend.grader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import frontend.grader.rubric.*;
import utils.Utils;

class MainPanel extends JPanel
{
	private Rubric _rubric;
	private SpringLayout _mainLayout;
	private JPanel _abovePanel = null;
	private int _height = 0;
	private java.util.Vector<NumberField> _totals = new java.util.Vector<NumberField>();
	private java.util.Vector<Section> _sections = new java.util.Vector<Section>();
	private NumberField _totalScoreField;
	private Vector<Component> _tabOrder = new Vector<Component>(7);
	private StateManager _stateManager;
	
	public MainPanel(Rubric rubric, StateManager manager)
	{
		_rubric = rubric;
		_stateManager = manager;
		this.setBackground(Color.white);
		
		//Set width
		//height will be set after everything else is populated
		Dimension size = new Dimension(600,0);
		this.setSize(size);
		this.setPreferredSize(size);
		
		//Create the layout
		_mainLayout = new SpringLayout();
		this.setLayout(_mainLayout);
		
		//Display the elements of the rubric
		displayAssignment();
		displayStudentAndGrader();
		for(Section section : _rubric.Sections)
		{
			this.displaySection(section);
		}
		
		this.displayStatus(_rubric);
		
		this.displayExtraCredit(_rubric);
		
		this.displayTotalScore();
		
		//Adjust the height based on the panels added
		this.setPreferredSize(new Dimension(this.getWidth(), _height));
		
	}
	
	/**
	 * Adds a panel below the last added panel.
	 * 
	 * ONLY use this method to add a JPanel,
	 * do not use add(...)
	 * 
	 * @param panel
	 */

	public void addPanelBelow(JPanel panel)
	{	
		int vGap = 5;
		_height += panel.getPreferredSize().height + vGap;
		panel.setBackground(this.getBackground());
		
		if(_abovePanel == null)
		{
			_abovePanel = new JPanel();
			_abovePanel.setSize(new Dimension(this.getWidth(),0));
			_abovePanel.setPreferredSize(new Dimension(this.getWidth(),0));
			this.add(_abovePanel);
		}
		
		_mainLayout.putConstraint(SpringLayout.NORTH, panel, vGap, SpringLayout.SOUTH, _abovePanel);
		_mainLayout.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, _abovePanel);
		this.add(panel);
		
		_abovePanel = panel;
	}

	private void displayAssignment()
	{
		JPanel panel = new JPanel();
		
		JLabel asgn = new JLabel("<html><b>Assignment " + _rubric.Number + " Grader Sheet: " + _rubric.Name + "</b></html>", JLabel.CENTER);
		panel.add(asgn);
		
		panel.setPreferredSize(new Dimension(this.getWidth(), asgn.getPreferredSize().height + 10));
		this.addPanelBelow(panel);
	}
	
	private void displayStudentAndGrader()
	{
		int height = 0;
		int vGap = 0;
		
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		
		//Student
		JLabel studentAcct = new JLabel(" Student's account: " + _rubric.Student.Acct);
		panel.add(studentAcct);
		height += studentAcct.getPreferredSize().height + vGap;
		
		JLabel studentName = new JLabel(" Student's name: " + _rubric.Student.Name);
		vGap = 2;
		layout.putConstraint(SpringLayout.NORTH, studentName, vGap, SpringLayout.SOUTH, studentAcct);
		panel.add(studentName);
		height += studentName.getPreferredSize().height + vGap;
		
		//Grader
		JLabel graderAcct = new JLabel(" Grader's account: " + _rubric.Grader.Acct);
		vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, graderAcct, vGap, SpringLayout.SOUTH, studentName);
		panel.add(graderAcct);
		height += graderAcct.getPreferredSize().height + vGap;
		
		JLabel graderName = new JLabel(" Grader's name: " + _rubric.Grader.Name);
		vGap = 2;
		layout.putConstraint(SpringLayout.NORTH, graderName, vGap, SpringLayout.SOUTH, graderAcct);
		panel.add(graderName);
		height += graderName.getPreferredSize().height + vGap;
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height));
		
		this.addPanelBelow(panel);
	}
	
	private void displaySection(Section section)
	{
		int height = 0;
		
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		
		//Section name
		JLabel sectionName = new JLabel("<html><b>" + section.Name + "</b></html>");
		int vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, sectionName, vGap, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, sectionName, 2, SpringLayout.WEST, panel);
		panel.add(sectionName);
		JComponent elemAbove = sectionName;
		height += sectionName.getPreferredSize().height + vGap;
		
		//For each subsection
		for(Subsection subsection : section.Subsections)
		{	
			//Section name
			JTextArea subsectionName = new JTextArea(subsection.Name);
			subsectionName.setSize(new Dimension(450,200));
			subsectionName.setEditable(false);
			subsectionName.setLineWrap(true);
			subsectionName.setWrapStyleWord(true);
			subsectionName.setBackground(this.getBackground());
			vGap = 10;
			layout.putConstraint(SpringLayout.NORTH, subsectionName, vGap, SpringLayout.SOUTH, elemAbove);
			layout.putConstraint(SpringLayout.WEST, subsectionName, 5, SpringLayout.WEST, panel);
			panel.add(subsectionName);
			elemAbove = subsectionName;
			height += subsectionName.getPreferredSize().height + vGap;
			
			//Score field
			NumberField scoreField;
			if(subsection.Name.equals("Design Check"))
			{
				//Design check score should not be modifiable
				scoreField = NumberField.getAsUneditable(subsection.Score);
				scoreField.setFocusable(false);
			}
			else
			{
				scoreField = NumberField.getAsScore(subsection, this, _stateManager);
				_tabOrder.add(scoreField);
			}
			layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, subsectionName);
			layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, subsectionName);
			panel.add(scoreField);
			
			//OutOf field
			NumberField outOfField = NumberField.getAsUneditable(subsection.OutOf);
			outOfField.setFocusable(false);
			layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
			layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, subsectionName);
			panel.add(outOfField);
			
			//For each detail
			for(Detail detail : subsection.Details)
			{
				//The detail's name, and the point values associated with
                JTextArea detailName;
                if(detail.Value == 0)
                {
                    detailName = new JTextArea(detail.Name);
                }
                else
                {
                    detailName = new JTextArea(detail.Name + " (" + detail.Value+" points)");
                }
				detailName.setSize(new Dimension(250,200));
				detailName.setEditable(false);
				detailName.setLineWrap(true);
				detailName.setWrapStyleWord(true);
				detailName.setBackground(this.getBackground());
				vGap = 3;
				layout.putConstraint(SpringLayout.NORTH, detailName, 3, SpringLayout.SOUTH, elemAbove);
				layout.putConstraint(SpringLayout.WEST, detailName, 25, SpringLayout.WEST, subsectionName);
				panel.add(detailName);
				elemAbove = detailName;
				height += detailName.getPreferredSize().height + vGap;
			}
		}
		
		//Notes
		if(!section.Notes.isEmpty())
		{
			JLabel notesLbl = new JLabel("Notes");
			vGap = 5;
			layout.putConstraint(SpringLayout.NORTH, notesLbl, vGap, SpringLayout.SOUTH, elemAbove);
			layout.putConstraint(SpringLayout.WEST, notesLbl, 5, SpringLayout.WEST, panel);
			elemAbove = notesLbl;
			height += notesLbl.getPreferredSize().height + vGap;
			panel.add(notesLbl);
			
			TextField notes = TextField.getAsNotesField(section);
			notes.setFocusable(false);
			vGap = 3;
			layout.putConstraint(SpringLayout.NORTH, notes, vGap, SpringLayout.SOUTH, elemAbove);
			layout.putConstraint(SpringLayout.WEST, notes, 0, SpringLayout.WEST, notesLbl);
			height += notes.getPreferredSize().height + vGap;
			elemAbove = notes;
			panel.add(notes);
		}
		
		//Comments
		JLabel commentLbl = new JLabel("Comments");
		vGap = 5;
		layout.putConstraint(SpringLayout.NORTH, commentLbl, vGap, SpringLayout.SOUTH, elemAbove);
		layout.putConstraint(SpringLayout.WEST, commentLbl, 5, SpringLayout.WEST, panel);
		elemAbove = commentLbl;
		height += commentLbl.getPreferredSize().height + vGap;
		panel.add(commentLbl);
		
		TextField comments = TextField.getAsCommentField(section, _stateManager);
		comments.setSize(new Dimension(400,500));
		JScrollPane scroll = new JScrollPane(comments);
		vGap = 3;
		layout.putConstraint(SpringLayout.NORTH, scroll, vGap, SpringLayout.SOUTH, elemAbove);
		layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, commentLbl);
		height += scroll.getPreferredSize().height + vGap;
		elemAbove = scroll;
		panel.add(scroll);
		
		//Display section totals
		JTextArea sectionTotal = new JTextArea("Section Total");
		sectionTotal.setSize(new Dimension(450,200));
		sectionTotal.setEditable(false);
		sectionTotal.setLineWrap(true);
		sectionTotal.setWrapStyleWord(true);
		sectionTotal.setBackground(this.getBackground());
		vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, sectionTotal, vGap, SpringLayout.SOUTH, elemAbove);
		layout.putConstraint(SpringLayout.WEST, sectionTotal, 5, SpringLayout.WEST, panel);
		panel.add(sectionTotal);
		elemAbove = sectionTotal;
		height += sectionTotal.getPreferredSize().height + vGap + 5;
		
		//Score field
		NumberField scoreField = NumberField.getAsUneditable(section.getSectionScore());
		scoreField.setFocusable(false);
		_totals.add(scoreField);
		_sections.add(section);
		layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, sectionTotal);
		layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(scoreField);
		
		//OutOf field
		NumberField outOfField = NumberField.getAsUneditable(section.getSectionOutOf());
		outOfField.setFocusable(false);
		layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
		layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(outOfField);
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
		this.addPanelBelow(panel);
	}
	
	private void displayExtraCredit(Rubric rubric)
	{
		int height = 0;
		
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		
		//ExtraCredit header
		JLabel extraCreditName = new JLabel("Bells & Whistles");
		int vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, extraCreditName, vGap, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, extraCreditName, 2, SpringLayout.WEST, panel);
		panel.add(extraCreditName);
		JComponent elemAbove = extraCreditName;
		height += extraCreditName.getPreferredSize().height + vGap;
		
		boolean editable = true;
		//Get time status - if handed in late, no extra credit
		TimeStatus status = TimeStatus.getStatus(rubric.Status);
		if(status.isLate())
		{
			editable = false;
		}
		
		//For each ExtraCredit section
		for(ExtraCredit extraCredit : rubric.ExtraCredit)
		{	
			//Section name
			JTextArea extraCreditText = new JTextArea(extraCredit.Text);
			extraCreditText.setSize(new Dimension(450,200));
			extraCreditText.setEditable(false);
			extraCreditText.setLineWrap(true);
			extraCreditText.setWrapStyleWord(true);
			extraCreditText.setBackground(this.getBackground());
			vGap = 10;
			layout.putConstraint(SpringLayout.NORTH, extraCreditText, vGap, SpringLayout.SOUTH, elemAbove);
			layout.putConstraint(SpringLayout.WEST, extraCreditText, 5, SpringLayout.WEST, panel);
			panel.add(extraCreditText);
			elemAbove = extraCreditText;
			height += extraCreditText.getPreferredSize().height + vGap + 5;
			
			//Score field
			NumberField scoreField;
			if(editable)
			{
				scoreField = NumberField.getAsScore(extraCredit, this, _stateManager);
				_tabOrder.add(scoreField);
			}
			else
			{
				scoreField = NumberField.getAsUneditable(0.0);
			}
			layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, extraCreditText);
			layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, extraCreditText);
			panel.add(scoreField);
			
			//OutOf field
			NumberField outOfField = NumberField.getAsUneditable(extraCredit.OutOf);
			outOfField.setFocusable(false);
			layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
			layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, extraCreditText);
			panel.add(outOfField);
	
		}
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height));
		this.addPanelBelow(panel);
	}
	
	private void displayStatus(Rubric rubric)
	{
		int height = 0;
		
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		
		//ExtraCredit header
		JLabel handinStatusLabel = new JLabel("Handin Status");
		int vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, handinStatusLabel, vGap, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, handinStatusLabel, 2, SpringLayout.WEST, panel);
		panel.add(handinStatusLabel);
		JComponent elemAbove = handinStatusLabel;
		height += handinStatusLabel.getPreferredSize().height + vGap;
		
		//Status
		TimeStatus status = TimeStatus.getStatus(_rubric.Status);
		JTextArea extraCreditText = new JTextArea(status.getPrettyPrintName());
		extraCreditText.setSize(new Dimension(450,200));
		extraCreditText.setEditable(false);
		extraCreditText.setLineWrap(true);
		extraCreditText.setWrapStyleWord(true);
		extraCreditText.setBackground(this.getBackground());
		vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, extraCreditText, vGap, SpringLayout.SOUTH, elemAbove);
		layout.putConstraint(SpringLayout.WEST, extraCreditText, 5, SpringLayout.WEST, panel);
		panel.add(extraCreditText);
		elemAbove = extraCreditText;
		height += extraCreditText.getPreferredSize().height + vGap + 5;
		
		//Total possible score
		double sc = _rubric.getTotalOutOf();
		//Score field
		double statusPoints = status.getEarlyBonus(sc) + status.getLatePenalty(sc);
		NumberField scoreField = NumberField.getAsUneditable(Double.valueOf(Utils.doubleToString(statusPoints)));
		layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, extraCreditText);
		layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, extraCreditText);
		panel.add(scoreField);
			
		//OutOf field
		NumberField outOfField = NumberField.getAsUneditable(0.0);
		outOfField.setFocusable(false);
		layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
		layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, extraCreditText);
		panel.add(outOfField);
	
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
		this.addPanelBelow(panel);
	}
	
	private void displayTotalScore()
	{
		int height = 0;
		
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		
		//ExtraCredit header
		JLabel extraCreditName = new JLabel("<html><b>Total Score</b></html>");
		int vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, extraCreditName, vGap, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, extraCreditName, 2, SpringLayout.WEST, panel);
		panel.add(extraCreditName);
		JComponent elemAbove = extraCreditName;
		height += extraCreditName.getPreferredSize().height + vGap;
		
		//Display section totals
		JTextArea sectionTotal = new JTextArea("Final Grade");
		sectionTotal.setSize(new Dimension(450,200));
		sectionTotal.setEditable(false);
		sectionTotal.setLineWrap(true);
		sectionTotal.setWrapStyleWord(true);
		sectionTotal.setBackground(this.getBackground());
		vGap = 10;
		layout.putConstraint(SpringLayout.NORTH, sectionTotal, vGap, SpringLayout.SOUTH, elemAbove);
		layout.putConstraint(SpringLayout.WEST, sectionTotal, 5, SpringLayout.WEST, panel);
		panel.add(sectionTotal);
		elemAbove = sectionTotal;
		height += sectionTotal.getPreferredSize().height + vGap + 15;
		
		//Score field
		NumberField scoreField = NumberField.getAsUneditable(_rubric.getTotalScore());
		scoreField.setFocusable(false);
		_totalScoreField = scoreField;
		layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, sectionTotal);
		layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(scoreField);
		
		//OutOf field
		NumberField outOfField = NumberField.getAsUneditable(_rubric.getTotalOutOf());
		outOfField.setFocusable(false);
		layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
		layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(outOfField);
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height));
		this.addPanelBelow(panel);
	}
	
	public void updateTotals() 
	{
		for(int i=0; i<_totals.size(); i++)
		{
			Section section = _sections.elementAt(i);
			NumberField score = _totals.elementAt(i);
			score.setText(Utils.doubleToString(section.getSectionScore()));
		}
		_totalScoreField.setText(Utils.doubleToString(_rubric.getTotalScore()));
	}	
	
	public Vector<Component> getTabOrder()
	{
		return _tabOrder;
	}

}

