package cakehat;

import cakehat.icon.CakehatIconLoader;
import cakehat.icon.CakehatIconLoader.IconSize;
import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import support.ui.PaddingPanel;

/**
 * The about box displays information about which release of cakehat this is and gives credit to the developers who have
 * worked on it.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class CakehatAboutBox extends JDialog
{
    private static final ImmutableList<String> DEVELOPERS = ImmutableList.of(
            "Jonathan Eldridge",
            "Yudi Fu",
            "Joshua Kaplan",
            "Stephen Poletto",
            "Hannah Rosen",
            "Paul Sastrasinh",
            "Alex Unger",
            "Wil Yegelwel");
    private static final String CAKEHAT_URL = "http://cakehat.googlecode.com";

    private static CakehatAboutBox _currentlyDisplayedBox;

    private CakehatAboutBox(Window owner)
    {
        super(owner, "About cakehat", ModalityType.MODELESS);

        // Overall
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setPreferredSize(new Dimension(500, 200));
        this.setContentPane(new PaddingPanel(contentPanel, 10, contentPanel.getBackground()));

        // Icon
        Icon cakehatIcon = CakehatIconLoader.loadIcon(IconSize.s200x200);
        JLabel cakehatIconLabel = new JLabel(cakehatIcon);
        cakehatIconLabel.setMinimumSize(new Dimension(cakehatIcon.getIconWidth(), cakehatIcon.getIconWidth()));
        contentPanel.add(cakehatIconLabel, BorderLayout.WEST);
        
        // Info
        StringBuilder infoText = new StringBuilder("<html>");
        infoText.append("<h2 style='margin-bottom:0px;'>Version ");
        infoText.append(CakehatReleaseInfo.getVersion());
        infoText.append("</h2>");
        infoText.append("<font color=gray>");
        infoText.append(CakehatReleaseInfo.getReleaseCommitNumber());
        infoText.append(" (");
        infoText.append(CakehatReleaseInfo.getReleaseDate());
        infoText.append(")</font>");
        infoText.append("<br/><br/>");
        infoText.append("<h3 style='margin-top:0px; margin-bottom:0px;'>Developers</h3>");
        for(int i = 0; i < DEVELOPERS.size(); i++)
        {
            infoText.append(DEVELOPERS.get(i));

            if(i != DEVELOPERS.size() - 1)
            {
                infoText.append(", ");
            }
            if(i == DEVELOPERS.size() - 2)
            {
                infoText.append("& ");
            }
        }
        infoText.append("<br/><br/>");
        infoText.append(CAKEHAT_URL);
        infoText.append("</html>");
        JLabel infoLabel = new JLabel(infoText.toString());
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, this.getFont().getSize()));
        contentPanel.add(infoLabel, BorderLayout.CENTER);

        // Display
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setMinimumSize(this.getSize());

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