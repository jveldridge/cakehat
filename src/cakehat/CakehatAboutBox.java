package cakehat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The about box displays information about which release of cakehat this is and gives credit to the developers who have
 * worked on it.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class CakehatAboutBox extends JDialog
{
    private static final int PANEL_HEIGHT = 200;
    private static final Dimension IMAGE_PANEL_SIZE = new Dimension(160, PANEL_HEIGHT);
    private static final Dimension INFO_PANEL_SIZE = new Dimension(270, PANEL_HEIGHT);
    private static final Dimension PANEL_SIZE = new Dimension(IMAGE_PANEL_SIZE.width + INFO_PANEL_SIZE.width, PANEL_HEIGHT);

    private static CakehatAboutBox _currentlyDisplayedBox;

    private CakehatAboutBox(Window owner)
    {
        super(owner, "About cakehat", ModalityType.MODELESS);

        // Overall
        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(PANEL_SIZE);
        panel.setPreferredSize(PANEL_SIZE);        
        this.add(panel);

        // Icon
        ImageIcon cakehatIcon = new ImageIcon(getClass().getResource("/cakehat/cakehat.png"));
        panel.add(new JLabel(cakehatIcon), BorderLayout.CENTER);

        // Info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        String infoText =
                "<html><h2>Version " + CakehatReleaseInfo.getVersion() + "</h2>" +
               "<font color=gray>" +
                CakehatReleaseInfo.getReleaseCommitNumber() +
                " (" + CakehatReleaseInfo.getReleaseDate() + ")</font>" +
                "<br/><br/>" +
                "<h3>Developers</h3>" +
                "Jonathan Eldridge, Yudi Fu, Joshua Kaplan," +
                "<br/>Stephen Poletto, Hannah Rosen, Paul" +
                "<br/>Sastrasinh, Alex Unger, & Wil Yegelwel" +
                "<br/><br/>" +
                "<a href=http://cakehat.googlecode.com/>http://cakehat.googlecode.com</a>" +
                "</html>";
        infoPanel.add(new JLabel(infoText));infoPanel.setPreferredSize(INFO_PANEL_SIZE);
        panel.add(infoPanel, BorderLayout.EAST);

        // Display
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setResizable(false);

        // Close operation
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                _currentlyDisplayedBox = null;
            }
        });
    }

    /**
     * Displays the about box.  If the about box is not already open it will be created, owned to the provided
     * {@code owner} and positioned relative to the {@code owner}. If the about box is already open it will be brought
     * to the front and positioned relative to {@code owner} but its ownership will not change.
     * 
     * 
     * @param owner The window the about box will be displayed relative to and potentially owned to. {@code null} may be
     * passed in, in which case the about box is centered on the screen and has no owner.
     */
    public static void display(Window owner)
    {
        if(_currentlyDisplayedBox != null)
        {
            _currentlyDisplayedBox.toFront();
        }
        else
        {
            _currentlyDisplayedBox = new CakehatAboutBox(owner);
        }
        _currentlyDisplayedBox.setLocationRelativeTo(owner);
        _currentlyDisplayedBox.setVisible(true);
    }
}