package gradesystem.rubric;

import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.views.shared.ErrorView;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import javax.swing.JFrame;


import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

/**
 * Allows for viewing and editing a rubric. Intended to be used to fill out a
 * rubric while grading. Create a GradingVisualizer by using RubricManager's
 * view(...) method.
 *
 * @author jak2
 * @author spoletto
 */
class GradingVisualizer extends JFrame
{
    private Rubric _rubric;
    private DistributablePart _distPart;
    private Group _group;
    private boolean _isAdmin;
    private StateManager _stateManager;

    GradingVisualizer(final Rubric rubric, final boolean isAdmin)
    {
        super("Grading " + rubric.getGroup().getName() + "'s " +
              rubric.getDistributablePart().getAssignment().getName() + ": " + rubric.getDistributablePart().getName());

        _rubric = rubric;
        _group = rubric.getGroup();
        _distPart = rubric.getDistributablePart();
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
        _stateManager = new StateManager(saveButton);
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
            @Override
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
            @Override
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
     * Saves the changes made to the GML file
     */
    private void save() {
        String gmlPath = Allocator.getRubricManager().getGroupRubricPath(_distPart, _group);

        try {
            RubricGMLWriter.write(_rubric, gmlPath);
        } catch (RubricException e) {
            new ErrorView(e, "Unable to save rubric.");
            return;
        }

        //If an admin is saving this rubric, write to that database
        if (_isAdmin) {
            double score = _rubric.getTotalDistPartScore();

            if (!_stateManager.isRubricSaved()) {
                try {
                    Allocator.getDatabaseIO().enterGrade(_group, _distPart, score);
                    _stateManager.rubricSaved();
                } catch (SQLException ex) {
                    new ErrorView(ex, "The new grade for group " + _group + " on part "
                            + _distPart + " of assignment " + _distPart.getAssignment() + " could not be "
                            + "stored in the database.");
                }
            }

            if (!_stateManager.isStatusSaved()) {
                try {
                    Allocator.getDatabaseIO().setHandinStatus(_distPart.getHandin(), _group, _stateManager.getHandinStatus());
                    _stateManager.statusSaved();
                } catch (SQLException ex) {
                    new ErrorView(ex, "The new handin status for group " + _group + " on "
                            + "assignment " + _distPart.getAssignment() + " could not be stored in the database.");
                }
            }
        }
        else {
            _stateManager.rubricSaved();
        }
    }
}