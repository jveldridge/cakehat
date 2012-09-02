package cakehat.views.config;

import cakehat.CakehatSession;
import cakehat.CakehatSession.ConnectionType;
import cakehat.logging.ErrorReporter;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import support.ui.ModalDialog;
import support.ui.ModalJFrameHostHelper;
import support.ui.ModalJFrameHostHelper.CloseAction;
import support.ui.PaddingPanel;

/**
 *
 * @author jak2
 */
public class ConfigManagerView extends JFrame
{
    private final JTabbedPane _tabbedPane;
    private final UniqueElementSingleThreadWorker _worker = UniqueElementSingleThreadWorker.newInstance();
    
    private ConfigManagerView(int selectedIndex)
    {
        super("cakehat (configuration manager)" +
                (CakehatSession.getUserConnectionType() == ConnectionType.REMOTE ? " [ssh]" : ""));
        
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
                    ErrorReporter.report("Database thread was interrupted while shutting down", ex);
                }
            }
        });
        
        //Create a tab for each configuration category: TAs, students, assignments, & email
        _tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.add(_tabbedPane);
        _tabbedPane.insertTab("TAs", null, new PaddingPanel(new TAPanel(_worker), 10), null, 0);
        _tabbedPane.insertTab("Students", null, new PaddingPanel(new StudentPanel(this, _worker), 10), null, 1);
        _tabbedPane.insertTab("Assignments", null, new PaddingPanel(new AssignmentPanel(this, _worker), 10), null, 2);
        _tabbedPane.insertTab("Email", null, new PaddingPanel(new EmailPanel(this, _worker), 10), null, 3);
        _tabbedPane.insertTab("Scripts", null, new PaddingPanel(new ScriptsPanel(this), 10), null, 4);
        
        _tabbedPane.setSelectedIndex(selectedIndex);
        
        //Display
        this.setMinimumSize(new Dimension(640, 360));
        this.setPreferredSize(new Dimension(960, 540));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
    }
    
    CloseAction hostModal(JComponent component)
    {
        boolean useTransparency = CakehatSession.getUserConnectionType() != ConnectionType.REMOTE;
        CloseAction closeAction = ModalJFrameHostHelper.host(this, component, 45, useTransparency);
        
        return closeAction;
    }
    
    public static void launch(final boolean isCakehatConfigured)
    {   
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {   
                boolean proceed = true;
                if(isCakehatConfigured)
                {
                    proceed = ModalDialog.showConfirmation(null, "Warning",
                        "The cakehat configuration manager should only be run when no other instances of cakehat are " +
                        "running. Running this configuration manager while other instances of cakehat are running " +
                        "can result in your database being left in an unusable state.",
                        "Proceed", "Cancel");
                }
                
                if(proceed)
                {
                    new ConfigManagerView(isCakehatConfigured ? 0 : 4).setVisible(true);
                }
            }
        });
    }
}