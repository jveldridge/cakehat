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
import utils.Constants;

public class TemplateVisualizer extends JFrame
{

    public TemplateVisualizer(String asgn) {
            super("Rubric Preview for " + asgn);
            this.setVisible(true);

            //Get grading rubric
            final String XMLFilePath = Constants.TEMPLATE_GRADE_SHEET_DIR + asgn + "/" + Constants.TEMPLATE_GRADE_SHEET_FILENAME;
            System.out.println(XMLFilePath);
            final Rubric rubric = RubricManager.processXML(XMLFilePath);
            rubric.Status = "ON_TIME";

            //Configure basic properties
            this.setLayout(new BorderLayout());

            VisualPanel mp = new VisualPanel(rubric);
            VizScrollPane mainPane = new VizScrollPane(mp);
            mainPane.getVerticalScrollBar().setUnitIncrement(16);
            this.add(mainPane, BorderLayout.CENTER);
            
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

