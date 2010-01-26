package matlab;

import utils.*;
import java.io.*;
import java.net.*;

/**
 * Client used to communicate with the MATLAB server to send
 * commands to MATLAB. Whenever cakehat needs to send a command
 * to MATLAB, it does so through this class.
 *
 * @author spoletto
 */

public class MatlabClient {

    private Socket _sock;
    private PrintWriter _out;

    public MatlabClient() throws Exception {
        _sock = new Socket("localhost", 4444);
        _out = new PrintWriter(_sock.getOutputStream(), true);
    }

    public void sendCommand(String cmd) {
        _out.println(cmd);
    }

    public void die() {
        try {
            _sock.close();
        } catch (IOException e) {
            new ErrorView(e, "Could not kill client");
        }
        _out.close();
    }
}
