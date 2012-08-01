package cakehat.views.grader;

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
class GraderMenu extends JMenuBar
{   
    GraderMenu(final GraderView graderView)
    {
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
                graderView.refresh();
            }
        });
        fileMenu.add(refreshItem);

        //Blacklist item
        JMenuItem blacklistItem = new JMenuItem("Modify blacklist");
        blacklistItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        blacklistItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                graderView.showModalContentInFrame(new BlacklistPanel());
            }
        });
        fileMenu.add(blacklistItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                graderView.dispose();
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
                CakehatAboutBox.display(graderView);
            }
        });
        helpMenu.add(aboutItem);
    }
}