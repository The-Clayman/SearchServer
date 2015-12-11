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
    ServerSock ss;
    public static S_ThreadPool s_ThreadPool;
    public static W_ThreadPool w_ThreadPool;
    public static R_ThreadPool r_ThreadPool;
    public static C_ThreadPoolSearch c_ThreadPool;
    public static DataFiles df;
    public static Cache cache;
    public ServerOp(){
        
    }
    public void GO(){
        ss = new ServerSock();
        (new Thread(ss)).start();
        df = new DataFiles(SearchServer.S, SearchServer.L);
        cache = new Cache(SearchServer.C);
        s_ThreadPool = new S_ThreadPool(SearchServer.S);
        r_ThreadPool = new R_ThreadPool(SearchServer.Y);
        w_ThreadPool = new W_ThreadPool(SearchServer.W);
        c_ThreadPool = new C_ThreadPoolSearch(SearchServer.C_num);
        
    }
    
    
}
