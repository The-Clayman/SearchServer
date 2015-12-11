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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
class ServerSock implements Runnable {

    ServerSocket Ssocket;

    Vector<conSock> clientSockets;
    final int port = 18524;

    public ServerSock() {

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
            while (connected) {
                try {
                    if (!CheckCon()) {
                        connected = false;
                        return;
                    }
                    if (in.ready()) {
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
            } else {
                return true;
            }
        }

        private void analizeMsg(String msg) {
            //  StringTokenizer tok = new StringTokenizer(msg);
            System.out.println("x-" + msg + "received");
            SearchTask searchTask;
            searchTask = new SearchTask(Integer.parseInt(msg));
            ServerOp.s_ThreadPool.enqueue(searchTask);

        }

        public void write(String msg) {
            out.println(msg);
            out.flush();
        }

        public class SearchTask implements Runnable {

            int x;
            Integer yCache;
            Integer yDB;

            public SearchTask(int x) {
                this.x = x;
            }

            @Override
            public void run() {

//                int ans = ServerOp.df.query(x);
//                write(new Integer(ans).toString());
                synchronized (this.yCache) {
                    ServerOp.c_ThreadPool.enqueue(new QueryCacheTask(x, this));
                    try {
                        this.yCache.wait();
                        int ans = this.yCache;
                        if (ans != -1) {// y exsist in cache. write back answer
                            write(new Integer(ans).toString());
                            // write z++ to data base; chache will updated as well later;
                            ServerOp.w_ThreadPool.enqueue(new writeDBTask(x,ans , -5 , this ));// -5 means incremetZ and updating cashe
                            // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                            return;
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                // y doesn't exsist in cach. search DB

                synchronized (this.yDB) {
                    ServerOp.r_ThreadPool.enqueue(new readDBTask(x, this));
                    try {
                        this.yDB.wait();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int returnedY = this.yDB;
                    if (returnedY != -1 && returnedY != -3 && returnedY != -4) {//y was found in Db. send y back to client
                        write(new Integer(this.yDB).toString());
                        // write z++ to data base;
                        ServerOp.w_ThreadPool.enqueue(new writeDBTask(x,returnedY , -5 , this ));// -5 means incremetZ
                        // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                        return;
                    }
                    // y not found, generate y, send it back;
                    int genratedY = GenY();
                    write(new Integer(genratedY).toString());
                    ServerOp.w_ThreadPool.enqueue(new writeDBTask(x,genratedY , 2 , this ));// write new y the DB
                    // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                    
                }

            }
        }

        public class QueryCacheTask implements Runnable {

            int x;
            SearchTask sThread;

            public QueryCacheTask(int x, SearchTask sThread) {
                this.x = x;
                this.sThread = sThread;
            }

            @Override
            public void run() {

                int ans = ServerOp.cache.QueryX(x);
                sThread.yCache = ans;
                sThread.yCache.notify();

            }
        }

        public class readDBTask implements Runnable {

            int x;
            SearchTask sThread;

            public readDBTask(int x, SearchTask sThread) {
                this.x = x;
                this.sThread = sThread;
            }

            @Override
            public void run() {

                int ans = ServerOp.df.query(x);
                sThread.yDB = ans;
                sThread.yDB.notify();

            }
        }

        public class writeDBTask implements Runnable {

            int x;
            int y;
            int z;
            SearchTask sThread;

            public writeDBTask(int x, int y, int z, SearchTask sThread) {
                this.x = x;
                this.sThread = sThread;
                this.y = y;
                this.z = z;
            }

            @Override
            public void run() {
                if (z == -5) {// increment Z
                    ServerOp.df.incrementZ(x);
                    return;
                }// write new qwery to database
                ServerOp.df.writeNewEntry(x, y);
                

            }
        }

        public int GenY() {
            return (int) (Math.random() * (SearchServer.L - 1)) + 1;
        }

    }

}
