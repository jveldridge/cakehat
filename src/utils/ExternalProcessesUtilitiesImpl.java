package utils;

public class ExternalProcessesUtilitiesImpl implements ExternalProcessesUtilities
{
    public void executeInVisibleTerminal(String title, String cmd)
    {
        String terminalCmd = "/usr/bin/xterm -fg black -bg gray" +
                " -title " + "\"" + title + "\"" +
                " -e " + "\"" + cmd + "; bash" + "\"";

        BashConsole.writeThreaded(terminalCmd);
    }
}