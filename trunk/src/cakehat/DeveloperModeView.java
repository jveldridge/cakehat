package cakehat;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Window shown when running in developer mode that allows for selecting either the grader or admin view.
 *
 * @author jak2
 */
class DeveloperModeView extends JFrame
{
    private DeveloperModeView(final String[] args)
    {
        super("cakehat (developer)");

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
                DeveloperModeView.this.dispose();
            }
        });
        menu.add(menuItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);

        //About
        menuItem = new JMenuItem("About cakehat");
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.display(DeveloperModeView.this);
            }
        });
        menu.add(menuItem);

        // Panel for buttons
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.setPreferredSize(new Dimension(300, 150));
        this.add(panel);

        // Grader
        JButton graderButton = new JButton("Grader");
        graderButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.GRADER, args);
               DeveloperModeView.this.dispose();
           }
        });
        panel.add(graderButton);

        // Admin
        JButton adminButton = new JButton("Admin");
        adminButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.ADMIN, args);
               DeveloperModeView.this.dispose();
           }
        });
        panel.add(adminButton);
        
        // Config
        JButton configButton = new JButton("Config");
        configButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
               CakehatMain.setRunMode(CakehatRunMode.CONFIG, args);
               DeveloperModeView.this.dispose();
            }
        });
        panel.add(configButton);
        
        panel.add(Box.createHorizontalBox());

        // Configure for display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void launch(String[] args)
    {
        new DeveloperModeView(args).setVisible(true);
    }
}