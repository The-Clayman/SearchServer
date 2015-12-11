/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
public class W_ThreadPool {
    
 private WorkerThread[] threads;
    private LinkedList<Runnable> taskQueue;
    
    public W_ThreadPool(int threadNumber){
        taskQueue = new LinkedList<Runnable>();
        threads = new WorkerThread[threadNumber];
        for (int i = 0; i < threads.length ; i++){
            threads[i] = new WorkerThread();
            threads[i].start();
        }
        
    }
    public void enqueue(Runnable r){
        synchronized(taskQueue){
            taskQueue.addLast(r);
            taskQueue.notify();
        }
    }
    
    
   private class WorkerThread extends Thread {
       public void run(){
           Runnable r;
           while (true){
               synchronized(taskQueue){
                   while (taskQueue.isEmpty()){
                       try {
                           taskQueue.wait();
                       } catch (InterruptedException ex) {
                           Logger.getLogger(ThreadPoolWorkers.class.getName()).log(Level.SEVERE, null, ex);
                       }
                   }
                   r = (Runnable) taskQueue.removeFirst();
               }
               r.run();
           }
       }
        
    }
  
}
