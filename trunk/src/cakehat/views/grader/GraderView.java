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
import java.awt.FlowLayout;
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
import javax.swing.SwingConstants;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import support.ui.FormattedLabel;
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
    
    private final JPanel _contentPanel;
    private final JPanel _navigationPanel;
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
        _contentPanel = new JPanel();
        _navigationPanel = new JPanel();
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
        JPanel navigationBufferPanel = new JPanel();
        
        //Visual setup
        this.setLayout(new BorderLayout(0, 0));
        this.add(_contentPanel, BorderLayout.CENTER);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(navigationBufferPanel, BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        
        //Content panel
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.X_AXIS));
        
        Dimension partAndGroupPanelSize = new Dimension(200, Short.MAX_VALUE);
        _partAndGroupPanel.setMinimumSize(partAndGroupPanelSize);
        _partAndGroupPanel.setPreferredSize(partAndGroupPanelSize);
        _partAndGroupPanel.setMaximumSize(partAndGroupPanelSize);
        _contentPanel.add(_partAndGroupPanel);
        
        _contentPanel.add(Box.createHorizontalStrut(5));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        _contentPanel.add(mainPanel);
        mainPanel.add(FormattedLabel.asHeader(" ")); //Used to match the spacing of the label headers for other panels
        mainPanel.add(Box.createVerticalStrut(5));
        _mainPane.setAlignmentX(LEFT_ALIGNMENT);
        _mainPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(_mainPane);
        
        _contentPanel.add(Box.createHorizontalStrut(5));
        
        Dimension actionsPanelSize = new Dimension(185, Short.MAX_VALUE);
        _actionsPanel.setMinimumSize(actionsPanelSize);
        _actionsPanel.setPreferredSize(actionsPanelSize);
        _actionsPanel.setMaximumSize(actionsPanelSize);
        _contentPanel.add(_actionsPanel);
        
        //Navigation panel
        navigationBufferPanel.setPreferredSize(new Dimension(0, 42));
        navigationBufferPanel.setLayout(new BorderLayout(0, 0));
        navigationBufferPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
        JPanel centerNavigationBufferPanel = new PaddingPanel(_navigationPanel, 5, 5, 10, 10, new Color(195, 195, 195));
        navigationBufferPanel.add(centerNavigationBufferPanel, BorderLayout.CENTER);
        
        this.addNotifyStudentsButton();
        
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
    
    private void addNotifyStudentsButton()
    {
        _navigationPanel.removeAll();
        _navigationPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton notifyButton = new JButton("Notify Students", IconLoader.loadIcon(IconSize.s16x16, IconImage.GO_NEXT));
        notifyButton.setHorizontalTextPosition(SwingConstants.LEFT);
        notifyButton.setIconTextGap(10);
        _navigationPanel.add(notifyButton);
        
        notifyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                showModalContentInFrame(new NotifyStudentsPanel());
            }
        });
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
    
    void showModalContentInFrame(final JComponent modalContent)
    {
        //Add back button to navigation panel
        _navigationPanel.removeAll();
        _navigationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton backButton = new JButton("Grade", IconLoader.loadIcon(IconSize.s16x16, IconImage.GO_PREVIOUS));
        backButton.setIconTextGap(10);
        _navigationPanel.add(backButton);

        //Hide current content and show modal content
        GraderView.this.getJMenuBar().setVisible(false);
        GraderView.this.remove(_contentPanel);
        GraderView.this.add(modalContent, BorderLayout.CENTER);

        //Restore content when the back button is pressed
        backButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                GraderView.this.getJMenuBar().setVisible(true);
                GraderView.this.remove(modalContent);
                GraderView.this.add(_contentPanel, BorderLayout.CENTER);
                GraderView.this.addNotifyStudentsButton();
            }
        });
    }
}