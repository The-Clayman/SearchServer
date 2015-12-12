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
import java.util.concurrent.CountDownLatch;
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
            Integer yCache = -6;
            final Object lock = new Object();
            Object yDB = -6;

            public SearchTask(int x) {
                this.x = x;
            }

            @Override
            public void run() {

//                int ans = ServerOp.df.query(x);
//                write(new Integer(ans).toString());
                synchronized (this.lock) {

                    try {
                        ServerOp.c_ThreadPool.enqueue(new QueryCacheTask(x, this));
                        this.lock.wait();
                        int ans = this.yCache;
                        if (ans != -1) {// y exsist in cache. write back answer
                            write(new Integer(ans).toString());
                            // write z++ to data base; chache will updated as well later;
                          
                            
                            ServerOp.w_ThreadPool.enqueue(new writeDBTask(x, ans, -5, this , null));// -5 means incremetZ and updating cashe
                            
                            
                            
                            // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                            return;
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                // y doesn't exsist in cach. search DB

                synchronized (this.lock) {
                    ServerOp.r_ThreadPool.enqueue(new readDBTask(x, this));
                    try {
                        this.lock.wait();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerSock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Object object = this.yDB;
                    if (object instanceof CountDownLatch) {// x doesnt exsit in db. halt readers until x will be added to db
                        int genratedY = GenY();
                        CountDownLatch latch = (CountDownLatch) object;
                        // lock the file untill new enrty will be written
                        write(new Integer(genratedY).toString());
                        ServerOp.w_ThreadPool.enqueue(new writeDBTask(x, genratedY, 2, this , latch));// write new y the DB
                        // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                        
                        
                        return;
                    }

                    int returnedY = (Integer) object;

                    if (returnedY != -1 && returnedY != -3 && returnedY != -4) {//y was found in Db. send y back to client
                        write(new Integer(returnedY).toString());
                        // write z++ to data base;
                        
                        
                       // ServerOp.w_ThreadPool.enqueue(new writeDBTask(x, returnedY, -5, this , null));//  incremetZ
                        
                        
                        // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                        return;
                    }
                    // y not found, generate y, send it back;

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
                synchronized (sThread.lock) {
                    int ans = ServerOp.cache.QueryX(x);
                    sThread.yCache = ans;
                    sThread.lock.notify();
                }
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
                synchronized (sThread.lock) {
                    Object ans = ServerOp.df.query(x);
                    sThread.yDB = ans;// return latch if new entry needed to be added
                    sThread.lock.notify();
                }
            }
        }

        public class writeDBTask implements Runnable {

            int x;
            int y;
            int z;
            SearchTask sThread;
            CountDownLatch latch;

            public writeDBTask(int x, int y, int z, SearchTask sThread , CountDownLatch latch) {
                this.x = x;
                this.sThread = sThread;
                this.y = y;
                this.z = z;
                this.latch = latch;
            }

            @Override
            public void run() {
                if (z == -5) {// increment Z
                    ServerOp.df.incrementZ(x);
                    return;
                }// write new qwery to database
                ServerOp.df.writeNewEntry(x, y);
                if (this.latch != null){
                    System.out.println("latch free\n"+latch);
                    latch.countDown();
                    System.out.println("latch free\n"+latch);
                }

            }
        }

        public int GenY() {
            return (int) (Math.random() * (SearchServer.L - 1)) + 1;
        }

    }

}
