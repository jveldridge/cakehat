package rubric;

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
import java.util.Collection;
import java.util.HashMap;
import javax.imageio.ImageIO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import utils.Allocator;

/**
 * Allows for viewing and editing a rubric. Intended to be used to fill out a
 * rubric while grading. Create a GradingVisualizer by using RubricManager's
 * view(...) method.
 *
 * @author jak2
 * @auther spoletto
 */
class GradingVisualizer extends JFrame
{
    private Rubric _rubric;
    private Map<String, String> _group;
    private boolean _isAdmin;
    private StateManager _stateManager;

    GradingVisualizer(final Rubric rubric, final boolean isAdmin)
    {
        super("Grading " + rubric.getStudentAccount() + "'s " + rubric.getName());

        _rubric = rubric;
        _isAdmin = isAdmin;

        //Load window icon
        try
        {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/format-justify-fill.png")));
        }
        catch (IOException e) {}

        //Saving and state manager
        JButton saveButton = new JButton("Save Changes  ");
        saveButton.setIconTextGap(20);
        try
        {
            saveButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/16x16/media-floppy.png"))));
        }
        catch (Exception e) {}
        _stateManager = new StateManager(saveButton, true);
        saveButton.setEnabled(false);

        //Panels
        this.setLayout(new BorderLayout());
        RubricPanel rubricPanel = new RubricPanel(rubric, _stateManager, isAdmin);
        final JScrollPane scrollPane = new JScrollPane(rubricPanel);
        Dimension size = new Dimension(rubricPanel.getPreferredSize().width + 30, 800);
        scrollPane.setPreferredSize(size);
        scrollPane.setSize(size);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        //Get all members of the group and their names
        Collection<String> groupLogins = Allocator.getDatabaseIO().getGroup(rubric._handinPart, rubric.getStudentName());
        Map<String, String> allStudents = Allocator.getDatabaseIO().getAllStudents();
        _group = new HashMap<String, String>();
        for(String login : groupLogins)
        {
            _group.put(login, allStudents.get(login));
        }

        //Save action
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                save();
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
                if (_stateManager.beenSaved())
                {
                    GradingVisualizer.this.dispose();
                }
                else
                {
                    int chosenOption = JOptionPane.showConfirmDialog(GradingVisualizer.this, "Would you like to save before exiting?");
                    if (chosenOption == JOptionPane.YES_OPTION)
                    {
                        save();
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
                scrollPane.getViewport().setViewPosition(new Point(0, 0));
            }
        });
            
        //Show
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void save()
    {
        for(String login : _group.keySet())
        {
            _rubric.setStudent(_group.get(login), login);
            String gmlPath = Allocator.getRubricManager().getStudentRubricPath(_rubric._handinPart, login);
            RubricGMLWriter.write(_rubric, gmlPath);
        }

        //If an admin is saving this rubric, write to that database
        if(_isAdmin)
        {
            double score = _rubric.getTotalHandinScore();

            for(String login : _group.keySet())
            {
                Allocator.getDatabaseIO().enterGrade(login, _rubric._handinPart, score);
            }
        }

        _stateManager.rubricSaved();
    }
}