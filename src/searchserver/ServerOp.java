/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

/**
 *
 * @author roy
 */
public class ServerOp {

    
    public static ServerSock ss;
    public static ThreadpoolWork s_ThreadPool;
    public static ThreadpoolWork w_ThreadPool;
    public static ThreadpoolWork r_ThreadPool;
    public static ThreadpoolWork c_ThreadPool;
    public static ThreadpoolWork c_updatePool;
    public static DataFiles df;
    public static Cache cache;
    public static UpdatingList UP;
    public ServerOp(){
        
    }
    public void GO(){
        
        df = new DataFiles(SearchServer.S, SearchServer.L);
        cache = new Cache(SearchServer.C);
        s_ThreadPool = new ThreadpoolWork(SearchServer.S);
        r_ThreadPool = new ThreadpoolWork(SearchServer.Y);
        w_ThreadPool = new ThreadpoolWork(SearchServer.Read_Thread_Workers_Num);
        c_ThreadPool = new ThreadpoolWork(SearchServer.Cache_workers_num);
        c_updatePool = new ThreadpoolWork(SearchServer.updatingWorkNum);
        UP = new UpdatingList(SearchServer.M ,SearchServer.upDatingroof);
        ss = new ServerSock();
        (new Thread(ss)).start();
    }
    
    
}
