package backend.visualizer;

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

import frontend.grader.rubric.*;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import utils.Allocator;

public class TemplateVisualizer extends JFrame
{

    public TemplateVisualizer(String asgn) {
            super("Rubric Preview for " + asgn);
            this.setVisible(true);

            //Get grading rubric
            final String XMLFilePath = Allocator.getConstants().getAssignmentDir() + asgn + "/" + Allocator.getConstants().getTemplateGradeSheetFilename();
            System.out.println(XMLFilePath);
            final Rubric rubric = RubricManager.processXML(XMLFilePath);
            rubric.Status = "ON_TIME";

            //Configure basic properties
            this.setLayout(new BorderLayout());

            VisualPanel mp = new VisualPanel(rubric);

            JScrollPane vizScrollPane = new JScrollPane(mp);
            Dimension size = new Dimension(mp.getPreferredSize().width + 30, 800);
            vizScrollPane.setPreferredSize(size);
            vizScrollPane.setSize(size);

            vizScrollPane.getVerticalScrollBar().setUnitIncrement(16);
            this.add(vizScrollPane, BorderLayout.CENTER);
            
            this.pack();

            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            //Open up a dialog on window close to save rubric data
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    TemplateVisualizer.this.dispose();
                }
            });


        }
    }

