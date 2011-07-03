package cakehat.views.frontend;

import cakehat.Allocator;
import cakehat.CakehatAboutBox;
import cakehat.config.TA;
import cakehat.config.handin.ActionException;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.MissingHandinException;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.Group;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.rubric.RubricSaveListener;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import support.ui.AlphaJPanel;
import support.ui.GenericJList;
import support.ui.StringConverter;
import support.utils.posix.NativeException;

/**
 * A frontend view to be used by TAs that are grading.
 * <br/><br/>
 * In order to have a very responsive user interface, this class uses a non-UI
 * thread to load information. Whenever these changes lead to UI changes, the
 * code that will do this is placed on the AWT Event Queue. In particular,
 * {@link #_assignedGroups} can be loaded using a non-UI thread. This is done
 * via the {@link #loadAssignedGrading(boolean)} method.
 *
 * @author jak2
 */
public class FrontendView extends JFrame implements RubricSaveListener
{
    private final TA USER = Allocator.getUserServices().getUser();

    private final boolean _isSSH;
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
    private Map<DistributablePart, List<GroupStatus>> _assignedGroups;

    /**
     * Launches a visible instance of the <code>FrontendView</code> if the user
     * is a TA as specified by the cakehat configuration. Otherwise the user
     * will be informed they are not an authorized user.
     */
    public static void launch()
    {
        if(Allocator.getUserServices().isUserTA())
        {
            boolean isSSH = false;
            try
            {
                isSSH = Allocator.getUserUtilities().isUserRemotelyConnected();
            }
            catch(NativeException e){}

            new FrontendView(isSSH);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "You [" +
                                         Allocator.getUserUtilities().getUserLogin() +
                                         "] are not an authorized user.");
            System.exit(0);
        }
    }

    private FrontendView(boolean isSSH)
    {
        //Frame title
        super("cakehat" + (isSSH ? " [ssh]" : ""));

        _isSSH = isSSH;

        //Create the directory to work in
        try
        {
            Allocator.getGradingServices().makeUserWorkspace();
        }
        catch (ServicesException ex)
        {
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
        this.initializeMenuBar();

        //Set up logical grouping of buttons
        this.createButtonGroups();

        //Set up close property
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
     * Ensures when the window closes the program terminates and that the
     * user's grading directory is removed.
     */
    private void initializeWindowCloseProperty()
    {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Allocator.getGradingServices().removeUserWorkspace();
            }
        });
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
            _groupList.clearListData();
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
        List<GroupStatus> statuses = new ArrayList(_assignedGroups.get(selected));
        Collections.sort(statuses);
        List<Group> groups = new ArrayList<Group>();
        for(GroupStatus status : statuses)
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
        _gradingGuideButton.setEnabled(part.hasGradingGuide());
        _submitGradingButton.setEnabled(part.hasRubricTemplate());

        //Determine if this group has a handin, if there is no selected group
        //then it will be treated as not having a handin
        Group selectedGroup = _groupList.getSelectedValue();
        boolean groupHasHandin = false;
        boolean anyGroupsHaveHandins = false;

        //The statuses should not be null, but if there were exceptions thrown
        //when retrieving the info from the database it could be
        List<GroupStatus> statuses = _assignedGroups.get(part);
        if(statuses != null)
        {
            for(GroupStatus status : statuses)
            {
                if(status.getGroup() == selectedGroup)
                {
                    groupHasHandin = status.hasHandin();
                }

                if(status.hasHandin())
                {
                    anyGroupsHaveHandins = true;
                }
            }
        }

        //Enable Print All if any groups have handins
        _printAllButton.setEnabled(anyGroupsHaveHandins);

        if(!groupHasHandin)
        {
            for(JButton button : _groupButtons)
            {
                button.setEnabled(false);
            }
        }
        //If there is a selected group and that group has a handin, enable buttons appropriately
        else
        {
            //Group buttons
            _gradeButton.setEnabled(Allocator.getRubricManager().hasRubric(part, selectedGroup));
            _testButton.setEnabled(part.hasTest());
            _runButton.setEnabled(part.hasRun());
            _openButton.setEnabled(part.hasOpen());
            _printButton.setEnabled(part.hasPrint());

            boolean hasReadme = false;
            try
            {
                hasReadme = part.hasReadme(selectedGroup);
            }
            catch(ActionException ex)
            {
                new ErrorView(ex, "Could not determine if " + selectedGroup + " has a README");
            }
            catch(MissingHandinException ex)
            {
                this.notifyHandinMissing(ex);
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
                    Group selectedGroup = _groupList.getSelectedValue();

                    //Update the currently grading label and buttons
                    _currentlyGradingLabel.update(selectedGroup);
                    updateButtonStates();

                    //If the selected group does not have a handin, inform the user
                    if(selectedGroup != null)
                    {
                        boolean hasHandin = false;
                        List<GroupStatus> statuses = _assignedGroups.get(_dpList.getSelectedValue());
                        if(statuses != null)
                        {
                            for(GroupStatus status : statuses)
                            {
                                if(status.getGroup().equals(selectedGroup))
                                {
                                    hasHandin = status.hasHandin();
                                }
                            }
                        }
                        if(!hasHandin)
                        {
                            JOptionPane.showMessageDialog(FrontendView.this,
                                    "The handin for " + selectedGroup.getName() + " is missing.",
                                    "Handin Missing", JOptionPane.WARNING_MESSAGE);
                        }
                    }
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
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

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

                        // Attempt to display the blacklist panel, if the necessary
                        // database information cannot be retrieved succesfully, do not
                        // show the blacklist panel
                        try
                        {
                            BlacklistPanel blacklistPanel = new BlacklistPanel(
                                    panel.getPreferredSize(),
                                    panel.getBackground());
                            panel.add(blacklistPanel);
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
                Allocator.getGradingServices().removeUserWorkspace();
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
                CakehatAboutBox.displayRelativeTo(FrontendView.this);
            }
        });
        helpMenu.add(aboutItem);
    }

    /**
     * Hides the menu and the components of the content pane. Visually it
     * appears as if they are disabled. A panel is returned to put the modal
     * content into.
     * <br/>
     * The disabled appearance does not look as good over SSH because when
     * running over SSH transparency is very expensive, so transparency is not
     * used when running over SSH.
     *
     * @return panel to put content into
     *
     * @see #showNormalContentInFrame()
     */
    private JPanel showModalContentInFrame()
    {
        // Padding used around edges to prevent the content from being flush
        // with the frame
        int paddingSize = 15;

        // Disabling a component in Swing such that all of its subcomponents are
        // disabled is an absurdly non-trivial task
        // To fake this, take the menu bar and content pane and draw its
        // appearance to a BufferedImage, then hide the actual menu bar and
        // content pane and draw the BufferedImage
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
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height - 2 * paddingSize)), BorderLayout.WEST);
        disablePanel.add(Box.createRigidArea(new Dimension(paddingSize, disablePanelSize.height - 2 * paddingSize)), BorderLayout.EAST);

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
                FrontendView.this.showNormalContentInFrame();
            }
        });
        closeButton.setPreferredSize(new Dimension(closeButtonWidth, closeButtonHeight));
        closePanel.add(closeButton);

        return overlayContentPanel;
    }

    /**
     * Removes the modally displayed content in the frame, and restores the
     * frame to showing its normal content.
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
                printButtonActionPerformed();
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
     * Called when the run button is clicked.
     */
    private void runButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            dp.run(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("run", group, dp, ex);
        }
        catch(MissingHandinException ex)
        {
            this.notifyHandinMissing(ex);
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
     * Called when the print button is clicked.
     */
    private void printButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            dp.print(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("print", group, dp, ex);
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
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            dp.test(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("test", group, dp, ex);
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
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            dp.open(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("open", group, dp, ex);
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
        DistributablePart dp = _dpList.getSelectedValue();
        Group group = _groupList.getSelectedValue();
        try
        {
            dp.viewReadme(group);
        }
        catch(ActionException ex)
        {
            this.generateErrorView("view README", group, dp, ex);
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
        DistributablePart dp = _dpList.getSelectedValue();
        try
        {
            dp.demo();
        }
        catch(ActionException ex)
        {
            this.generateErrorView("demo", null, dp, ex);
        }
    }

    /**
     * Called when the print all button is clicked.
     */
    private void printAllButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        Collection<Group> groups = _groupList.getListData();
        try
        {
            dp.print(groups);
        }
        catch(ActionException ex)
        {
            this.generateErrorViewMultiple("print", groups, dp, ex);
        }
    }

    /**
     * Called when the grading guide button is clicked.
     */
    private void gradingGuideButtonActionPerformed()
    {
        DistributablePart dp = _dpList.getSelectedValue();
        try
        {
            dp.viewGradingGuide();
        }
        catch (FileNotFoundException ex)
        {
            this.generateErrorView("view grading guide", null, dp, ex);
        }
    }

    private void notifyHandinMissing(MissingHandinException ex)
    {
        JOptionPane.showMessageDialog(this,
            "The handin for " + ex.getGroup().getName() + " is missing.\n" +
            "The data will now be refreshed.",
            "Handin Missing",
            JOptionPane.WARNING_MESSAGE);

        this.loadAssignedGrading(true);
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
                            FrontendView.this,
                            _dpList.getSelectedValue(),
                            _groupList.getListData());
                    disablePanel.add(submitPanel);
                }
                catch(SQLException e)
                {
                    showNormalContentInFrame();
                    new ErrorView(e, "Unable to show submit grading view due to database issues");
                }
            }
        });
    }

    /**
     * Creates an {@link ErrorView} with the message "Could not
     * <code>msgInsert</code> for group <code>group</code> on part
     * <code>dp</code> of assignment <code>dp.getAssignment()</code>." and the
     * given <code>cause</code>.
     *
     * @see #generateErrorViewMultiple(java.lang.String, java.util.Collection, cakehat.config.handin.DistributablePart, java.lang.Exception)
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorView(String msgInsert, Group group, DistributablePart dp, Exception cause)
    {
        String message = "Could not " + msgInsert;

        if(group != null)
        {
            message += " for group " + group;
        }

        if(dp != null)
        {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
    }

    /**
     * Creates an {@link ErrorView} with the message "Could not
     * <code>msgInsert</code> for groups <code>groups</code> on part
     * <code>dp</code> of assignment <code>dp.getAssignment()</code>." and the
     * given <code>cause</code>.
     *
     * @see #generateErrorView(java.lang.String, cakehat.database.Group, cakehat.config.handin.DistributablePart, java.lang.Exception)
     *
     * @param msgInsert
     * @param group
     * @param dp
     * @param cause
     */
    private void generateErrorViewMultiple(String msgInsert, Collection<Group> groups, DistributablePart dp, Exception cause)
    {
        String message = "Could not " + msgInsert;

        if(!groups.isEmpty())
        {
            message += " for groups " + groups;
        }

        if(dp != null)
        {
            message += " on part " + dp + " of assignment " + dp.getAssignment();
        }

        message += ".";

        new ErrorView(cause, message);
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
        Map<DistributablePart, List<GroupStatus>> updatedMap =
                new HashMap<DistributablePart, List<GroupStatus>>();

        for(DistributablePart part : _assignedGroups.keySet())
        {
            List<GroupStatus> statuses = _assignedGroups.get(part);

            //If this is the part, find and update the status
            if(part.equals(savedPart))
            {
                GroupStatus currStatus = null;
                for(GroupStatus status : statuses)
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

                GroupStatus newStatus = new GroupStatus(savedGroup,
                        currStatus.isSubmitted(), hasRubricScore,
                        currStatus.hasHandin());

                int currStatusIndex = statuses.indexOf(currStatus);
                ArrayList<GroupStatus> newStatuses = new ArrayList<GroupStatus>(statuses);
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
    void loadAssignedGrading(boolean useSeparateThread)
    {
        Runnable loadRunnable = new Runnable()
        {
            public void run()
            {
                Map<DistributablePart, List<GroupStatus>> newMap =
                        new HashMap<DistributablePart, List<GroupStatus>>();
                try
                {
                    Set<DistributablePart> parts = Allocator.getDatabase().getDPsWithAssignedStudents(USER);
                    for(DistributablePart part : parts)
                    {
                        part.getHandin().clearHandinCache();

                        Collection<Group> groups = Allocator.getDatabase().getGroupsAssigned(part, USER);
                        Map<Group, Double> submittedScores = Allocator.getDatabase()
                                .getPartScoresForGroups(part, groups);
                        Map<Group, Double> rubricScores = Allocator.getRubricManager()
                                .getPartScores(part, groups);

                        List<GroupStatus> statuses = new ArrayList<GroupStatus>();
                        newMap.put(part, statuses);

                        for(Group group : groups)
                        {
                            boolean hasHandin = false;
                            try
                            {
                                hasHandin = part.getHandin().hasHandin(group);
                            }
                            catch(IOException e)
                            {
                                new ErrorView(e, "Unable to determine if " + group + " has a handin");
                            }

                            GroupStatus status = new GroupStatus(group,
                                    submittedScores.containsKey(group),
                                    rubricScores.containsKey(group) &&
                                    rubricScores.get(group) != 0,
                                    hasHandin);
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
    private void showAssignedGradingChanges(final Map<DistributablePart, List<GroupStatus>> newAssignedGroups)
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
                List<GroupStatus> currGroups = _assignedGroups.get(selected);
                List<GroupStatus> newGroups = newAssignedGroups.get(selected);
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
                        List<GroupStatus> statuses;
                        if(_assignedGroups.containsKey(currPart))
                        {
                            statuses = new ArrayList(_assignedGroups.get(currPart));
                        }
                        else
                        {
                            statuses = Collections.emptyList();
                        }
                        Collections.sort(statuses);
                        List<Group> groups = new ArrayList<Group>();
                        for(GroupStatus status : statuses)
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

                    updateButtonStates();
                }
            };

            EventQueue.invokeLater(uiRunnable);
        }
        else
        {
            _assignedGroups = newAssignedGroups;
        }
    }

    /**
     * The status of a group, which contains the following information:
     * <ul>
     * <li>If a grade has been submitted</li>
     * <li>If the rubric has a non-zero score</li>
     * <li>If there is a handin</li>
     * </ul>
     */
    private static class GroupStatus implements Comparable<GroupStatus>
    {
        private final Group _group;
        private final boolean _submitted;
        private final boolean _hasRubricScore;
        private final boolean _hasHandin;

        public GroupStatus(Group group, boolean submitted, boolean hasRubricScore, boolean hasHandin)
        {
            _group = group;
            _submitted = submitted;
            _hasRubricScore = hasRubricScore;
            _hasHandin = hasHandin;
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

        public boolean hasHandin()
        {
            return _hasHandin;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean equals = false;
            if(obj instanceof GroupStatus)
            {
                GroupStatus other = (GroupStatus) obj;

                equals = other._group.equals(_group) &&
                         other._submitted == _submitted &&
                         other._hasRubricScore == _hasRubricScore &&
                         other._hasHandin == _hasHandin;
            }

            return equals;
        }

        @Override
        public int compareTo(GroupStatus other)
        {
            //Comparison on group names breaks ties
            int groupComp = this.getGroup().getName().compareTo(other.getGroup().getName());

            //Heirarchy
            // - submitted
            // - rubric score
            // - nothing
            // - missing handin
            if(!this.hasHandin() && !other.hasHandin())
            {
                return groupComp;
            }
            else if(!this.hasHandin())
            {
                return 1;
            }
            else if(!other.hasHandin())
            {
                return -1;
            }
            else if(this.isSubmitted() && other.isSubmitted())
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
            boolean hasRubricScore = false;
            boolean submitted = false;
            boolean hasHandin = false;
            List<GroupStatus> statuses = _assignedGroups.get(_part);
            for(GroupStatus status : statuses)
            {
                if(status.getGroup().equals(group))
                {
                    submitted = status.isSubmitted();
                    hasRubricScore = status.hasRubricScore();
                    hasHandin = status.hasHandin();
                }
            }

            //Build representation
            String pre;
            String post;
            if(!hasHandin)
            {
                pre = "<font color=red>✗</font> <strike>";
                post = "</strike>";
            }
            else if(submitted)
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
            List<GroupStatus> statuses = _assignedGroups.get(part);
            for(GroupStatus status : statuses)
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

            String name = part.getAssignment().getName() + ": "  + part.getName();
            String representation = "<html>" + pre + name + post + "</html>";

            return representation;
        }
    }

    /**
     * Label that displays the currently selected student.
     */
    private class CurrentlyGradingLabel extends JLabel
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
            if(_dpList.getSelectedValue().getAssignment().hasGroups())
            {
                text = group.getName() + " " + group.getMembers();
            }
            //If not a group assignment, show name of student
            else if(!group.getMembers().isEmpty())
            {
                text = group.getMembers().get(0).getLogin();
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