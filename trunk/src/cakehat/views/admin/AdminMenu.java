package cakehat.views.admin;

import cakehat.CakehatAboutBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author jak2
 */
class AdminMenu extends JMenuBar
{
    private final AdminView _adminView;
    
    AdminMenu(AdminView adminView)
    {
        _adminView = adminView;
        
        //File menu
        JMenu fileMenu = new JMenu("File");
        this.add(fileMenu);

        //Refresh item
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        refreshItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _adminView.refresh();
            }
        });
        fileMenu.add(refreshItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                System.exit(0);
            }
        });
        fileMenu.add(quitItem);

        //Help menu
        JMenu helpMenu = new JMenu("Help");
        this.add(helpMenu);

        //About
        JMenuItem aboutItem = new JMenuItem("About cakehat");
        aboutItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.displayRelativeTo(_adminView);
            }
        });
        helpMenu.add(aboutItem); 
    }
}