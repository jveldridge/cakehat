
package frontend.grader;

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
import utils.Allocator;
import utils.basicXMLviewer.BasicNumberField;
import utils.basicXMLviewer.BasicTextField;

class MainPanel extends utils.basicXMLviewer.ViewerPanel
{
	private StateManager _stateManager;
	
	public MainPanel(Rubric rubric, StateManager manager)
	{
            super(rubric);
            _stateManager = manager;
	}
	
    @Override
	protected void displaySection(Section section)
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
			BasicNumberField scoreField;
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
			BasicNumberField outOfField = NumberField.getAsUneditable(subsection.OutOf);
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
			
			BasicTextField notes = TextField.getAsNotesField(section);
                        notes.setSize(new Dimension(400,500));
                        JScrollPane scroll = new JScrollPane(notes);
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
		BasicNumberField scoreField = NumberField.getAsUneditable(section.getSectionScore());
		scoreField.setFocusable(false);
		_totals.add(scoreField);
		_sections.add(section);
		layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, sectionTotal);
		layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(scoreField);
		
		//OutOf field
		BasicNumberField outOfField = NumberField.getAsUneditable(section.getSectionOutOf());
		outOfField.setFocusable(false);
		layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
		layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, sectionTotal);
		panel.add(outOfField);
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
		this.addPanelBelow(panel);
	}
	
    @Override
	protected void displayExtraCredit(Rubric rubric)
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
			BasicNumberField scoreField;
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
			BasicNumberField outOfField = NumberField.getAsUneditable(extraCredit.OutOf);
			outOfField.setFocusable(false);
			layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
			layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, extraCreditText);
			panel.add(outOfField);
	
		}
		
		panel.setPreferredSize(new Dimension(this.getWidth(), height));
		this.addPanelBelow(panel);
	}
	
    @Override
	protected void displayTotalScore()
	{
            super.displayTotalScore();
            _totalScoreField = NumberField.getAsUneditable(_rubric.getTotalScore());
	}
	
	public void updateTotals() 
	{
		for(int i=0; i<_totals.size(); i++)
		{
			Section section = _sections.elementAt(i);
			BasicNumberField score = _totals.elementAt(i);
			score.setText(Allocator.getGeneralUtilities().doubleToString(section.getSectionScore()));
		}
		_totalScoreField.setText(Allocator.getGeneralUtilities().doubleToString(_rubric.getTotalScore()));
	}	
	
	public Vector<Component> getTabOrder()
	{
		return _tabOrder;
	}

}