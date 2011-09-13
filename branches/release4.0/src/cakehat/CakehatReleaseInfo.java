package cakehat;

/**
 * All of the information is updated before each release. It is not updated
 * during development.
 *
 * @author jak2
 */
public class CakehatReleaseInfo
{
    public static String getVersion()
    {
        return "3.2 Development";
    }

    public static String getReleaseDate()
    {
        return "unreleased";
    }

    public static String getReleaseCommitNumber()
    {
        return "N/A";
    }
}