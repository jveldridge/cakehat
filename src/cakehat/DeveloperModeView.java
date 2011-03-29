package cakehat;

import cakehat.views.admin.BackendView;
import cakehat.views.frontend.FrontendView;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Window shown when running in developer mode that allows for selecting either
 * the frontend or backend view.
 *
 * @author jak2
 */
class DeveloperModeView extends JFrame
{
    private DeveloperModeView()
    {
        super("[cakehat] developer mode - " + Allocator.getUserUtilities().getUserLogin());

         //Menu bar
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        //File menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        //Quit item
        JMenuItem menuItem = new JMenuItem("Quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                System.exit(0);
            }
        });
        menu.add(menuItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);

        //Help contents item
        menuItem = new JMenuItem("Help Contents");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JOptionPane.showMessageDialog(DeveloperModeView.this, "This feature is not yet available");
            }
        });
        menu.add(menuItem);

        //About
        menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(DeveloperModeView.this);
            }
        });
        menu.add(menuItem);

        // Panel for buttons
        JPanel panel = new JPanel(new GridLayout(1,2));
        panel.setPreferredSize(new Dimension(300,80));
        this.add(panel);

        // Frontend
        JButton frontendButton = new JButton("Frontend");
        frontendButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.FRONTEND);
               FrontendView.launch();
               DeveloperModeView.this.dispose();
           }
        });
        panel.add(frontendButton);

        // Backend
        JButton backendButton = new JButton("Backend");
        backendButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.BACKEND);
               BackendView.launch();
               DeveloperModeView.this.dispose();
           }
        });
        panel.add(backendButton);

        // Configure for display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void launch()
    {
        new DeveloperModeView().setVisible(true);
    }
}