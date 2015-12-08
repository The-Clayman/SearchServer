/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
class ServerSock implements Runnable {

    ServerSocket Ssocket;
    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    final int port = 18524;

    public ServerSock() {

    }

    @Override
    public void run() {

        try {
            Ssocket = new ServerSocket(port);
            System.out.println("Server is online, " + Ssocket.toString());
            while (true) {
                
                clientSocket = Ssocket.accept();
                System.out.println("connection has been accepted: "+clientSocket.toString());
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
            }

        } catch (IOException ex) {
            Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
