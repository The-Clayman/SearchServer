/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author roy
 */
public class Cache {

    public Cache(int size) {

    }

    public class myTree {

        TreeMap<Integer, set> mapX;
        TreeMap<Integer, Stack<Integer>> mapZ;
        int size;

        public myTree(int size) {
            mapX = new TreeMap<Integer, set>();
            mapZ = new TreeMap<Integer, Stack<Integer>>();
            this.size = size;
        }

        public void insert(int x, int y, int z) {
            if (mapX.size() == size) {// if full
                rempveLowZ();// removes one low Z elemnt
            }

            if (mapX.get(x) == null) {// x does not exsist yet
                mapX.put(x, new set(y, z));
                addXtoZ(x,z);

            } else {// x exsist, override, update mapZ
                if (mapX.get(x).getY() == y) {
                    int delZ = mapX.get(x).getZ();
                    mapX.get(x).setZ(z);
                    mapZ.get(delZ).remove(x);
                    addXtoZ(x,z);

                } else {
                    //error, y does not match
                }
            }

        }

        private void addXtoZ(int x, int z) {
            Stack<Integer> temp;
            if (mapZ.get(z) == null) {// z size does not exsist. adds new z;
                temp = new Stack<Integer>();
                temp.add(x);
                mapZ.put(z, temp);
            }
            else{//z size exsists
                mapZ.get(z).add(x); 
            }
        }

        private void rempveLowZ() {
            Entry<Integer, Stack<Integer>> del = mapZ.firstEntry();
            if (del.getValue().size() == 1) {// only one x in this Z size, remove all Z entry
                int Xdel = del.getValue().firstElement();
                mapZ.remove(del.getKey());
                mapX.remove(Xdel);
            } else {// more than one x in this Z size, remove first X
                int Xdel = del.getValue().pop();
                mapX.remove(Xdel);
            }
        }

        private int getYbyX(int x) {
            return mapX.get(x).getY();
        }
    }

    private class set {

        private int y, z;

        public set(int y, int z) {
            this.y = y;
            this.z = z;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }

}
