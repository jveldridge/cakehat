package cakehat.icon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import support.resources.icons.IconLoader;

/**
 * Loads the cakehat icon in a variety of size.
 *
 * @author jak2
 */
public class CakehatIconLoader
{
    public static enum IconSize
    {
        s100x100("cakehat_100.jpg"),
        s200x200("cakehat_200.jpg"),
        s300x300("cakehat_300.jpg"),
        s400x400("cakehat_400.jpg"),
        s500x500("cakehat_500.jpg"),
        s600x600("cakehat_600.jpg");
        
        private final String _fileName;
        
        private IconSize(String fileName)
        {
            _fileName = fileName;
        }
    }
    
    private static final String ROOT_ICON_DIR = "/" + CakehatIconLoader.class.getPackage().getName().replace(".", "/");
    
    private CakehatIconLoader() { }
    
    public static BufferedImage loadBufferedImage(IconSize size) throws IOException
    {
        return ImageIO.read(getResourceURL(size));
    }

    public static Icon loadIcon(IconSize size)
    {
        return new ImageIcon(getResourceURL(size));
    }

    private static URL getResourceURL(IconSize size)
    {
        String resourcePath = ROOT_ICON_DIR + "/" + size._fileName;
        URL resourceURL = IconLoader.class.getResource(resourcePath);

        return resourceURL;
    }
}