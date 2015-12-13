/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author roy
 */
public class UpdatingList {
    TreeMap<Integer, YzSet> dbList , cacheList;
     TreeMap<Integer, Mat> DBunloadList = null;
     
    int chaceLowZ;
    public UpdatingList(int chaceLowZ){
        this.dbList = new TreeMap<Integer, YzSet>();
        this.cacheList = new TreeMap<Integer, YzSet>();
        this.chaceLowZ = chaceLowZ;
    }
    public synchronized void incrementZ(int x , int y , int z){
        YzSet temp = dbList.get(x);
        int tempZsize = -1;
        if (temp == null){// x does not exsist in dbList add new entry (x,y,z=z)
            tempZsize = z +1;
            YzSet entryToAdd = new YzSet(y, tempZsize);
            dbList.put(x, entryToAdd);
        }
        else{// x exsists 
            //temp.yzSet.this.z++;
            temp.setZ(temp.getZ()+1);
            tempZsize = temp.getZ();
        }
        if (tempZsize > chaceLowZ){
            cacheList.put(x, temp);
        }
    }
    public void updateLowZ(int lowZ){
        this.chaceLowZ = lowZ;
    }
    public int getLowZ(){
        return this.getLowZ();
    }
    public void emptyLists(){
        DBunloadList = new TreeMap<Integer,Mat>();
        while (!this.dbList.isEmpty()){
            Entry temp = this.dbList.firstEntry();
            int fileNumToInsert =((int) temp.getKey())/SearchServer.divitionDataFiles;
            Mat mat = DBunloadList.get(fileNumToInsert);
            if (mat == null){ // mat isn't found, first entry to insert
                mat = new Mat();
            }
            mat.mat.add(temp);
            this.dbList.remove(temp.getKey());
        }
        
        
            
        
        
        this.cacheList.clear();
        this.dbList.clear();
    }
    

   
    

    public class Mat{
        ArrayList <Entry>mat;
        public Mat(){
            mat = new ArrayList<Entry>();
        } 
    }

    
}
