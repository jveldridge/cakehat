package cakehat.rubric;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Used to view the rubric for a given assignment. Any changes made to the rubric
 * in this view will not be saved.
 *
 * @author spoletto
 * @author jak2
 */
@Deprecated
class TemplateVisualizer extends JFrame
{
    TemplateVisualizer(Rubric rubric)
    {
        super("Rubric Preview for " + rubric.getDistributablePart().getAssignment().getName() +
              ": " + rubric.getDistributablePart().getName());

        //Layout
        this.setLayout(new BorderLayout());

        //Panels
        RubricPanel rubricPanel = new RubricPanel(rubric, null, false);
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
            @Override
            public void windowClosing(WindowEvent e)
            {
                TemplateVisualizer.this.dispose();
            }
        });

        //Fit everything together
        this.pack();

        //On window open, scroll to top
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                scrollPane.getViewport().setViewPosition(new Point(0,0));
            }
        });

        //Show
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}