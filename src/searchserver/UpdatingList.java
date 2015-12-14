/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchserver.ServerSock.writeUpdatesToDBandCache;
import sun.security.pkcs11.Secmod;

/**
 *
 * @author roy
 */
public class UpdatingList {

    TreeMap<Integer, YzSet> dbList, cacheList;
    long countAllUpdates = 0;
    final Object MainLock;
    private int countUpdates = 0;
    private int updateRoof;
    final Object PrivilegeLock;
    boolean wait = false;

    int chaceLowZ;

    public UpdatingList(int chaceLowZ, int updateFoof) {
        this.dbList = new TreeMap<Integer, YzSet>();
        this.cacheList = new TreeMap<Integer, YzSet>();
        this.chaceLowZ = chaceLowZ;
        this.MainLock = new Object();
        this.PrivilegeLock = new Object();
        this.updateRoof = updateFoof;
    }

    public void incrementZ(int x, int y, int z , boolean fromDB) {
        synchronized (this.MainLock) {
            while (wait) {
                try {
                    MainLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(UpdatingList.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            YzSet temp = dbList.get(x);
            int tempZsize = -1;
            if (temp == null) {// x does not exsist in dbList add new entry (x,y,z=z)
                tempZsize = z + 1;
                YzSet entryToAdd = new YzSet(y, tempZsize);
                dbList.put(x, entryToAdd);
                temp = dbList.get(x);
            } else {// x exsists 
                //temp.yzSet.this.z++;
                temp.z++;
                tempZsize = temp.getZ();
            }
            if (tempZsize > ServerOp.cache.getLowZ() && tempZsize > SearchServer.M) {
                cacheList.put(x, temp);
            }
            MainLock.notify();
            if (fromDB){
               this.countUpdates++;
            }
            if (countUpdates == updateRoof) {// initiate updating procedure
                System.err.println("*********count all updates: " + countAllUpdates + " dbSize: " + dbList.size() + " cache size: " + cacheList.size());
                //    emptyLists();

                ServerOp.ss.initiateUpdates();
                countAllUpdates++;

            }

        }
    }

    public void updateLowZ(int lowZ) {
        this.chaceLowZ = lowZ;
    }

//    public void Unlock() { // for chache to unlock UP after apdates.
//        synchronized (PrivilegeLock) {
//            synchronized (MainLock) {
//                this.wait = false;
//                this.cacheList = new TreeMap<Integer, YzSet>();// asign new tree
//                this.MainLock.notify();
//            }
//        }
//    }
    public int getLowZ() {
        return this.getLowZ();
    }

    public void emptyLists(writeUpdatesToDBandCache wutc) {
        TreeMap<Integer, YzSet> dbCopy;
        synchronized (PrivilegeLock) {

            wait = true;//halt updating until cache will update lowZ, lock will release by cache update tree;
            dbCopy = this.dbList;
            this.dbList = new TreeMap<Integer, YzSet>();// copy data sturctures and asign new stuctures to UP 
            wutc.dataToUpdateCache = this.cacheList;
            this.cacheList = new TreeMap<Integer, YzSet>();
            this.countUpdates = 0;
            synchronized (MainLock) {
                this.wait = false;
                this.MainLock.notify();// unlock
            }
        }
        wutc.dataToUpdateDB = new TreeMap<Integer, Mat>();
        while (!dbCopy.isEmpty()) {
            Entry<Integer, YzSet> temp = dbCopy.firstEntry();
            int fileNumToInsert = ((int) temp.getKey()) / SearchServer.divitionDataFiles;
            Mat mat = wutc.dataToUpdateDB.get(fileNumToInsert);
            if (mat == null) { // mat isn't found, first entry to insert
                mat = new Mat();
                wutc.dataToUpdateDB.put(fileNumToInsert, mat);
            }
            mat.mat.add(temp);
            dbCopy.remove(temp.getKey());
        }
            //    chachUpdateList = cacheList;

        //   this.cacheList.clear();
        this.countUpdates = 0;
    }

    public class Mat {

        ArrayList<Entry<Integer, YzSet>> mat;

        public Mat() {
            mat = new ArrayList<Entry<Integer, YzSet>>();
        }
    }

}
