package cakehat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * All of the information is updated before each release. It is not updated during development. Release information
 * is stored in the manifest file. The manifest file, manifest.mf, is located in the root directory of cakehat.
 *
 * @author jak2
 */
public class CakehatReleaseInfo
{
    /**
     * Keys correspond to values in the manifest file. 
     */
    private static final String VERSION_KEY = "cakehat-Version",
                                RELEASE_DATE_KEY = "cakehat-Release-Date",
                                RELEASE_COMMIT_NUMBER_KEY = "cakehat-Release-Commit-Number";
    
    /**
     * The values corresponding to the keys.
     */
    private static final String VERSION_VALUE, RELEASE_DATE_VALUE, RELEASE_COMMIT_NUMBER_VALUE;
    static
    {
        String versionValue = null, releaseDateValue = null, releaseCommitNumberValue = null;
        
        try
        {
            //Determine location of cakehat
            URL url = CakehatReleaseInfo.class.getProtectionDomain().getCodeSource().getLocation();
            File codeLocation = new File(url.toURI().getPath()).getCanonicalFile();
            
            //Load the manifest file
            Manifest manifest;
            
            //Running inside of jar - read the manifest file from the jar file
            if(codeLocation.toString().endsWith(".jar"))
            {
                manifest = new JarFile(codeLocation).getManifest();
            }
            //Running from .class files - read the manifest file directly
            else
            {
                //Manifest file is located in a directory two levels above the root of the compiled code
                File manifestFile = new File(codeLocation.getParentFile().getParentFile(), "manifest.mf");
                manifest = new Manifest(new FileInputStream(manifestFile));
            }

            Attributes manifestAttributes = manifest.getMainAttributes();
            versionValue = manifestAttributes.getValue(VERSION_KEY);
            releaseDateValue = manifestAttributes.getValue(RELEASE_DATE_KEY);
            releaseCommitNumberValue = manifestAttributes.getValue(RELEASE_COMMIT_NUMBER_KEY);
        }
        //If an issue is encountered while reading this information then the default Unknown will be used
        catch(IOException e) { }
        catch(URISyntaxException e) { }
        
        VERSION_VALUE = versionValue == null ? "Unknown" : versionValue;
        RELEASE_DATE_VALUE = releaseDateValue == null ? "Unknown" : releaseDateValue;
        RELEASE_COMMIT_NUMBER_VALUE = releaseCommitNumberValue == null ? "Unknown" : releaseCommitNumberValue;
    }
    
    public static String getVersion()
    {   
        return VERSION_VALUE;
    }

    public static String getReleaseDate()
    {
        return RELEASE_DATE_VALUE;
    }

    public static String getReleaseCommitNumber()
    {
        return RELEASE_COMMIT_NUMBER_VALUE;
    }
}