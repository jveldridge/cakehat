package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.CakehatAboutBox;
import cakehat.database.assignment.ActionException;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.MissingHandinException;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.database.TA;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import cakehat.views.shared.gradingsheet.GradingSheet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.compress.archivers.ArchiveException;
import support.ui.AlphaJPanel;
import support.ui.DescriptionProvider;
import support.ui.GenericJList;
import support.ui.ModalDialog;
import support.ui.PartialDescriptionProvider;

public class GraderView extends JFrame
{
    private final TA USER = Allocator.getUserServices().getUser();

    private final boolean _isSSH;
    private GenericJList<Part> _partList;
    private GenericJList<Group> _groupList;
    private JLabel _groupListLabel;
    private CurrentlyGradingLabel _currentlyGradingLabel;
    private JLabel _selectedGroupCommandsLabel;
    private JButton _demoButton, _gradingGuideButton, _printAllButton,
                    _submitGradingButton, _readmeButton, _openButton,
                    _testButton, _printButton, _gradeButton,
                    _runButton;
    private JButton[] _allButtons, _groupButtons;
    
    private final Map<Part, Map<Group, GroupStatus>> _assignedGroups = new HashMap<Part, Map<Group, GroupStatus>>();
    
    public static void launch(final boolean isSSH)
    {   
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {   
                new GraderView(isSSH).setVisible(true);
            }
        });
    }
    
    private GraderView(boolean isSSH)
    {
        //Frame title
        super("cakehat" + (isSSH ? " [ssh]" : ""));

        _isSSH = isSSH;

        //Initialize GUI components necessary to show database information
        this.initializeComponents();

        //Initialize more GUI components
        this.initializeMenuBar();

        //Set up logical grouping of buttons
        this.createButtonGroups();

        //Set up close property
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        //Retrieve the database information
        this.loadAssignedGrading();

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
     * Called when a different Part is selected from the part list to update other GUI components
     */
    private void updatePartList()
    {
        Part part = _partList.getSelectedValue();

        if(part == null)
        {
            _groupList.clearListData();
        }
        else
        {
            //Update visual references to student/group appropriately depending on if the assignment the part belongs to
            //is a group assignment
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
     * Populates the group list with the Groups that the TA has been assigned to grade (as recorded in the database) for
     * the selected Part.
     */
    private void populateGroupsList()
    {
        Part selected = _partList.getSelectedValue();
        List<GroupStatus> statuses = new ArrayList<GroupStatus>(_assignedGroups.get(selected).values());
        Collections.sort(statuses);
        List<Group> groups = new ArrayList<Group>();
        for(GroupStatus status : statuses)
        {
            groups.add(status.getGroup());
        }
        _groupList.setDescriptionProvider(new GroupDescriptionProvider(selected));
        _groupList.setListData(groups, true);
        if(_groupList.isSelectionEmpty())
        {
            _groupList.selectFirst();
        }

        _currentlyGradingLabel.update(_groupList.getSelectedValue());
    }
    
    /**
     * Enable or disable buttons based on the part selected.
     */
    private void updateButtonStates()
    {
        Part part = _partList.getSelectedValue();

        //If there is no part selected, disable all of the buttons
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
        _gradingGuideButton.setEnabled(part.hasGradingGuide());
        _submitGradingButton.setEnabled(true);
        
        //Group buttons
        Group selectedGroup = _groupList.getSelectedValue();
        _gradeButton.setEnabled(selectedGroup != null);
        boolean selectedGroupHasHandin = false;
        boolean anyGroupsHaveHandins = false;
        boolean selectedGroupHasReadme = false;

        //Determine if this group and any groups currently displayed have digital handins
        if(part.getGradableEvent().hasDigitalHandins())
        {
            try
            {
                for(Group group : _groupList.getListData())
                {
                    boolean hasHandin = part.getGradableEvent().hasDigitalHandin(group);
                    anyGroupsHaveHandins = anyGroupsHaveHandins || hasHandin;
                    if(group == selectedGroup)
                    {
                        selectedGroupHasHandin = hasHandin;
                    }
                }
            }
            catch(IOException e)
            {
                new ErrorView(e, "Unable to determine if a digtial handin exists");
            }

            if(selectedGroupHasHandin)
            {
                try
                {
                    selectedGroupHasReadme = part.hasReadme(selectedGroup);
                }
                catch(ArchiveException ex)
                {
                    new ErrorView(ex, "Could not determine if " + selectedGroup + " has a readme");
                }
                catch(ActionException ex)
                {
                    new ErrorView(ex, "Could not determine if " + selectedGroup + " has a readme");
                }
                catch(MissingHandinException ex)
                {
                    this.notifyHandinMissing(ex);
                }
            }
        }
        
        //Enable Print All if any groups for this part have handins and the part has print
        _printAllButton.setEnabled(anyGroupsHaveHandins && part.hasPrint());
        
        //Group buttons
        _testButton.setEnabled(selectedGroupHasHandin && part.hasTest());
        _runButton.setEnabled(selectedGroupHasHandin && part.hasRun());
        _openButton.setEnabled(selectedGroupHasHandin && part.hasOpen());
        _printButton.setEnabled(selectedGroupHasHandin && part.hasPrint());
        _readmeButton.setEnabled(selectedGroupHasReadme);
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

        //Part list
        Dimension partListPanelSize = new Dimension(190, contentHeight);
        Dimension partLabelSize = new Dimension(partListPanelSize.width, 13);
        Dimension partListSize = new Dimension(partListPanelSize.width, contentHeight - partLabelSize.height);

        JPanel partPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        partPanel.setPreferredSize(partListPanelSize);
        JLabel partLabel = new JLabel("<html><b>Assignment Part</b></html>");
        partLabel.setPreferredSize(partLabelSize);

        _partList = new GenericJList<Part>(Collections.<Part>emptyList(), new PartDescriptionProvider());
        _partList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _partList.usePlainFont();
        _partList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    updatePartList();
                }
            }
        });
        partPanel.add(partLabel);
        JScrollPane partPane = new JScrollPane(_partList);
        partPane.setPreferredSize(partListSize);
        partPanel.add(partPane);
        contentPanel.add(partPanel);

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
                    Group selectedGroup = _groupList.getSelectedValue();

                    //Update the currently grading label and buttons
                    _currentlyGradingLabel.update(selectedGroup);
                    updateButtonStates();
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
                    _partList.grabFocus();
                }
            }
        });
        //When the right key is pressed, switch focus to the group list
        _partList.addKeyListener(new KeyListener()
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
        int availableHeight = contentHeight - currentlyGradingSize.height - 2 * labelHeight - 3 * buttonGap;
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
        Dimension studentButtonsSize = new Dimension(groupCommandsSize.width, groupCommandsSize.height - labelHeight);
        JPanel groupButtonsPanel = new JPanel(new GridLayout(3, 2, buttonGap, buttonGap));
        groupButtonsPanel.setPreferredSize(studentButtonsSize);
        this.initializeGroupCommandButtons(groupButtonsPanel);
        groupCommandsPanel.add(groupButtonsPanel);
        controlPanel.add(groupCommandsPanel);

        //Set content panel size based on the content in it
        int contentWidth = partListPanelSize.width + groupListPanelSize.width + controlPanelSize.width + 2 * gapSpace;
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
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        //Refresh item
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        refreshItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                loadAssignedGrading();
            }
        });
        fileMenu.add(refreshItem);

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
                        JPanel panel = showModalContentInFrame();

                        // Attempt to display the blacklist panel, if the necessary database information cannot be
                        // retrieved succesfully, do not show the blacklist panel
                        try
                        {
                            panel.add(new BlacklistPanel(panel.getPreferredSize(), panel.getBackground()));
                        }
                        catch(SQLException e)
                        {
                            new ErrorView(e, "Unable to launch modify blacklist view");
                            showNormalContentInFrame();
                        }
                        catch(ServicesException e)
                        {
                            new ErrorView(e, "Unable to launch modify blacklist view");
                            showNormalContentInFrame();
                        }
                    }
                });
            }
        });
        fileMenu.add(blacklistItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    Allocator.getGradingServices().makeDatabaseBackup();
                }
                catch(ServicesException ex)
                {
                    new ErrorView(ex, "Could not make database backup.");
                }
                System.exit(0);
            }
        });
        fileMenu.add(quitItem);

        //Help menu
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        //About
        JMenuItem aboutItem = new JMenuItem("About cakehat");
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(GraderView.this);
            }
        });
        helpMenu.add(aboutItem);
    }

    /**
     * Hides the menu and the components of the content pane. Visually it appears as if they are disabled. A panel is
     * returned to put the modal content into.
     * <br/><br/>
     * The disabled appearance does not look as good over SSH because when running over SSH transparency is very
     * expensive, so transparency is not used when running over SSH.
     *
     * @return panel to put content into
     *
     * @see #showNormalContentInFrame()
     */
    private JPanel showModalContentInFrame()
    {
        // Padding used around edges to prevent the content from being flush with the frame
        int paddingSize = 15;

        // Disabling a component in Swing such that all of its subcomponents are disabled is an absurdly non-trivial
        // task. To fake this, take the menu bar and content pane and draw its appearance to a BufferedImage, then hide
        // the actual menu bar and content pane and draw the BufferedImage.
        Container contentPane = this.getContentPane();
        Rectangle contentPaneBounds = contentPane.getBounds();
        final BufferedImage disableImage = new BufferedImage(contentPaneBounds.width,
                contentPaneBounds.height + contentPaneBounds.y, BufferedImage.TYPE_INT_ARGB);
        
        // Draw the menu bar and content pane
        Graphics2D imageGraphics = disableImage.createGraphics();
        this.getJMenuBar().paint(imageGraphics);
        imageGraphics.translate(0, contentPaneBounds.y);
        contentPane.paint(imageGraphics);
        imageGraphics.translate(0, -contentPaneBounds.y);

        // Hide the menu bar and the components of the content pane
        for(Component component : contentPane.getComponents())
        {
            component.setVisible(false);
        }
        this.getJMenuBar().setVisible(false);

        // Turn on anti-aliasing so rounded corners are not jagged
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a semi-transparent gray over everything
        imageGraphics.setColor(new Color(192, 192, 192, 200));
        imageGraphics.fillRect(0, 0, disableImage.getWidth(), disableImage.getHeight());

        // Draw a rounded rectangle with the top part off the top of the screen
        int cornerRadius = 40;
        int shadingPadding = 10;
        if(_isSSH)
        {
            imageGraphics.setColor(new Color(144, 144, 144));
        }
        else
        {
            imageGraphics.setColor(new Color(128, 128, 128, 200));
        }
        imageGraphics.fillRoundRect(paddingSize - shadingPadding,
                paddingSize - shadingPadding - cornerRadius,
                disableImage.getWidth() - (2 * paddingSize) + (2 * shadingPadding),
                disableImage.getHeight() - (2 * paddingSize) + (2 * shadingPadding) + cornerRadius,
                cornerRadius, cornerRadius);

        // Add a panel to the glass pane, make it visible
        JPanel disablePanel = new JPanel(new BorderLayout(0, 0))
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                g.drawImage(disableImage, 0, 0, this);
            }
        };

        disablePanel.setPreferredSize(new Dimension(disableImage.getWidth(), disableImage.getHeight()));
        this.getContentPane().add(disablePanel);

        // Center an overlay panel in the disablePanel that will hold the content
        Dimension disablePanelSize = disablePanel.getPreferredSize();

        disablePanel.add(Box.createRigidArea(new Dimension(disablePanelSize.width, paddingSize)), BorderLayout.NORTH);
        disablePanel.add(Box.createRigidArea(new Dimension(disablePanelSize.width, paddingSize)), BorderLayout.SOUTH);
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height - 2 * paddingSize)),
                BorderLayout.WEST);
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height - 2 * paddingSize)),
                BorderLayout.EAST);

        AlphaJPanel overlayPanel = new AlphaJPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        Dimension overlayPanelSize = new Dimension(disablePanelSize.width - 2 * paddingSize, disablePanelSize.height - 2 * paddingSize);
        overlayPanel.setPreferredSize(overlayPanelSize);
        disablePanel.add(overlayPanel, BorderLayout.CENTER);

        if(_isSSH)
        {
            overlayPanel.setBackground(new Color(144, 144, 144));
        }
        else
        {
            overlayPanel.setBackground(new Color(0, 0, 0, 0));
        }

        // Create the panel that will host the content provided from elsewhere,
        // and place a button to "Close" the view, which will re-enable the frame
        int closeVerticalGap = 5;
        int closeButtonWidth = 120;
        int closeButtonHeight = 25;

        AlphaJPanel overlayContentPanel = new AlphaJPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        overlayContentPanel.setPreferredSize(new Dimension(overlayPanelSize.width,
                overlayPanelSize.height - closeVerticalGap - closeButtonHeight));
        overlayContentPanel.setBackground(overlayPanel.getBackground());

        overlayPanel.add(overlayContentPanel);

        overlayPanel.add(Box.createRigidArea(new Dimension(overlayPanelSize.width, closeVerticalGap)));

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setPreferredSize(new Dimension(overlayPanelSize.width, closeButtonHeight));
        closePanel.setBackground(overlayPanel.getBackground());
        overlayPanel.add(closePanel);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GraderView.this.showNormalContentInFrame();
            }
        });
        closeButton.setPreferredSize(new Dimension(closeButtonWidth, closeButtonHeight));
        closePanel.add(closeButton);

        return overlayContentPanel;
    }

    /**
     * Removes the modally displayed content in the frame, and restores the frame to showing its normal content.
     *
     * @see #showModalContentInFrame()
     */
    private void showNormalContentInFrame()
    {
        // Show the menu bar
        this.getJMenuBar().setVisible(true);

        // Remove the components visible in the content pane
        // Show all of the hidden components as that is the normal content
        Component[] children = this.getContentPane().getComponents();
        for(Component child : children)
        {
            if(child.isVisible())
            {
                this.getContentPane().remove(child);
            }
            else
            {
                child.setVisible(true);
            }
        }
    }

    /**
     * Creates the buttons corresponding to the part.
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
                demoButtonActionPerformed();
            }

        });
        generalButtonsPanel.add(_demoButton);

        //Submit Grading
        _submitGradingButton = createButton(IconImage.DOCUMENT_SAVE, "Submit Grading");
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
                gradingGuideButtonActionPerformed();
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
                runButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_runButton);

        //Test
        _testButton = createButton(IconImage.UTILITIES_SYSTEM_MONITOR, "Test");
        _testButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                testButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_testButton);

        //Open
        _openButton = createButton(IconImage.DOCUMENT_OPEN, "Open");
        _openButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                openButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_openButton);

        //Readme
        _readmeButton = createButton(IconImage.DOCUMENT_PROPERTIES, "Readme");
        _readmeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                readmeButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_readmeButton);

        //Grade
        _gradeButton = createButton(IconImage.FONT_X_GENERIC, "Grade");
        _gradeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                gradeButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_gradeButton);

        //Print
        _printButton = createButton(IconImage.PRINTER, "Print");
        _printButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printButtonActionPerformed();
            }

        });
        groupButtonsPanel.add(_printButton);
    }
    
    /**
     * Called when the submit grading button is clicked.
     */
    private void submitGradingButtonActionPerformed()
    {                
        // Invoke later so that the button has time to unclick
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JPanel disablePanel = showModalContentInFrame();

                try
                {
                    SubmitPanel submitPanel = new SubmitPanel(disablePanel.getPreferredSize(),
                            disablePanel.getBackground(),
                            GraderView.this,
                            _partList.getSelectedValue(),
                            new HashSet<Group>(_groupList.getListData()));
                    disablePanel.add(submitPanel);
                }
                catch(ServicesException e)
                {
                    showNormalContentInFrame();
                    new ErrorView(e, "Unable to show submit grading view due to database issues");
                }
            }
        });
    }    
    
    /**
     * Creates an {@link ErrorView} with the message "Could not {@code msgInsert} for group {@code group} on part
     * {@link Part#getFullDisplayName()}" and the given {@code cause}.
     *
     * @param msgInsert
     * @param group
     * @param part
     * @param cause
     */
    private void generateErrorView(String msgInsert, Group group, Part part, Exception cause)
    {
        String message = "Could not " + msgInsert;

        if(group != null)
        {
            message += " for group " + group;
        }

        if(part != null)
        {
            message += " on part " + part.getFullDisplayName();
        }

        message += ".";

        new ErrorView(cause, message);
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
     * Called when the run button is clicked.
     */
    private void runButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            part.run(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("run", group, part, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
        }
    }

    /**
     * Called when the grade assignment button is clicked.
     */
    private void gradeButtonActionPerformed()
    {
        final Part part = _partList.getSelectedValue();
        final Group group = _groupList.getSelectedValue();
        GradingSheet gradingSheet = Allocator.getGradingSheetManager().showFrame(part, group, false, false);
        gradingSheet.addGradingSheetListener(new GradingSheet.GradingSheetListener()
        {
            @Override
            public void earnedChanged(double prevEarned, double currEarned) { }

            @Override
            public void saveChanged(boolean hasUnsavedChanges)
            {
                Map<Group, GroupStatus> statuses = _assignedGroups.get(part);
                
                //Could be null if the grader had saved a grading sheet that had been reassigned and they no longer had
                //any assigned groups in that part and then they refreshed the grader interface
                if(statuses != null)
                {
                    //Check if null due to possible reassignment
                    GroupStatus status = statuses.get(group);
                    if(status != null)
                    {
                        status.setModified(true);
                        
                        //If this part is currently being displayed, visually update the group list to reflect the
                        //grading sheet modification
                        if(_partList.getSelectedValue() == part)
                        {
                            populateGroupsList();
                        }
                    }
                }
            }
        });
    }

    /**
     * Called when the print button is clicked.
     */
    private void printButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            part.print(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("print", group, part, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
        }
    }

    /**
     * Called when the test button is clicked.
     */
    private void testButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            part.test(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("test", group, part, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
        }
    }

    /**
     * Called when the open button is clicked.
     */
    private void openButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            part.open(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("open", group, part, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
        }
    }

    /**
     * Called when the readme button is clicked.
     */
    private void readmeButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            part.viewReadme(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("view readme", group, part, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
        }
    }

    /**
     * Called when the demo button is clicked.
     */
    private void demoButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        try
        {
            part.demo();
        }
        catch(ActionException ex)
        {
            this.generateErrorView("demo", null, part, ex);
        }
    }

    /**
     * Called when the print all button is clicked.
     */
    private void printAllButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        Collection<Group> groups = _groupList.getListData();
        try
        {
            part.print(groups);
        }
        catch(ActionException ex)
        {
            this.generateErrorViewMultiple("print", groups, part, ex);
        }
    }

    /**
     * Called when the grading guide button is clicked.
     */
    private void gradingGuideButtonActionPerformed()
    {
        Part part = _partList.getSelectedValue();
        try
        {
            part.viewGradingGuide();
        }
        catch (FileNotFoundException ex)
        {
            this.generateErrorView("view grading guide", null, part, ex);
        }
    }

    private void notifyHandinMissing(MissingHandinException ex)
    {
        ModalDialog.showMessage("Digital Handin Missing",
                "The handin for " + ex.getGroup().getName() + " can no longer be found");
        ex.getPart().getGradableEvent().clearDigitalHandinCache();
        updateButtonStates();
    }

    /**
     * Creates an {@link ErrorView} with the message "Could not {@code msgInsert} for groups {@code groups} on part
     * {@link Part#getFullDisplayName()}" and the given {@code cause}.
     *
     * @param msgInsert
     * @param groups
     * @param part
     * @param cause
     */
    private void generateErrorViewMultiple(String msgInsert, Collection<Group> groups, Part part, Exception cause)
    {
        String message = "Could not " + msgInsert;

        if(!groups.isEmpty())
        {
            message += " for groups " + groups;
        }

        if(part != null)
        {
            message += " on part " + part.getFullDisplayName();
        }

        message += ".";

        new ErrorView(cause, message);
    }
    
    final void loadAssignedGrading()
    {
        _assignedGroups.clear();
        try
        {
            Set<Part> parts = Allocator.getDataServices().getPartsWithAssignedGroups(USER);
            for(Part part : parts)
            {
                Set<Group> groups = new HashSet<Group>(Allocator.getDataServices().getAssignedGroups(part, USER));
                Map<Group, PartGrade> grades = Allocator.getDataServices().getEarned(groups, part);
                
                _assignedGroups.put(part, new HashMap<Group, GroupStatus>());
                for(Group group : groups)
                {
                    _assignedGroups.get(part).put(group, new GroupStatus(group, grades.get(group)));
                }
            }
        }
        catch(ServicesException e)
        {
            _assignedGroups.clear();
            new ErrorView(e, "Unable to load assigned grading");
        }
        
        //Parts list
        List<Part> sortedParts = new ArrayList<Part>(_assignedGroups.keySet());
        Collections.sort(sortedParts);
        _partList.setListData(sortedParts, true);
        if(_partList.isSelectionEmpty())
        {
            _partList.selectFirst();
        }

        //If all groups are submitted, then the part list needs to be updated to reflect this
        _partList.refreshList();
        _groupList.refreshList();
    }
    
    /**
     * The status of a group, which is made up whether it has been submitted or modified.
     */
    private static class GroupStatus implements Comparable<GroupStatus>
    {
        private final Group _group;
        private boolean _submitted;
        private boolean _modified;

        public GroupStatus(Group group, PartGrade partGrade)
        {
            _group = group;
            _submitted = (partGrade != null && partGrade.isSubmitted());
            _modified = (partGrade != null);
        }

        public Group getGroup()
        {
            return _group;
        }

        public boolean isSubmitted()
        {
            return _submitted;
        }

        public boolean isModified()
        {
            return _modified;
        }
        
        public void setSubmitted(boolean submitted)
        {
            _submitted = submitted;
        }
        
        public void setModified(boolean modified)
        {
            _modified = modified;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean equals = false;
            if(obj instanceof GroupStatus)
            {
                GroupStatus other = (GroupStatus) obj;
                equals = other._group.equals(_group) && other._modified == _modified && other._submitted == _submitted;
            }
            
            return equals;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 61 * hash + (this._group != null ? this._group.hashCode() : 0);
            hash = 61 * hash + (this._submitted ? 1 : 0);
            hash = 61 * hash + (this._modified ? 1 : 0);
            
            return hash;
        }

        @Override
        public int compareTo(GroupStatus other)
        {
            //Comparison on group names breaks ties
            int groupComp = this.getGroup().getName().compareTo(other.getGroup().getName());

            //Heirarchy
            // - submitted
            // - modified
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
            else if(this.isModified() && other.isModified())
            {
                return groupComp;
            }
            else if(this.isModified())
            {
                return -1;
            }
            else if(other.isModified())
            {
                return 1;
            }
            else
            {
                return groupComp;
            }
        }
    }

    private final class GroupDescriptionProvider implements DescriptionProvider<Group>
    {
        private final Part _part;

        public GroupDescriptionProvider(Part part)
        {
            _part = part;
        }

        @Override
        public String getDisplayText(Group group)
        {
            boolean modified = false;
            boolean submitted = false;
            
            GroupStatus groupStatus = _assignedGroups.get(_part).get(group);  
            if(groupStatus != null)
            {
                modified = groupStatus.isModified();
                submitted = groupStatus.isSubmitted();
            }

            //Build representation
            String pre;
            String post;
            if(submitted)
            {
                pre = "<font color=green>✓</font> <font color=#686868>";
                post = "</font>";
            }
            else if(modified)
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

        @Override
        public String getToolTipText(Group group)
        {
            String toolTip = "";
            List<Student> students = new ArrayList<Student>(group.getMembers());
            Collections.sort(students);
            for(int i = 0; i < students.size(); i++)
            {
                toolTip += students.get(i).getName();
                if(i != students.size() - 1)
                {
                    toolTip += ", ";
                }
            }
            
            return toolTip;
        }
    }

    private class PartDescriptionProvider extends PartialDescriptionProvider<Part>
    {
        @Override
        public String getDisplayText(Part part)
        {
            //Because all parts shown have at least one group, this will never be vacuously true
            boolean allGroupsSubmitted = true;
            for(GroupStatus status : _assignedGroups.get(part).values())
            {
                if(!status.isSubmitted())
                {
                    allGroupsSubmitted = false;
                    break;
                }
            }

            //Build representation
            String pre;
            String post;

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
            
            //Hide the name of the gradable event if there are not other parts that belong to this assignment
            int numParts = 0;
            for(GradableEvent ge : part.getAssignment())
            {
                numParts += ge.getParts().size();
            }
            String name;
            if(numParts == 1)
            {
                name = part.getAssignment().getName() + " - "  + part.getName();
            }
            else
            {
                name = part.getFullDisplayName();
            }
            String representation = "<html>" + pre + name + post + "</html>";

            return representation;
        }
    }
    
    /**
     * Label that displays the currently selected student.
     */
    private static class CurrentlyGradingLabel extends JLabel
    {
        private final static String BEGIN ="<html><b>Currently Grading</b><br/>",
                                    END = "</html>",
                                    DEFAULT = "None";

        public CurrentlyGradingLabel()
        {
            super(BEGIN + DEFAULT + END);

            this.setFont(this.getFont().deriveFont(Font.PLAIN));
        }

        public void update(Group group)
        {
            if(group == null)
            {
                this.setText(BEGIN + DEFAULT + END);
            }
            else
            {
                this.setText(BEGIN + getGroupText(group) + END);
            }
        }

        private String getGroupText(Group group)
        {
            String text;

            //If group assignment, show group name and members
            if(group.getAssignment().hasGroups())
            {
                text = group.getName() + " " + group.getMembers();
            }
            //If not a group assignment, show name of student
            else if(!group.getMembers().isEmpty())
            {
                text = group.getOnlyMember().getLogin();
            }
            //A non-group assignment with no student, this situation should not arise
            else
            {
                text = "Unknown";
            }

            return text;
        }
    }
}