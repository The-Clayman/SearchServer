/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import com.sun.jmx.snmp.tasks.Task;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import static searchserver.ServerOp.c_updatePool;
import searchserver.UpdatingList.Mat;

/**
 *
 * @author roy
 */
class ServerSock implements Runnable {

    ServerSocket Ssocket;
    static long allStatisitc = 1;
    static long cacheStatisitcs = 1;
    static long newDBStatisitcs = 1;
    static long DBstatistics = 1;
    static long NumOfupdates;
    static int numOfUsers = 0;

    Vector<conSock> clientSockets;
    final int port = 18524;

    public ServerSock() {

    }

    public static String PrintResult(long time,int name) {
        long all = allStatisitc;
        int cache = (int) (cacheStatisitcs * 100.0 / all + 0.5); //(double)(cacheStatisitcs/allStatisitc);
        int Ndb = (int) (newDBStatisitcs * 100.0 / all + 0.5);
        int db = (int) (DBstatistics * 100.0 / all + 0.5);
        String ans = "";
        ans = "client "+name+"- All: " + all + ", cache: " + cache + "%, newDB: " + Ndb + "%, DB: " + db + "% Time: "+time;
        return ans;
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
        Long startTime;
        boolean connected = true;
        int name = -1;

        public conSock(Socket sock) {
            this.sock = sock;
            this.name = ServerSock.numOfUsers++;
            this.startTime= System.currentTimeMillis(); 
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
                    if (!CheckCon()) {// disconnected, 
                        connected = false;

                        return;
                    }

                    if (in.ready()) {
                        msg = in.readLine();
                        if (msg.compareTo("Close Socket!") == 0) {
                            if (SearchServer.Statistics) {
                                long stopTime = System.currentTimeMillis();
                                System.out.println(ServerSock.PrintResult(stopTime-this.startTime , this.name));
                                
                            }
                            return;
                        }
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
            SearchTask searchTask;
            searchTask = new SearchTask(Integer.parseInt(msg) , this.name);
            ServerOp.s_ThreadPool.enqueue(searchTask);

        }

        public void write(String msg) {
            out.println(msg);
            out.flush();
        }

        public class SearchTask implements Runnable {

            int x;
            int name;
            Integer yCache = -6;
            int zCache = -6;
            final Object lock = new Object();
            int yDB = -6;
            int zDB = -6;

            public SearchTask(int x , int name) {
                this.x = x;
                this.name = name;
            }

            @Override
            public void run() {
                synchronized (this.lock) {

                    try {
                        ServerOp.c_ThreadPool.enqueue(new QueryCacheTask(x, this));
                        this.lock.wait();
                        int Yans = this.yCache;
                        int Zans = this.zCache;
                        if (Yans != -1) {// y exsist in cache. write back answer
                            write(new Integer(Yans).toString());
                            if (SearchServer.debug) {
                                System.out.println("client "+this.name+": cache X:" + x + "=" + Yans);
                            }
                            if (SearchServer.Statistics) {
                                allStatisitc++;
                                cacheStatisitcs++;
                            }
                            // write z++ to data base; chache will updated as well later;  
                            //ServerOp.UP.incrementZ(x, Yans, Zans);

                            // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                            ServerOp.c_updatePool.enqueue(new ZincrementToUpdateTask(x, Yans, Zans, false));
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
                    int yDb = this.yDB;
                    int zDb = this.zDB;
                    if (yDb == -2 || yDb == -3 || yDb == -4) {// x doesnt exsit in db.readers halted until x will be added to db
                        int generatedY = GenY();
                        // lock the file untill new enrty will be written
                        write(new Integer(generatedY).toString());// send Generated Y to client;
                        if (SearchServer.debug) {
                            System.out.println("client "+this.name+": New X:" + x + "=" + generatedY);
                        }
                        ServerOp.w_ThreadPool.enqueue(new writeDBTask(x, generatedY, 1, true));// write generated Y the DB
                        // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                        // ServerOp.c_updatePool.enqueue(new ZincrementToUpdateTask(x, generatedY, zDb, this));
                        if (SearchServer.Statistics) {
                            allStatisitc++;
                            newDBStatisitcs++;
                        }

                        return;
                    }

                    if (yDb != -1 && yDb != -3 && yDb != -4 && yDb != -2) {//y was found in Db. send y back to client
                        write(new Integer(yDb).toString());
                        if (SearchServer.debug) {
                            System.out.println("client "+this.name+": DataBase X:" + x + "=" + yDb);
                        }
                        // write z++ to data base;

                        // ServerOp.w_ThreadPool.enqueue(new writeDBTask(x, returnedY, -5, this , null));//  incremetZ
                        // notify cach; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                        ServerOp.c_updatePool.enqueue(new ZincrementToUpdateTask(x, yDb, zDb, true));
                        if (SearchServer.Statistics) {
                            allStatisitc++;
                            DBstatistics++;
                        }
                        return;
                    }

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
                    int[] ans = ServerOp.cache.QueryX(x);
                    sThread.yCache = ans[0];
                    sThread.zCache = ans[1];
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
                    int ans[] = ServerOp.df.query(x);
                    sThread.yDB = ans[0];
                    sThread.zDB = ans[1];
                    sThread.lock.notify();
                }
            }
        }

        public int GenY() {
            return (int) (Math.random() * (SearchServer.L - 1)) + 1;
        }

    }

    public void initiateUpdates() {
        ServerOp.c_updatePool.enqueue(new writeUpdatesToDBandCache());
    }

    public class writeUpdatesToDBandCache implements Runnable {

        TreeMap<Integer, Mat> dataToUpdateDB;
        TreeMap<Integer, YzSet> dataToUpdateCache;
        UpdatingList localUP;

        public writeUpdatesToDBandCache() {
            this.localUP = ServerOp.UP;
        }

        @Override
        public void run() {

            ServerOp.UP.emptyLists(this);
            if (!dataToUpdateCache.isEmpty()) {
                ServerOp.cache.insertTree(dataToUpdateCache);// locking and unlocking is done by cache.
            }
            dataToUpdateCache = null;
            while (!dataToUpdateDB.isEmpty()) {// updating db;
                Integer key = dataToUpdateDB.firstEntry().getKey();
                int fileNum = dataToUpdateDB.firstEntry().getValue().mat.get(0).getKey() / SearchServer.divitionDataFiles;
                ServerOp.df.lockFile(fileNum);// locking file
                ServerOp.df.writeFromUpdates(dataToUpdateDB.firstEntry().getValue());
                dataToUpdateDB.remove(key);
            }
            dataToUpdateDB = null;
            System.gc();

        }
    }

    public class writeDBTask implements Runnable {

        int x;
        int y;
        int z;
        boolean isPrivilege;

        public writeDBTask(int x, int y, int z, boolean isPrivilege) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.isPrivilege = isPrivilege;
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

    public class ZincrementToUpdateTask implements Runnable {

        int x;
        int y;
        int z;
        boolean fromDB;

        public ZincrementToUpdateTask(int x, int y, int z, boolean fromDB) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.fromDB = fromDB;

        }

        @Override
        public void run() {
            ServerOp.UP.incrementZ(x, y, z, fromDB);

        }
    }

}
