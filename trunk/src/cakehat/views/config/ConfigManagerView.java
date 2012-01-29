package cakehat.views.config;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author jak2
 */
public class ConfigManagerView extends JFrame
{
    public static void launch(final boolean isSSH)
    {   
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    //For testing
    public static void main(String[] args) throws Throwable
    {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        launch(false);
    }
}