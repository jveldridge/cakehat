package cakehat.views.config;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import support.ui.PaddingPanel;

/**
 *
 * @author jak2
 */
public class ConfigManagerView extends JFrame
{
    private final JTabbedPane _tabbedPane;
    private final UniqueElementSingleThreadWorker _worker = UniqueElementSingleThreadWorker.newInstance();
    
    private ConfigManagerView(boolean isSSH)
    {
        super("cakehat (configuration manager)" + (isSSH ? " [ssh]" : ""));
        
        //On close, save any remaining changes and shut down the worker
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent we)
            {
                try
                {
                    _worker.blockingShutdown();
                }
                catch(InterruptedException ex)
                {
                    //TODO: Look into if there is anything better to be done here - this occurs after the configuration
                    //      window has been closed
                    ex.printStackTrace();
                }
            }
        });
        
        //Create a tab for each configuration category: TAs, students, assignments, & email
        _tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.add(_tabbedPane);
        _tabbedPane.insertTab("TAs", null, new PaddingPanel(new TAPanel(_worker), 10), null, 0);
        _tabbedPane.insertTab("Students", null, new PaddingPanel(new StudentPanel(_worker), 10), null, 1);
        _tabbedPane.insertTab("Assignments", null, new PaddingPanel(new AssignmentPanel(_worker), 10), null, 2);
        _tabbedPane.insertTab("Email", null, new PaddingPanel(new EmailPanel(_worker), 10), null, 3);
        
        //Display
        this.setMinimumSize(new Dimension(640, 360));
        this.setPreferredSize(new Dimension(960, 540));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        this.setVisible(true);
    }
    
    public static void launch(final boolean isSSH)
    {   
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {   
                new ConfigManagerView(isSSH);
            }
        });
    }
    
    //For testing
    public static void main(String[] args) throws Throwable
    {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        launch(false);
    }
}