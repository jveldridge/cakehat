package gradesystem.rubric;

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
import java.awt.event.InputEvent;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import gradesystem.utils.Allocator;

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
        Collection<String> groupLogins = Allocator.getDatabaseIO().getGroup(rubric._handinPart, rubric.getStudentAccount());
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
                handleClose();
            }
        });

        //Add custom focus traversal policy
        this.setFocusTraversalPolicy(new GradingFocusTraversalPolicy(rubricPanel.getTabOrder(), scrollPane.getVerticalScrollBar()));

        //Add enter as forward tab key
        Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

        //Menu bar for saving and closing
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("File");
        bar.add(menu);
        JMenuItem item = new JMenuItem("Save");
        item.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
        menu.add(item);
        item = new JMenuItem("Close");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                handleClose();
            }
        });
        menu.add(item);
        this.setJMenuBar(bar);

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

    /**
     * Handle a request to close the window. Prompts the user if they want to save, if applicable.
     */
    private void handleClose()
    {
        if (_stateManager.beenSaved())
        {
            this.dispose();
        }
        else
        {
            int chosenOption = JOptionPane.showConfirmDialog(GradingVisualizer.this, "Would you like to save before exiting?");
            if (chosenOption == JOptionPane.YES_OPTION)
            {
                save();
                this.dispose();
            }
            if (chosenOption == JOptionPane.NO_OPTION)
            {
                this.dispose();
            }
            if (chosenOption == JOptionPane.CANCEL_OPTION) { }
        }
    }

    /**
     * Saves the changes made to all of the effected GML files (can be multiple if part of a group).
     */
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