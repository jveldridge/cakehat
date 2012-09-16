package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.CakehatReleaseInfo;
import cakehat.CakehatSession;
import cakehat.CakehatSession.ConnectionType;
import cakehat.database.Group;
import cakehat.assignment.Part;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import cakehat.views.shared.gradingsheet.GradingSheetPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import support.ui.FormattedLabel;
import support.ui.ModalJFrameHostHelper;
import support.ui.ModalJFrameHostHelper.CloseAction;
import support.ui.PaddingPanel;

/**
 *
 * @author jak2
 */
public class GraderView extends JFrame
{
    public static void launch()
    {   
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {   
                new GraderView().setVisible(true);
            }
        });
    }
    
    private final JScrollPane _mainPane;
    private final PartAndGroupPanel _partAndGroupPanel;
    private final ActionsPanel _actionsPanel;
    
    private GradingSheetPanel _currentlyDisplayedSheet;
    
    private GraderView()
    {
        //Frame title
        super("cakehat" + (CakehatSession.getUserConnectionType() == ConnectionType.REMOTE ? " [ssh]" : ""));

        //Close operation
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent we)
            {
                saveDisplayedGradingSheet();
            }
        });
        
        //Setup UI
        _mainPane = new JScrollPane();
        _partAndGroupPanel = new PartAndGroupPanel();
        _actionsPanel = new ActionsPanel(this, _partAndGroupPanel);
        this.initUI();
        this.setJMenuBar(new GraderMenu(this));
        
        //Display
        this.setMinimumSize(new Dimension(879, 550));
        this.setPreferredSize(new Dimension(1024, 550));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
    }
    
    private void initUI()
    {
        //Visual setup
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        
        //Part and group panel
        JPanel partAndGroupPaddingPanel = new PaddingPanel(_partAndGroupPanel, 10, 10, 10, 5,
                _partAndGroupPanel.getBackground());
        Dimension partAndGroupPanelSize = new Dimension(200, Short.MAX_VALUE);
        partAndGroupPaddingPanel.setMinimumSize(partAndGroupPanelSize);
        partAndGroupPaddingPanel.setPreferredSize(partAndGroupPanelSize);
        partAndGroupPaddingPanel.setMaximumSize(partAndGroupPanelSize);
        this.add(partAndGroupPaddingPanel);
        
        //Right panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        JPanel rightContentPanel = new JPanel();
        rightContentPanel.setLayout(new BoxLayout(rightContentPanel, BoxLayout.X_AXIS));
        rightPanel.add(rightContentPanel, BorderLayout.CENTER);
        this.add(new PaddingPanel(rightPanel, 10, 0, 0, 10, this.getBackground()));
        
        //Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(FormattedLabel.asHeader(" ")); //Used to match the spacing of the label headers for other panels
        mainPanel.add(Box.createVerticalStrut(10));
        _mainPane.setAlignmentX(LEFT_ALIGNMENT);
        _mainPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(_mainPane);
        rightContentPanel.add(mainPanel);
        
        rightContentPanel.add(Box.createHorizontalStrut(5)); 
        
        //Actions panel
        Dimension actionsPanelSize = new Dimension(185, Short.MAX_VALUE);
        _actionsPanel.setMinimumSize(actionsPanelSize);
        _actionsPanel.setPreferredSize(actionsPanelSize);
        _actionsPanel.setMaximumSize(actionsPanelSize);
        rightContentPanel.add(_actionsPanel);
        
        //Notify students bar
        JPanel notifyBarPanel = this.createNotifyBar(rightPanel, rightContentPanel);
        rightPanel.add(notifyBarPanel, BorderLayout.SOUTH);
        
        //Selection change
        _partAndGroupPanel.addSelectionListener(new PartAndGroupPanel.PartAndGroupSelectionListener()
        {
            @Override
            public void selectionChanged(Part part, Set<Group> groups)
            {
                notifySelectionChanged(part, groups);
            }
        });
        this.notifySelectionChanged(_partAndGroupPanel.getSelectedPart(), _partAndGroupPanel.getSelectedGroups());
    }
    
    private JPanel createNotifyBar(final JPanel containerPanel, final JPanel currentContentPanel)
    {
        final NotifyStudentsPanel notifyPanel = new NotifyStudentsPanel(this, _partAndGroupPanel);
        
        //Layout
        JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
        
        final JButton notifyButton = new JButton("Notify Students",
                IconLoader.loadIcon(IconSize.s16x16, IconImage.GO_NEXT));
        notifyButton.setHorizontalTextPosition(SwingConstants.LEFT);
        notifyButton.setIconTextGap(10);
        buttonPanel.add(notifyButton, BorderLayout.EAST);
        
        final JButton backButton = new JButton("Grade", IconLoader.loadIcon(IconSize.s16x16, IconImage.GO_PREVIOUS));
        backButton.setVisible(false);
        backButton.setIconTextGap(10);
        buttonPanel.add(backButton, BorderLayout.WEST);

        JPanel innerPanel = new JPanel(new BorderLayout(0, 0));
        innerPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        innerPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        innerPanel.add(buttonPanel, BorderLayout.SOUTH);
        JPanel outerPanel = new PaddingPanel(innerPanel, 5, 10, 0, 10, innerPanel.getBackground());
        
        //Logic
        final Runnable backAction = new Runnable()
        {
            public void run()
            {
                notifyButton.setVisible(true);
                backButton.setVisible(false);
                
                notifySelectionChanged(_partAndGroupPanel.getSelectedPart(), _partAndGroupPanel.getSelectedGroups());
                
                containerPanel.remove(notifyPanel);
                containerPanel.add(currentContentPanel, BorderLayout.CENTER);
                containerPanel.repaint();
                containerPanel.revalidate();
            }
        };
        notifyPanel.setBackAction(backAction);
        
        notifyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                notifyButton.setVisible(false);
                backButton.setVisible(true);
                
                saveDisplayedGradingSheet();
                
                containerPanel.remove(currentContentPanel);
                containerPanel.add(notifyPanel, BorderLayout.CENTER);
                containerPanel.repaint();
                containerPanel.revalidate();
            }
        });
        backButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                backAction.run();
            }
        });
                
        return outerPanel;
    }
    
    private void notifySelectionChanged(Part part, Set<Group> groups)
    {   
        saveDisplayedGradingSheet();
        _currentlyDisplayedSheet = null;
        
        Component componentToDisplay = null;
        
        if(part == null)
        {
            componentToDisplay = FormattedLabel.asHeader("cakehat v" + CakehatReleaseInfo.getVersion())
                        .centerHorizontally();
        }
        else
        {   
            if(groups.isEmpty())
            {
                String studentOrGroup = part.getAssignment().hasGroups() ? "group" : "student";
                componentToDisplay = FormattedLabel.asHeader("Select a " + studentOrGroup + " to continue grading")
                        .centerHorizontally();
            }
            else if(groups.size() == 1)
            {
                Group group = groups.iterator().next();
                _currentlyDisplayedSheet = GradingSheetPanel.getPanel(part, group, false, true);
                componentToDisplay = _currentlyDisplayedSheet;
            }
            else
            {
                String studentOrGroup = part.getAssignment().hasGroups() ? "group" : "student";
                componentToDisplay = FormattedLabel.asHeader("Select a single " + studentOrGroup + " to fill out " + 
                        "their grading sheet").centerHorizontally();
            }
        }
        
        _mainPane.setViewportView(componentToDisplay);
        _mainPane.repaint();
        _mainPane.revalidate();
    }
    
    private void saveDisplayedGradingSheet()
    {
        if(_currentlyDisplayedSheet != null)
        {
            _currentlyDisplayedSheet.save();
        }
    }
    
    final void refresh()
    {
        try
        {
            Allocator.getDataServices().updateDataCache();
            
            saveDisplayedGradingSheet();
            
            _partAndGroupPanel.loadAssignedGrading();
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to refresh", e);
        }
    }
    
    CloseAction hostModal(JComponent component)
    {
        boolean useTransparency = CakehatSession.getUserConnectionType() != ConnectionType.REMOTE;
        CloseAction closeAction = ModalJFrameHostHelper.host(this, component, 45, useTransparency);
        
        return closeAction;
    }
}