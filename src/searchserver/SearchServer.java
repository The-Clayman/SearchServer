/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;



/**
 *
 * @author roy
 */
public class SearchServer {
    
    public static int S = 10;// S - number of allowed S-threads.
    public static int C = 70; // C - size of the cache
    public static int M = 50; // M - the least number of times a query has to be requested in order to be allowed to enter the cache.
    public static int L = 100;// L - to specify the range [1, L] from which missing replies will be drawn uniformly at random.
    public static int Y = 10; // Y - number of reader threads.
    public static int divitionDataFiles = S;
    public static int Read_Thread_Workers_Num = 5;
    public static int Cache_workers_num = 10;
    public static int updatingWorkNum = 10;
    public static int upDatingroof = C*M;
    public static boolean debug = false;
    public static boolean Statistics = true;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args == null || args.length < 4) {
            help();
        } else {
            parseArgs(args);
        }
        
        //ServerSock ss = new ServerSock();
        ServerOp op = new ServerOp();
        op.GO();
        

    }

    private static void parseArgs(String[] args) {
        S = new Integer(args[0]);
        M = new Integer(args[1]);
        L = new Integer(args[2]);
        Y = new Integer(args[3]);

    }

    private static void help() {
        System.out.println("Wrong Parameters! should use: java -jar SearchServer <sTheardNum> <C sizeOfCatch> <M least num for catch> <L range mising replies> <Y num readingThreads>");
        System.out.println("Using defalt: java -jar All_Cliques 10, 40, 5, 100, 10");
    }
}
