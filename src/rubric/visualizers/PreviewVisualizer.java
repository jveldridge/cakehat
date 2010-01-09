package rubric.visualizers;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import rubric.Rubric;
import rubric.RubricException;
import rubric.RubricManager;

import utils.Allocator;
import utils.ErrorView;

/**
 * Used to view the rubric for a given assignment. Any changes made to the rubric
 * in this view will not be saved.
 *
 * @author spoletto
 * @author jak2
 */
public class PreviewVisualizer extends JFrame
{
    /**
     * Views an assignment's template rubric.
     *
     * @param asgn
     */
    public PreviewVisualizer(config.HandinPart part)
    {
        super("Rubric Preview for " + part.getAssignment().getName());

        Rubric rubric = part.getRubric();

        //Preview status as if on time (necessary as visualizer expects a status)
        rubric.Status = "ON_TIME";

        //Layout
        this.setLayout(new BorderLayout());

        //Panels
        RubricPanel rubricPanel = new RubricPanel(rubric, null);
        final JScrollPane scrollPane = new JScrollPane(rubricPanel);
        Dimension size = new Dimension(rubricPanel.getPreferredSize().width + 30, 800);
        scrollPane.setPreferredSize(size);
        scrollPane.setSize(size);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

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

        //Fit everything together
        this.pack();

                //On window open, scroll to top
        this.addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
                scrollPane.getViewport().setViewPosition(new Point(0,0));
            }
        });

        //Show
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * TODO: After new configuration/assignment code is incorporated, remove this constructor
     *
     * @param asgn
     */
    public PreviewVisualizer(String asgn)
    {
        super("Rubric Preview for " + asgn);
        try {
            Rubric rubric = RubricManager.processXML(Allocator.getConstants().getAssignmentDir() + asgn + "/" + Allocator.getConstants().getTemplateGradeSheetFilename());
            //Preview status as if on time (necessary as visualizer expects a status)
            rubric.Status = "ON_TIME";
            //Layout
            this.setLayout(new BorderLayout());
            //Panels
            RubricPanel rubricPanel = new RubricPanel(rubric, null);
            final JScrollPane scrollPane = new JScrollPane(rubricPanel);
            Dimension size = new Dimension(rubricPanel.getPreferredSize().width + 30, 800);
            scrollPane.setPreferredSize(size);
            scrollPane.setSize(size);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            this.add(scrollPane, BorderLayout.CENTER);
            //Handle closing
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            //Open up a dialog on window close to save rubric data
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    PreviewVisualizer.this.dispose();
                }
            });
            //Fit everything together
            this.pack();
            //On window open, scroll to top
            this.addWindowListener(new WindowAdapter() {

                public void windowOpened(WindowEvent e) {
                    scrollPane.getViewport().setViewPosition(new Point(0, 0));
                }
            });
            //Show
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
        catch (RubricException ex) {
            new ErrorView(ex);
        }
    }
}