package rubric.visualizers;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;

import java.awt.Point;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.HashSet;
import java.util.Set;

import rubric.*;

import utils.Allocator;

public class GradingVisualizer extends JFrame
{
    public GradingVisualizer(String asgn, String graderAcct, String studentAcct)
    {
        super("Grading " + studentAcct + "'s " + asgn);

        //Load window icon
        try
        {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/format-justify-fill.png")));
        }
        catch (IOException e) { }

        //Get grading rubric
        final String XMLFilePath = Allocator.getGradingUtilities().getStudentRubricPath(asgn, studentAcct);
        final Rubric rubric = RubricManager.processXML(XMLFilePath);

        //Saving and state manager
        JButton saveButton = new JButton("Save Changes  ");
        saveButton.setIconTextGap(20);
        try
        {
            saveButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/16x16/media-floppy.png"))));
        }
        catch (Exception e) { }
        final StateManager stateManager = new StateManager(saveButton, true);
        saveButton.setEnabled(false);

        //Panels
        this.setLayout(new BorderLayout());

        RubricPanel rubricPanel = new RubricPanel(rubric, stateManager);

        final JScrollPane scrollPane = new JScrollPane(rubricPanel);
        Dimension size = new Dimension(rubricPanel.getPreferredSize().width + 30, 800);
        scrollPane.setPreferredSize(size);
        scrollPane.setSize(size);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        //Save action
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                RubricManager.writeToXML(rubric, XMLFilePath);
                stateManager.rubricSaved();
            }
        });
        //Save panel
        JPanel savePanel = new JPanel();
        savePanel.add(saveButton);
        this.add(savePanel, BorderLayout.SOUTH);

        //Fit everything visual together properly
        this.pack();

        //Handle closing
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //Open up a dialog on window close to save rubric data
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (stateManager.beenSaved())
                {
                    GradingVisualizer.this.dispose();
                }
                else
                {
                    int chosenOption = JOptionPane.showConfirmDialog(GradingVisualizer.this, "Would you like to save before exiting?");
                    if (chosenOption == JOptionPane.YES_OPTION)
                    {
                        RubricManager.writeToXML(rubric, XMLFilePath);
                        GradingVisualizer.this.dispose();
                    }
                    if (chosenOption == JOptionPane.NO_OPTION)
                    {
                        GradingVisualizer.this.dispose();
                    }
                    if (chosenOption == JOptionPane.CANCEL_OPTION) { }
                }
            }
        });

        //Add custom focus traversal policy
        this.setFocusTraversalPolicy(new GradingFocusTraversalPolicy(rubricPanel.getTabOrder(), scrollPane.getVerticalScrollBar()));

        //Add enter as forward tab key
        Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

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
}