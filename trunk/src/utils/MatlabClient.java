/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.io.*;
import java.net.*;

/**
 *
 * @author spoletto
 */
 public class MatlabClient {

        private Socket _sock;
        private PrintWriter _out;

        public MatlabClient() {
            _sock = null;
            _out  = null;
             try {
                    _sock = new Socket("localhost", 4444);
                    _out = new PrintWriter(_sock.getOutputStream(), true);
                } catch (UnknownHostException e) {
                new ErrorView(e, "Could not bind socket to localhost"); }
                catch (IOException e) {
                new ErrorView(e, "Could not bind socket to localhost"); }
        }

        public void sendCommand(String cmd) {
            _out.println(cmd);
        }

        public void die() {
            try {
                _sock.close();
            }
            catch (IOException e) {
                new ErrorView(e, "Could not kill client");
            }
            _out.close();
        }


    }
