package gradesystem.views.frontend;

import gradesystem.components.GenericJList;
import gradesystem.handin.ActionException;
import gradesystem.rubric.RubricException;
import gradesystem.services.ServicesException;
import gradesystem.config.TA;
import gradesystem.CakehatAboutBox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import gradesystem.Allocator;
import gradesystem.components.StringConverter;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.resources.icons.IconLoader;
import gradesystem.resources.icons.IconLoader.IconImage;
import gradesystem.resources.icons.IconLoader.IconSize;
import gradesystem.rubric.RubricSaveListener;
import gradesystem.views.shared.ErrorView;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingConstants;

/**
 * A frontend view to be used by TAs that are grading.
 * <br/><br/>
 * In order to have a very responsive user interface, this class uses a non-UI
 * thread to load information. Whenver these changes lead to UI changes, the
 * code that will do this is placed on the AWT Event Queue. In particular,
 * {@link #_assignedGroups} can be loaded using a non-UI thread. This is done
 * via the {@link #loadAssignedGrading(boolean)} method.
 *
 * @author jak2
 */
public class FrontendView extends JFrame implements RubricSaveListener
{
    /**
     * Label that displays the currently selected student.
     */
    private class CurrentlyGradingLabel extends JLabel
    {
        private final static String _begin ="<html><b>Currently Grading</b><br/>",
                                    _end = "</html>",
                                    _default = "None";

        public CurrentlyGradingLabel()
        {
            super(_begin + _default + _end);
        }

        public void update(Group group)
        {
            if(group == null)
            {
                this.setText(_begin + _default + _end);
            }
            else
            {
                this.setText(_begin + getGroupText(group) + _end);
            }
        }

        private String getGroupText(Group group)
        {
            String text;

            //If group assignment, show group name and members
            if(_dpList.getSelectedValue().getAssignment().hasGroups())
            {
                text = group.getName() + " " + group.getMembers();
            }
            //If not a group assignment, show name of student
            else if(!group.getMembers().isEmpty())
            {
                text = group.getMembers().get(0);
            }
            //A non-group assignment with no student, this situation should not arise
            else
            {
                text = "Unknown";
            }

            return text;
        }
    }

