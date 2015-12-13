/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.TreeMap;

/**
 *
 * @author roy
 */
public class UpdatingList {
    Lists dbList , cacheList;
    int chaceLowZ;
    public UpdatingList(int chaceLowZ){
        this.dbList = new Lists();
        this.cacheList = new Lists();
        this.chaceLowZ = chaceLowZ;
    }
    public synchronized void incrementZ(int x , int y , int z){
        yzSet temp = dbList.mapX.get(x);
        int tempZsize = -1;
        if (temp == null){// x does not exsist in dbList add new entry (x,y,z=z)
            tempZsize = z +1;
            yzSet entryToAdd = new yzSet(y, tempZsize);
            dbList.mapX.put(x, entryToAdd);
        }
        else{// x exsists 
            temp.zAdding++;
            tempZsize = temp.zAdding;
        }
        if (tempZsize > chaceLowZ){
            cacheList.mapX.put(x, temp);
        }
    }
    public void updateLowZ(int lowZ){
        this.chaceLowZ = lowZ;
    }
    public int getLowZ(){
        return this.getLowZ();
    }
    public void emptyLists(){
        this.cacheList.mapX.clear();
        this.dbList.mapX.clear();
    }
    

    private class Lists {
            TreeMap<Integer, yzSet> mapX;
        public Lists() {
            this.mapX = new TreeMap<Integer, yzSet>();
            
        }
    }
    private class yzSet{
        int y,zAdding;
        public yzSet(int y , int z){
            this.y = y;
            this.zAdding = z;
        }
        
    }
    
}
