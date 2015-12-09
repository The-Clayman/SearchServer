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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
class ServerSock implements Runnable {

    ServerSocket Ssocket;
    ThreadPoolWorkers sTherads;// = new ThreadPoolWorkers(S);
    int S;
    
    Vector<conSock> clientSockets;
    final int port = 18524;

    public ServerSock(int S) {
         this.S = S;
         sTherads = new ThreadPoolWorkers(this.S);
    }

    @Override
    public void run() {

        try {
            Ssocket = new ServerSocket(port);

            System.out.println("Server is online, " + Ssocket.toString());
            clientSockets = new Vector<conSock>();
            while (true) {
                Socket socket;
                socket = Ssocket.accept();
                conSock consock = new conSock(socket);
                (new Thread(consock)).start();
                clientSockets.add(consock);
                System.out.println("connection has been accepted: " + socket.toString());

            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                Ssocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class conSock implements Runnable {

        Socket sock;
        PrintWriter out;
        BufferedReader in;
        boolean connected = true;

        public conSock(Socket sock) {
            this.sock = sock;
            try {
                out = new PrintWriter(this.sock.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(this.sock.getInputStream()));
                
            } catch (IOException ex) {
                Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            String msg;
            while (connected){
                try {
                    if (!CheckCon()){
                        connected = false;
                        return;
                    }
                    if (in.ready()){
                        msg = in.readLine();
                        analizeMsg(msg);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

        }

        private boolean CheckCon() {
            if (!this.sock.isConnected()) {
                return false;
            }
            else return true;
        }
        private void analizeMsg(String msg){
          //  StringTokenizer tok = new StringTokenizer(msg);
            System.out.println(msg);
            
        }

    }
    private class SearchTask implements Runnable{

        @Override
        public void run() {
            
        }
        
    }

}