    public static void launch()
    {
        if(Allocator.getUserServices().isUserTA())
        {
            new FrontendView();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "You [" +
                                         Allocator.getUserUtilities().getUserLogin() +
                                         "] are not an authorized user.");
            System.exit(0);
        }
    }

    private final TA USER = Allocator.getUserServices().getUser();

    private GenericJList<DistributablePart> _dpList;
    private GenericJList<Group> _groupList;
    private JLabel _groupListLabel;
    private CurrentlyGradingLabel _currentlyGradingLabel;
    private JLabel _selectedGroupCommandsLabel;
    private JButton _demoButton, _gradingGuideButton, _printAllButton,
                    _submitGradingButton, _readmeButton, _openButton,
                    _testButton, _printButton, _gradeButton,
                    _runButton;
    private JButton[] _allButtons, _groupButtons;
    private Map<DistributablePart, List<GroupGradedStatus>> _assignedGroups;

    private FrontendView()
    {
        //Frame title
        super("[cakehat] frontend - " + Allocator.getUserUtilities().getUserLogin());

        //Create the directory to work in
        try {
            Allocator.getGradingServices().makeUserWorkspace();
        } catch (ServicesException ex) {
            new ErrorView(ex, "Could not make user cakehat workspace directory; " +
                             "functionality will be significantly impaired.  " +
                             "You are advised to restart cakehat and to send an " +
                             "error report if the problem persists.");
        }

        //Initialize GUI components necessary to show database information
        this.initializeComponents();

        //Retrieve the database information in a separate thread
        this.loadAssignedGrading(true);

        //Initialize more GUI components
        this.initializeFrameIcon();
        this.initializeMenuBar();

        this.createButtonGroups();
        
        //Setup close property
        this.initializeWindowCloseProperty();

        //Update button states
        this.updateButtonStates();

        //Display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Set up the groups of buttons that will be enabled or disabled.
     */
    private void createButtonGroups()
    {
        //Build group of buttons so they can be enabled/disabled appropriately
        _allButtons = new JButton[] {
                                      _demoButton, _gradingGuideButton,
                                      _printAllButton,_submitGradingButton,
                                      _readmeButton, _openButton,
                                      _testButton, _printButton,
                                      _gradeButton, _runButton
                                    };
        _groupButtons = new JButton[] {
                                        _readmeButton, _openButton,
                                        _printButton, _testButton,
                                        _runButton, _gradeButton
                                      };
    }

    /**
     * Called when a different DistributablePart is selected from the dpList
     * to update other GUI components
     */
    private void updateDPList()
    {
        DistributablePart part = _dpList.getSelectedValue();

        if(part == null)
        {
            _groupList.clearList();
        }
        else
        {
            //Create directory for the part so GRD files can be created, even if
            //no handins have been unarchived
            File partDir = Allocator.getPathServices().getUserPartDir(part);

            try
            {
                Allocator.getFileSystemServices().makeDirectory(partDir);
            }
            catch(ServicesException e)
            {
                new ErrorView(e, "Unable to create user part directory: " + partDir.getAbsolutePath());
            }

            //Update visual references to student/group appropriately depending
            //on if the assignment the part belongs to is a group assignment
            if(part.getAssignment().hasGroups())
            {
                _groupListLabel.setText("<html><b>Group</b></html>");
                _selectedGroupCommandsLabel.setText("<html><b>Selected Group Commands</b></html>");
            }
            else
            {
                _groupListLabel.setText("<html><b>Student</b></html>");
                _selectedGroupCommandsLabel.setText("<html><b>Selected Student Commands</b></html>");
            }

            //Get the groups assigned for this distributable part
            this.populateGroupsList();
        }

        //Update buttons accordingly
        this.updateButtonStates();
    }

    /**
     * Populates the group list with the Groups that the TA has been assigned to
     * grade (as recorded in the database) for the selected DistributablePart.
     */
    private void populateGroupsList()
    {
        DistributablePart selected = _dpList.getSelectedValue();
        List<GroupGradedStatus> statuses = new ArrayList(_assignedGroups.get(selected));
        Collections.sort(statuses);
        List<Group> groups = new ArrayList<Group>();
        for(GroupGradedStatus status : statuses)
        {
            groups.add(status.getGroup());
        }
        _groupList.setStringConverter(new GroupConverter(selected));
        _groupList.setListData(groups, true);
        if(_groupList.isSelectionEmpty())
        {
            _groupList.selectFirst();
        }

        _currentlyGradingLabel.update(_groupList.getSelectedValue());
    }

    /**
     * Enable or disable buttons based on the distributable part selected.
     */
    private void updateButtonStates()
    {
        DistributablePart part = _dpList.getSelectedValue();
 
        //If there is no distributable part selected, disable all of the buttons
        if(part == null)
        {
            for(JButton button : _allButtons)
            {
                button.setEnabled(false);
            }
            return;
        }

        //General commands
        _demoButton.setEnabled(part.hasDemo());
        _gradingGuideButton.setEnabled(part.hasDeductionList());
        _submitGradingButton.setEnabled(part.hasRubricTemplate());
        _printAllButton.setEnabled(part.hasPrint());

        //Get selected student
        Group group = _groupList.getSelectedValue();

        //If no group is selected, disable all group buttons
        if(group == null)
        {
            for(JButton button : _groupButtons)
            {
                button.setEnabled(false);
            }

            _submitGradingButton.setEnabled(false);
            _printAllButton.setEnabled(false);
        }
        //If there is a student, enable buttons appropriately
        else
        {
            //Student buttons
            _gradeButton.setEnabled(part.hasRubricTemplate());
            _testButton.setEnabled(part.hasTester());
            _runButton.setEnabled(part.hasRun());
            _openButton.setEnabled(part.hasOpen());
            _printButton.setEnabled(part.hasPrint());

            boolean hasReadme = true;
            try {
                hasReadme = part.hasReadme(group);
            } catch (ActionException ex) {
                new ErrorView(ex, "Could not determine if group " + group + " has a README");
            }
            _readmeButton.setEnabled(hasReadme);
        }
    }

    /**
     * Creates all of the GUI components aside from the menu bar
     */
    private void initializeComponents()
    {
        final int gapSpace = 10;

        //Outer panel that centers the panel containing the content
        JPanel outerPanel = new JPanel(new BorderLayout(0, 0));
        outerPanel.add(Box.createVerticalStrut(gapSpace), BorderLayout.NORTH);
        outerPanel.add(Box.createVerticalStrut(gapSpace), BorderLayout.SOUTH);
        outerPanel.add(Box.createHorizontalStrut(gapSpace), BorderLayout.WEST);
        outerPanel.add(Box.createHorizontalStrut(gapSpace), BorderLayout.EAST);
        this.getContentPane().add(outerPanel);

        final int contentHeight = 315;
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        outerPanel.add(contentPanel, BorderLayout.CENTER);

        //Distributable Part list
        Dimension dpListPanelSize = new Dimension(190, contentHeight);
        Dimension dpLabelSize = new Dimension(dpListPanelSize.width, 13);
        Dimension dpListSize = new Dimension(dpListPanelSize.width,
                contentHeight - dpLabelSize.height);

        JPanel dpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        dpPanel.setPreferredSize(dpListPanelSize);
        JLabel dpLabel = new JLabel("<html><b>Assignment Part</b></html>");
        dpLabel.setPreferredSize(dpLabelSize);

        _dpList = new GenericJList<DistributablePart>(Collections.EMPTY_LIST, new DPConverter());
        _dpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _dpList.usePlainFont();
        _dpList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    updateDPList();
                }
            }
        });
        dpPanel.add(dpLabel);
        JScrollPane dpPane = new JScrollPane(_dpList);
        dpPane.setPreferredSize(dpListSize);
        dpPanel.add(dpPane);
        contentPanel.add(dpPanel);

        contentPanel.add(Box.createHorizontalStrut(gapSpace));

        //Group list
        Dimension groupListPanelSize = new Dimension(140, contentHeight);
        Dimension groupLabelSize = new Dimension(groupListPanelSize.width, 13);
        Dimension groupListSize = new Dimension(groupListPanelSize.width,
                groupListPanelSize.height - groupLabelSize.height);
        
        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        groupPanel.setPreferredSize(groupListPanelSize);
        _groupListLabel = new JLabel("Student");
        _groupListLabel.setPreferredSize(groupLabelSize);
        _groupList = new GenericJList<Group>();
        _groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _groupList.usePlainFont();
        _groupList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    _currentlyGradingLabel.update(_groupList.getSelectedValue());
                    updateButtonStates(); //To check for README
                }
            }
        });
        groupPanel.add(_groupListLabel);
        JScrollPane groupPane = new JScrollPane(_groupList);
        groupPane.setPreferredSize(groupListSize);
        groupPanel.add(groupPane);
        contentPanel.add(groupPanel);

        //When the left key is pressed, switch focus to the distributable part list
        _groupList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_LEFT == ke.getKeyCode())
                {
                    _dpList.grabFocus();
                }
            }
        });
        //When the right key is pressed, switch focus to the group list
        _dpList.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}

            public void keyPressed(KeyEvent ke)
            {
                if(KeyEvent.VK_RIGHT == ke.getKeyCode())
                {
                    _groupList.grabFocus();
                }
            }
        });

        contentPanel.add(Box.createHorizontalStrut(gapSpace));

        //Control Panel
        Dimension controlPanelSize = new Dimension(400, contentHeight);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.setPreferredSize(controlPanelSize);
        contentPanel.add(controlPanel);

        //Currently grading panel
        Dimension currentlyGradingSize = new Dimension(controlPanelSize.width, 28);
        _currentlyGradingLabel = new CurrentlyGradingLabel();
        _currentlyGradingLabel.setPreferredSize(currentlyGradingSize);
        controlPanel.add(_currentlyGradingLabel);

        //Split up the remaining space such that the buttons all have the same height
        final int labelHeight = 30;
        final int buttonGap = 4;
        int availableHeight = contentHeight - currentlyGradingSize.height -
                2 * labelHeight - 3 * buttonGap;
        int buttonHeight = availableHeight / 5;

        //General commands
        Dimension generalCommandsSize = new Dimension(controlPanelSize.width,
                labelHeight + buttonGap + 2 * buttonHeight);
        JPanel generalCommandsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        generalCommandsPanel.setPreferredSize(generalCommandsSize);
        JLabel generalCommandsLabel = new JLabel("<html><b>General Commands</b></html>");
        generalCommandsLabel.setPreferredSize(new Dimension(controlPanelSize.width, labelHeight));
        generalCommandsPanel.add(generalCommandsLabel);
        //General command buttons
        Dimension generalButtonsSize = new Dimension(generalCommandsSize.width,
                generalCommandsSize.height - labelHeight);
        JPanel generalButtonsPanel = new JPanel(new GridLayout(2, 2, buttonGap, buttonGap));
        generalButtonsPanel.setPreferredSize(generalButtonsSize);
        this.initializeGeneralCommandButtons(generalButtonsPanel);
        generalCommandsPanel.add(generalButtonsPanel);
        controlPanel.add(generalCommandsPanel);

        //Selected group commands
        Dimension groupCommandsSize = new Dimension(controlPanelSize.width,
                labelHeight + 2 * buttonGap + 3 * buttonHeight);
        JPanel groupCommandsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        groupCommandsPanel.setPreferredSize(groupCommandsSize);
        _selectedGroupCommandsLabel = new JLabel("<html><b>Selected Student Commands</b></html>");
        _selectedGroupCommandsLabel.setPreferredSize(new Dimension(controlPanelSize.width, labelHeight));
        groupCommandsPanel.add(_selectedGroupCommandsLabel);
        //Selected group command buttons
        Dimension studentButtonsSize = new Dimension(groupCommandsSize.width,
                groupCommandsSize.height - labelHeight);
        JPanel groupButtonsPanel = new JPanel(new GridLayout(3, 2, buttonGap, buttonGap));
        groupButtonsPanel.setPreferredSize(studentButtonsSize);
        this.initializeGroupCommandButtons(groupButtonsPanel);
        groupCommandsPanel.add(groupButtonsPanel);
        controlPanel.add(groupCommandsPanel);

        //Set content panel size based on the content in it
        int contentWidth = dpListPanelSize.width + groupListPanelSize.width +
                controlPanelSize.width + 2 * gapSpace;
        Dimension contentSize = new Dimension(contentWidth, contentHeight);
        contentPanel.setPreferredSize(contentSize);
    }

    /**
     * Create the menu bar
     */
    private void initializeMenuBar()
    {
        //Menu bar
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        //File menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        //Refresh item
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        refreshItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                loadAssignedGrading(true);
            }
        });
        menu.add(refreshItem);

        //Blacklist item
        JMenuItem blacklistItem = new JMenuItem("Modify Blacklist");
        blacklistItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        blacklistItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                // Invoke later so that the menu has time to dismiss
                EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        JPanel panel = disableFrame();

                        // Attempt to display the blacklist panel, if the necessary
                        // database information cannot be retrieved succesfully, do not
                        // show the blacklist panel
                        try
                        {
                            panel.add(new BlacklistPanel(panel.getPreferredSize(), FrontendView.this));
                        }
                        catch(SQLException e)
                        {
                            new ErrorView(e, "Unable to modify blacklist");
                            enableFrame();
                        }
                    }
                });
            }
        });
        menu.add(blacklistItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    Allocator.getGradingServices().removeUserWorkspace();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to remove your cakehat workspace directory.");
                }

                System.exit(0);
            }
        });
        menu.add(quitItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);

        //Help contents item
        JMenuItem helpItem = new JMenuItem("Help Contents");
        helpItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JOptionPane.showMessageDialog(FrontendView.this, "This feature is not yet available");
            }
        });
        menu.add(helpItem);

        //About
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(FrontendView.this);
            }
        });
        menu.add(aboutItem);
    }

    /**
     * Disables the menu and the content pane. Unlike
     * {@link JFrame#setEnabled(boolean) } and passing in false, this method
     * does not disable the glass pane. Instead, the glasspane is made visible
     * and semi-transparent areas are drawn over the frame's content and
     * menubar. A panel is returned which content may be put into. That panel
     * should provide so manner of re-enabling the frame via
     * {@link #enableFrame() }.
     *
     * @return panel to put content into
     *
     * @see #enableFrame()
     */
    private JPanel disableFrame()
    {
        final JPanel glassPane = (JPanel) FrontendView.this.getGlassPane();
        final Component contentPane = FrontendView.this.getContentPane();
        final int paddingSize = 15;

        // Disable pane
        JPanel disablePanel = new JPanel(new BorderLayout(0, 0))
        {
            private BufferedImage _contentPaneImage = null;

            @Override
            protected void paintComponent(Graphics g)
            {
                // Turn on anti-aliasing
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Disable a component in Swing such that all of its
                // subcomponents are disabled is an absurdly non-trivial task
                // To fake this, take the content pane and draw its appearance
                // to a BufferedImage, then hide the actual content pane and
                // draw the BufferedImage
                Rectangle contentPaneBounds = contentPane.getBounds();
                if(_contentPaneImage == null)
                {
                    _contentPaneImage = new BufferedImage(contentPaneBounds.width, contentPaneBounds.height, BufferedImage.TYPE_INT_ARGB);
                    contentPane.paint(_contentPaneImage.createGraphics());
                    
                    // Now hide the content pane
                    contentPane.setVisible(false);
                }
                g.drawImage(_contentPaneImage, contentPaneBounds.x, contentPaneBounds.y, Color.WHITE, this);

                // Draw a semi-transparent gray over everything
                Rectangle glassPaneBounds = glassPane.getBounds();
                g.setColor(new Color(192, 192, 192, 200));
                g.fillRect(glassPaneBounds.x, glassPaneBounds.y, glassPaneBounds.width, glassPaneBounds.height);

                // Draw a semi-transparent rounded rectangle with the top part
                // off the top of the screen
                int cornerRadius = 40;
                int shadingPadding = 10;
                g.setColor(new Color(128, 128, 128, 200));
                g.fillRoundRect(paddingSize - shadingPadding,
                        paddingSize - shadingPadding - cornerRadius,
                        glassPaneBounds.width - (2 * paddingSize) + 2 * shadingPadding,
                        glassPaneBounds.height - (2 * paddingSize) + 2 * shadingPadding + cornerRadius,
                        cornerRadius, cornerRadius);
            }
        };

        // Make the glass pane visible, add the disablePanel
        glassPane.setVisible(true);
        glassPane.setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));
        disablePanel.setPreferredSize(glassPane.getBounds().getSize());
        glassPane.removeAll();
        glassPane.add(disablePanel);

        // Entirely disable the menu bar
        final JMenuBar menuBar = FrontendView.this.getJMenuBar();
        menuBar.setEnabled(false);
        for(int i = 0; i < menuBar.getMenuCount(); i++)
        {
            menuBar.getMenu(i).setEnabled(false);
        }

        // Center an overlay panel that will hold the content
        final Dimension disablePanelSize = disablePanel.getPreferredSize();

        disablePanel.add(Box.createRigidArea(new Dimension(disablePanelSize.width, paddingSize)), BorderLayout.NORTH);
        disablePanel.add(Box.createRigidArea(new Dimension(disablePanelSize.width, paddingSize)), BorderLayout.SOUTH);
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height)), BorderLayout.WEST);
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height)), BorderLayout.EAST);

        JPanel overlayPanel = new JPanel(new BorderLayout(0, 0));
        overlayPanel.setBackground(new Color(0, 0, 0, 0));
        overlayPanel.setPreferredSize(new Dimension(contentPane.getWidth() - 2 * paddingSize,
                menuBar.getHeight() + contentPane.getHeight() - 2 * paddingSize));
        disablePanel.add(overlayPanel, BorderLayout.CENTER);

        return overlayPanel;
    }

    /**
     * Enables the frame, to be called after a call to {@link #disableFrame() }.
     * This method is package private to allow for separate classes such as
     * {@link BlacklistPanel} that are displayed in the glasspane overlay to
     * dismiss themselves and re-enable the frame.
     *
     * @see #disableFrame()
     */
    void enableFrame()
    {
        // Hide glass pane
        final JPanel glassPane = (JPanel) FrontendView.this.getGlassPane();
        glassPane.setVisible(false);

        // Entirely enable the menu bar
        final JMenuBar menuBar = FrontendView.this.getJMenuBar();
        menuBar.setEnabled(true);
        for(int i = 0; i < menuBar.getMenuCount(); i++)
        {
            menuBar.getMenu(i).setEnabled(true);
        }

        // Show the content pane
        final Component contentPane = FrontendView.this.getContentPane();
        contentPane.setVisible(true);
    }

    /**
     * Creates the buttons corresponding to the distributable part.
     *
     * @param generalButtonsPanel
     */
    private void initializeGeneralCommandButtons(JPanel generalButtonsPanel)
    {
        //Demo
        _demoButton = createButton(IconImage.APPLICATIONS_SYSTEM, "Demo");
        _demoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runDemoButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_demoButton);
        
        //Submit Grading
        _submitGradingButton = createButton(IconImage.MAIL_SEND_RECEIVE, "Submit Grading");
        _submitGradingButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                submitGradingButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_submitGradingButton);

        //Grading Guide
        _gradingGuideButton = createButton(IconImage.TEXT_X_GENERIC, "Grading Guide");
        _gradingGuideButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewDeductionsButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_gradingGuideButton);

        //Print All
        _printAllButton = createButton(IconImage.PRINTER, "Print All");
        _printAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printAllButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_printAllButton);
    }

    /**
     * Creates the student/group specific buttons.
     *
     * @param groupButtonsPanel
     */
    private void initializeGroupCommandButtons(JPanel groupButtonsPanel)
    {
        //Run
        _runButton = createButton(IconImage.GO_NEXT, "Run");
        _runButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runCodeButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_runButton);
        
        //Test
        _testButton = createButton(IconImage.UTILITIES_SYSTEM_MONITOR, "Test");
        _testButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                runTesterButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_testButton);

        //Open
        _openButton = createButton(IconImage.DOCUMENT_OPEN, "Open");
        _openButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                openCodeButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_openButton);

        //Readme
        _readmeButton = createButton(IconImage.DOCUMENT_PROPERTIES, "Readme");
        _readmeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewReadmeButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_readmeButton);

        //Grade
        _gradeButton = createButton(IconImage.FONT_X_GENERIC, "Grade");
        _gradeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                gradeAssignmentButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_gradeButton);

        //Print
        _printButton = createButton(IconImage.PRINTER, "Print");
        _printButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printStudentButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_printButton);
    }

    /**
     * Creates a button with an image on the left side and bolded text on the
     * right side.
     *
     * @param image
     * @param text
     * @return the button created
     */
    private JButton createButton(IconImage image, String text)
    {
        Icon icon = IconLoader.loadIcon(IconSize.s32x32, image);
        JButton button = new JButton("<html><b><font size=3>" + text +"</font></b></html>", icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);

        return button;
    }
    
    /**
     * Called when the run code button is clicked.
     */
    private void runCodeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.run(group);
        } catch (ActionException ex) {
            this.generateErrorView("run code", group, dp, ex);
        }
    }

    /**
     * Called when the grade assignment button is clicked.
     */
    private void gradeAssignmentButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        Allocator.getRubricManager().view(dp, group, false, this);
    }

    /**
     * Called when the print student code button is clicked.
     */
    private void printStudentButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.print(group);
        } catch (ActionException ex) {
            this.generateErrorView("print code", group, dp, ex);
        }
    }

    /**
     * Called when the run tester button is clicked.
     */
    private void runTesterButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.runTester(group);
        } catch (ActionException ex) {
            this.generateErrorView("run tester", group, dp, ex);
        }
    }

    /**
     * Called when the open code button is clicked.
     */
    private void openCodeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.open(group);
        } catch (ActionException ex) {
            this.generateErrorView("open code", group, dp, ex);
        }
    }

    /**
     * Called when the view readme button is clicked.
     */
    private void viewReadmeButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try {
            dp.viewReadme(group);
        } catch (ActionException ex) {
            this.generateErrorView("view README", group, dp, ex);
        }
    }

    /**
     * Called when the run demo button is clicked.
     */
    private void runDemoButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        try {
            dp.runDemo();
        } catch (ActionException ex) {
            this.generateErrorView("run code", null, dp, ex);
        }
    }

    /**
     * Called when the print all button is clicked.
     */
    private void printAllButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Collection<Group> groups = _groupList.getValues();
        try {
            dp.print(groups);
        } catch (ActionException ex) {
            this.generateErrorViewMultiple("print code", groups, dp, ex);
        }
    }

    /**
     * Called when the view deductions button is clicked.
     */
    private void viewDeductionsButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        try {
            dp.viewDeductionList();
        } catch (FileNotFoundException ex) {
            this.generateErrorView("view deductions list", null, dp, ex);
        }
    }

    private final ExecutorService _submitExecutor = Executors.newSingleThreadExecutor();
    /**
     * Called when the submit grading button is clicked.
     */
    private void submitGradingButtonActionPerformed()
    {
        final DistributablePart dp = _dpList.getSelectedValue();

        if(dp != null)
        {
            final SubmitDialog sd = new SubmitDialog(dp.getAssignment(), _groupList.getValues(),
                    Allocator.getConfigurationInfo().getSubmitOptions());

            if(sd.showDialog() == JOptionPane.OK_OPTION)
            {
                final Collection<Group> selected = sd.getSelectedGroups();

                //Enter grades into database
                if(sd.submitChecked())
                {
                    Runnable submitRunnable = new Runnable()
                    {
                        public void run()
                        {
                            Map<Group, Double> handinTotals = Allocator
                                    .getRubricManager().getPartScores(dp, selected);
                            for(Group group : handinTotals.keySet())
                            {
                                try
                                {
                                    Allocator.getDatabaseIO()
                                            .enterGrade(group, dp, handinTotals.get(group));
                                }
                                catch (SQLException ex)
                                {
                                    generateErrorView("enter grade", group, dp, ex);
                                }
                            }

                            //Retrieve updated database information to reflect submitted grades
                            loadAssignedGrading(false);
                        }
                    };

                    _submitExecutor.submit(submitRunnable);
                }

                //Generate GRD files if they will be emailed or printer
                if(sd.emailChecked() || sd.printChecked())
                {
                    Runnable convertRunnable = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Allocator.getRubricManager()
                                        .convertToGRD(dp.getHandin(), selected);
                            }
                            catch (RubricException ex)
                            {
                                generateErrorViewMultiple("enter grade", selected, dp, ex);
                            }
                        }
                    };
                    _submitExecutor.submit(convertRunnable);
                }

                //Print GRD files
                if(sd.printChecked())
                {
                    //This needs to be placed on the submission executor so that
                    //it is guaranteed run after the rubrics have been converted
                    //to GRD files
                    Runnable printRunnable = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Allocator.getGradingServices()
                                        .printGRDFiles(dp.getHandin(), selected);
                            }
                            catch (ServicesException ex)
                            {
                                new ErrorView(ex, "Could not print GRD files.");
                            }
                        }
                    };
                    _submitExecutor.submit(printRunnable);
                }

                //If groups/students are to be notified
                if(sd.notifyChecked())
                {
                    //This needs to be placed on the submission executor so that
                    //it is guaranteed run after the rubrics have been converted
                    //to GRD files
                    Runnable notifyRunnable = new Runnable()
                    {
                        public void run()
                        {
                           Allocator.getGradingServices().notifyStudents(
                                   dp.getHandin(), selected, sd.emailChecked());
                        }
                    };
                    _submitExecutor.submit(notifyRunnable);
                }
            }
        }
    }

    /**
     * Creates an ErrorView with the message "Could not <msgInsert> for group <group> on
     * part <dp> of assignment <dp.getAssignment()> and the given Exception.
     *
     * Use generateErrorViewMultiple when the failing operation was to be run on multiple groups.
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorView(String msgInsert, Group group, DistributablePart dp, Exception cause) {
        String message = "Could not " + msgInsert;
        
        if (group != null) {
            message += " for group " + group;
        }

        if (dp != null) {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
    }

    /**
     * Creates an ErrorView with the message "Could not <msgInsert> for groups <groups> on
     * part <dp> of assignment <dp.getAssignment()> and the given Exception.
     *
     * Use generateErrorView when the failing operation was to be run on a single group.
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorViewMultiple(String msgInsert, Collection<Group> groups, DistributablePart dp, Exception cause) {
        String message = "Could not " + msgInsert;

        if (!groups.isEmpty()) {
            message += " for groups " + groups;
        }

        if (dp != null) {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
    }


    /**
     * Ensures when the window closes the program terminates and that the
     * user's grading directory is removing.
     */
    private void initializeWindowCloseProperty()
    {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter()
        {
            @Override 
            public void windowClosing(WindowEvent e)
            {
                //remove user grading directory when frontend is closed
                try
                {
                    Allocator.getGradingServices().removeUserWorkspace();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Unable to remove your cakehat workspace directory.");
                }
            }
        });
    }

    /**
     * Initializes this frame's icon. Only visible on certain operating systems
     * and window managers.
     */
    private void initializeFrameIcon()
    {
        try
        {
            //randomly selects one of 5 icons
            BufferedImage icon = null;
            switch ((int) (Math.random() * 5))
            {
                case 0:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_DEVILISH);
                    break;
                case 1:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_ANGEL);
                    break;
                case 2:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_SURPRISE);
                    break;
                case 3:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_CRYING);
                    break;
                case 4:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_MONKEY);
                    break;
                case 5:
                    icon = IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.FACE_GLASSES);
                    break;
            }
            this.setIconImage(icon);
        }
        catch (IOException e) { }
    }

    /**
     * Called when a rubric is saved. This method should not be called except
     * as a listener to rubric save events.
     * <br/><br/>
     * Updates the user interface to reflect this.
     *
     * @param part
     * @param group
     */
    @Override
    public void rubricSaved(DistributablePart savedPart, Group savedGroup)
    {
        Double score = Allocator.getRubricManager().getPartScore(savedPart, savedGroup);
        boolean hasRubricScore = (score != 0);

        //Build a new map that is identical to the map currently referenced by
        //_assignedGroups except that it will reflect whether or not the saved
        //rubric has a score
        //This map is built by copying from _assignedGroups, NOT by reading
        //from the database
        //This map is built instead of directly mutating _assignedGroups so
        //that the showAssignedGradingChanges(...) method can be called which
        //will update the UI, if necessary, by comparing against _assignedGroups
        Map<DistributablePart, List<GroupGradedStatus>> updatedMap =
                new HashMap<DistributablePart, List<GroupGradedStatus>>();

        for(DistributablePart part : _assignedGroups.keySet())
        {
            List<GroupGradedStatus> statuses = _assignedGroups.get(part);

            //If this is the part, find and update the status
            if(part.equals(savedPart))
            {
                GroupGradedStatus currStatus = null;
                for(GroupGradedStatus status : statuses)
                {
                    if(status.getGroup().equals(savedGroup))
                    {
                        currStatus = status;
                        break;
                    }
                }

                //For this to have occurred, the rubric must have been open for
                //a group that is no longer assigned to the TA
                if(currStatus == null)
                {
                    return;
                }

                GroupGradedStatus newStatus = new GroupGradedStatus(savedGroup,
                        currStatus.isSubmitted(), hasRubricScore);

                int currStatusIndex = statuses.indexOf(currStatus);
                ArrayList<GroupGradedStatus> newStatuses = new ArrayList<GroupGradedStatus>(statuses);
                newStatuses.set(currStatusIndex, newStatus);
                statuses = newStatuses;
            }

            updatedMap.put(part, statuses);
        }

        this.showAssignedGradingChanges(updatedMap);
    }

    private final ExecutorService _loadAssignedGradingExecutor = Executors.newSingleThreadExecutor();
    /**
     * Retrieves from the database all of the groups that have been assigned.
     * Then determines if the grade has been submitted and if the rubric has
     * been edited.
     * <br/><br/>
     * This method may run the retrieval using a separate thread. Typically this
     * will be the desired behavior because of the time involved in retrieving
     * this information. Once it is complete, if UI changes are to be made then
     * those updates will be enqueued on the UI thread.
     * <br/><br/>
     * The likely reason this method would not be run with a separate thread is
     * if the calling code is already running on a non-UI thread.
     *
     * @param useSeparateThread 
     */
    private void loadAssignedGrading(boolean useSeparateThread)
    {
        Runnable loadRunnable = new Runnable()
        {
            public void run()
            {
                Map<DistributablePart, List<GroupGradedStatus>> newMap;
                try
                {
                    newMap = new HashMap<DistributablePart, List<GroupGradedStatus>>();

                    Set<DistributablePart> parts = Allocator.getDatabaseIO().getDPsWithAssignedStudents(USER);
                    for(DistributablePart part : parts)
                    {
                        Collection<Group> groups = Allocator.getDatabaseIO().getGroupsAssigned(part, USER);
                        Map<Group, Double> submittedScores = Allocator.getDatabaseIO()
                                .getPartScoresForGroups(part, groups);
                        Map<Group, Double> rubricScores = Allocator.getRubricManager()
                                .getPartScores(part, groups);

                        List<GroupGradedStatus> statuses = new Vector<GroupGradedStatus>();
                        newMap.put(part, statuses);

                        for(Group group : groups)
                        {
                            GroupGradedStatus status = new GroupGradedStatus(group,
                                    submittedScores.containsKey(group),
                                    rubricScores.containsKey(group) &&
                                    rubricScores.get(group) != 0);
                            statuses.add(status);
                        }
                    }
                }
                catch (CakeHatDBIOException ex)
                {
                    new ErrorView(ex, "Unable to retrieve information on who you have " +
                            "been assigned to grade");
                    return;
                }
                catch (SQLException ex)
                {
                    new ErrorView(ex, "Unable to retrieve information on who you have " +
                            "been assigned to grade");
                    return;
                }

                showAssignedGradingChanges(newMap);
            }
        };

        if(useSeparateThread)
        {
            _loadAssignedGradingExecutor.submit(loadRunnable);
        }
        else
        {
            loadRunnable.run();
        }
    }

    /**
     * Determines if there are any differences between
     * <code>newAssignedGroups</code> and {@link #_assignedGroups}. If there
     * are changes, then visually updates to reflect the changes. In either
     * case, updates {@link #_assignedGroups} to reference the data referenced
     * by <code>newAssignedGroups</code>.
     *
     * @param newAssignedGroups
     */
    private void showAssignedGradingChanges(final Map<DistributablePart, List<GroupGradedStatus>> newAssignedGroups)
    {
        //Determine if any data has changed
        boolean dpChanged = false;
        boolean groupsChangedForSelectedDP = false;

        //If currently no data has been loaded
        if(_assignedGroups == null)
        {
            dpChanged = true;
            groupsChangedForSelectedDP = true;
        }
        else
        {
            //If groups for a new part has been assigned (or all groups
            //for an existing part were removed)
            dpChanged = !Allocator.getGeneralUtilities().containSameElements(
                    newAssignedGroups.keySet(), _assignedGroups.keySet());

            //If the group or the status of a group has changed
            DistributablePart selected = _dpList.getSelectedValue();
            if(selected != null)
            {
                List<GroupGradedStatus> currGroups = _assignedGroups.get(selected);
                List<GroupGradedStatus> newGroups = newAssignedGroups.get(selected);
                groupsChangedForSelectedDP = !Allocator.getGeneralUtilities()
                        .containSameElements(currGroups, newGroups);
            }
        }

        //If anything has changed, refresh it on the UI thread
        if(dpChanged || groupsChangedForSelectedDP)
        {
            //Store as final variables so that they may be referenced in the Runnable
            final boolean updateDPList = dpChanged;
            final boolean updateGrouplist = groupsChangedForSelectedDP;

            Runnable uiRunnable = new Runnable()
            {
                public void run()
                {
                    _assignedGroups = newAssignedGroups;

                    if(updateDPList)
                    {
                        List<DistributablePart> sortedParts =
                                new ArrayList<DistributablePart>(_assignedGroups.keySet());
                        Collections.sort(sortedParts);
                        _dpList.setListData(sortedParts, true);

                        if(_dpList.isSelectionEmpty())
                        {
                            _dpList.selectFirst();
                        }
                    }

                    if(updateGrouplist)
                    {
                        DistributablePart currPart = _dpList.getSelectedValue();
                        List<GroupGradedStatus> statuses = new ArrayList(_assignedGroups.get(currPart));
                        Collections.sort(statuses);
                        List<Group> groups = new ArrayList<Group>();
                        for(GroupGradedStatus status : statuses)
                        {
                            groups.add(status.getGroup());
                        }
                        _groupList.setStringConverter(new GroupConverter(currPart));
                        _groupList.setListData(groups, true);

                        if(_groupList.isSelectionEmpty())
                        {
                            _groupList.selectFirst();
                        }

                        //If all groups are submitted, then the part list needs
                        //to be updated to reflect this
                        _dpList.refreshList();
                    }
                }
            };

            EventQueue.invokeLater(uiRunnable);
        }
        else
        {
            _assignedGroups = newAssignedGroups;
        }
    }

    private static class GroupGradedStatus implements Comparable<GroupGradedStatus>
    {
        private final Group _group;
        private final boolean _submitted;
        private final boolean _hasRubricScore;

        public GroupGradedStatus(Group group, boolean submitted, boolean hasRubricScore)
        {
            _group = group;
            _submitted = submitted;
            _hasRubricScore = hasRubricScore;
        }

        public Group getGroup()
        {
            return _group;
        }

        public boolean isSubmitted()
        {
            return _submitted;
        }

        public boolean hasRubricScore()
        {
            return _hasRubricScore;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean equals = false;
            if(obj instanceof GroupGradedStatus)
            {
                GroupGradedStatus other = (GroupGradedStatus) obj;

                equals = other._group.equals(_group) &&
                         other._submitted == _submitted &&
                         other._hasRubricScore == _hasRubricScore;
            }

            return equals;
        }

        @Override
        public int compareTo(GroupGradedStatus other)
        {
            //Comparison on group names breaks ties
            int groupComp = this.getGroup().getName().compareTo(other.getGroup().getName());

            //Heirarchy
            // - submitted
            // - rubric score
            // - nothing
            if(this.isSubmitted() && other.isSubmitted())
            {
                return groupComp;
            }
            else if(this.isSubmitted())
            {
                return -1;
            }
            else if(other.isSubmitted())
            {
                return 1;
            }
            else if(this.hasRubricScore() && other.hasRubricScore())
            {
                return groupComp;
            }
            else if(this.hasRubricScore())
            {
                return -1;
            }
            else if(other.hasRubricScore())
            {
                return 1;
            }
            else
            {
                return groupComp;
            }
        }
    }

    private class GroupConverter implements StringConverter<Group>
    {
        private final DistributablePart _part;

        public GroupConverter(DistributablePart part)
        {
            _part = part;
        }

        public String convertToString(Group group)
        {
            //Determine status of this group
            boolean hasRubricScore = false;
            boolean submitted = false;
            List<GroupGradedStatus> statuses = _assignedGroups.get(_part);
            for(GroupGradedStatus status : statuses)
            {
                if(status.getGroup().equals(group))
                {
                    submitted = status.isSubmitted();
                    hasRubricScore = status.hasRubricScore();
                }
            }

            //Build representation
            String pre = "";
            String post = "";
            if(submitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else if(hasRubricScore)
            {
                pre = "<strong><font color=#686868>";
                post = "</font><strong>";
            }
            else
            {
                pre = "<strong>";
                post = "<strong>";
            }

            String representation = "<html>" + pre + group.getName() + post + "</html>";

            return representation;
        }
    }

    private class DPConverter implements StringConverter<DistributablePart>
    {
        public String convertToString(DistributablePart part)
        {
            //Because all parts shown have at least one group, this will never
            //be vacuously true
            boolean allGroupsSubmitted = true;
            List<GroupGradedStatus> statuses = _assignedGroups.get(part);
            for(GroupGradedStatus status : statuses)
            {
                if(!status.isSubmitted())
                {
                    allGroupsSubmitted = false;
                    break;
                }
            }

            //Build representation
            String pre = "";
            String post = "";

            if(allGroupsSubmitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else
            {
                pre = "<strong>";
                post = "</strong>";
            }

            String name = part.getAssignment().getName() + ": "  + part.getName();
            String representation = "<html>" + pre + name + post + "</html>";

            return representation;
        }
    }
}