/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.logging.Handler;
import java.util.logging.LogRecord;



/**
 *
 * @author roy
 */
public class SearchServer {
    
    static private int S = 10;
    static private int C = 40;
    static private int M = 5;
    static private int L = 100;
    static private int Y = 10;

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
        ServerSock ss = new ServerSock(S);
        (new Thread(ss)).start();

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
    
    private final Handler handler = new Handler() {

        @Override
        public void publish(LogRecord record) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    };

   

}
