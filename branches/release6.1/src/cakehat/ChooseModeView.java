package cakehat;

import cakehat.CakehatMain.TerminalOption;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Window shown when the cakehat is being run developer mode and no run mode was specified.
 *
 * @author jak2
 */
class ChooseModeView extends JFrame
{
    private ChooseModeView(final Map<TerminalOption, List<String>> parsedArgs)
    {
        super("cakehat (mode selector)");
        
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
                ChooseModeView.this.dispose();
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
                CakehatAboutBox.display(ChooseModeView.this);
            }
        });
        menu.add(menuItem);

        // Panel for buttons
        JPanel panel = new JPanel(new GridLayout(1, 3));
        this.add(panel);

        // Grader
        JButton graderButton = new JButton("Grader");
        graderButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.GRADER, parsedArgs);
               ChooseModeView.this.dispose();
           }
        });
        panel.add(graderButton);

        // Admin
        JButton adminButton = new JButton("Admin");
        adminButton.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               CakehatMain.setRunMode(CakehatRunMode.ADMIN, parsedArgs);
               ChooseModeView.this.dispose();
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
               CakehatMain.setRunMode(CakehatRunMode.CONFIG, parsedArgs);
               ChooseModeView.this.dispose();
            }
        });
        panel.add(configButton);

        // Configure for display
        this.pack();
        this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(400, 150));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void launch(Map<TerminalOption, List<String>> parsedArgs)
    {
        new ChooseModeView(parsedArgs).setVisible(true);
    }
}