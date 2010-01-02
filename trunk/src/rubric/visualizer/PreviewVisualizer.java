package rubric.visualizer;

/**
 * Class used to visualize XML stencils created for
 * each assignment.
 * 
 * @author spoletto
 */

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import rubric.*;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import utils.Allocator;

public class PreviewVisualizer extends JFrame
{
    
    public PreviewVisualizer(String asgn)
    {
        super("Rubric Preview for " + asgn);
        this.setVisible(true);

        //Get grading rubric
        String XMLFilePath = Allocator.getConstants().getAssignmentDir() + asgn + "/" + Allocator.getConstants().getTemplateGradeSheetFilename();
        Rubric rubric = RubricManager.processXML(XMLFilePath);
        //Preview status as if on time (necessary as visualizer expects a status
        rubric.Status = "ON_TIME";

        //Layout
        this.setLayout(new BorderLayout());

        //Panels
        RubricPanel rubricPanel = new RubricPanel(rubric, null);
        JScrollPane scrollPane = new JScrollPane(rubricPanel);
        Dimension size = new Dimension(rubricPanel.getPreferredSize().width + 30, 800);
        scrollPane.setPreferredSize(size);
        scrollPane.setSize(size);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        //Fit everything together
        this.pack();

        //Handle closing
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //Open up a dialog on window close to save rubric data
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                PreviewVisualizer.this.dispose();
            }
        });
    }
    
}