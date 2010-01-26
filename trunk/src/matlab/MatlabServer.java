package matlab;

import java.io.*;
import java.net.*;
import utils.ErrorView;

/**
 * The MATLAB server is launched from within MATLAB. It listens
 * for commands from the MATLAB client to forward requests off
 * to MATLAB. This must happen because the java application
 * sending commands to MATLAB must be in the same java virtual
 * machine as MATLAB.
 *
 * @author spoletto
 */

public class MatlabServer {

    MatlabControl _controller;

    public MatlabServer() {
        _controller = new MatlabControl();
        try {
            this.setup();
        } catch (IOException e) {
            new ErrorView(e, "Could not create MATLAB server");
        }
    }

    public void setup() throws IOException {
        class Caller extends Thread {

            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    new ErrorView(e, "Could not create MATLAB server");
                }
            }

            public void listen() throws IOException {
                ServerSocket sock = null;
                boolean isListening = true;
                try {
                    sock = new ServerSocket(4444);
                } catch (IOException e) {
                    new ErrorView(e, "Could not create MATLAB server socket");
                }
                while (isListening) {
                    new ControlThread(sock.accept()).run();
                }
                sock.close();
            }
        }
        
        Caller c = new Caller();
        c.start();
    }

    public class ControlThread {

        private Socket _sock = null;

        public ControlThread(Socket socket) {
            _sock = socket;
        }

        public void run() {
            try {
                //PrintWriter out = new PrintWriter(_sock.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) { //while there are still lines to exec
                    processInput(inputLine);
                }
                //out.close();
                in.close();
                _sock.close();
            } catch (IOException e) {
                new ErrorView(e, "Error occurred while listening for MATLAB requests");
            }
        }

        public void processInput(String cmd) {
            try {
                _controller.eval(cmd);
            } catch (Exception e) {
                new ErrorView(e, "Error occurred while evaluating MATLAB command");
            }
        }
    }
}

