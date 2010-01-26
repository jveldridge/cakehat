/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package matlab;

/**
 *
 * @author spoletto
 */

import java.io.*;
import java.net.*;
 
public class MatlabServer
{
  MatlabControl _controller;

  public MatlabServer() {
	_controller = new MatlabControl();
	try {
		this.setup();
	} catch(IOException e) {
		System.out.println(e.toString());
	}
  }

  public void setup() throws IOException {
	class Caller extends Thread {
		public void run() {
			try {
				listen();
			}
			catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		public void listen() throws IOException {
			ServerSocket sock = null;
			boolean isListening = true;
			try {
				sock = new ServerSocket(4444);
			}
			catch (IOException e) {
				System.out.println(e.toString());
			}
			while(isListening) {
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
			PrintWriter out = new PrintWriter(_sock.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) { //while there are still lines to exec
				processInput(inputLine);
			}
			out.close();
			in.close();
			_sock.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public void processInput(String cmd) {
		try {
			_controller.eval(cmd);
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
	}
  }
}

