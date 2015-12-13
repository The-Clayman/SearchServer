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
public class Cache {

    int sizeMax;
    myTree tree;
    int lowZ = 0;

    public Cache(int size) {
        this.sizeMax = size;
        tree = new myTree(size);
    }

    public void insert(int x, int y, int z) {
        tree.insert(x, y, z);
    }

    public int[] QueryX(int x) {
        return tree.getYbyX(x);
    }

    private class myTree {

        TreeMap<Integer, set> mapX;
        TreeMap<Integer, ArrayList<Integer>> mapZ;
        int size;

        public myTree(int size) {
            mapX = new TreeMap<Integer, set>();
            mapZ = new TreeMap<Integer, ArrayList<Integer>>();
            this.size = size;
        }

        public int getLowZValue() {
            return this.mapZ.firstKey();
        }

        public void insert(int x, int y, int z) {
            if (mapX.size() == size && mapX.get(x) == null) {// if full and a not updating exsiting set
                if (z <= this.getLowZValue()) {
                    return; // The cache is full, the new entry has z<= from lowest z value in cache. do nothing
                }
                removeLowZ();// removes one low Z elemnt
            }

            if (mapX.get(x) == null) {// x does not exsist yet
                mapX.put(x, new set(y, z));
                addXtoZ(x, z);

            } else {// x exsist, override, update mapZ
                if (mapX.get(x).getY() == y) {
                    int delZ = mapX.get(x).getZ();
                    mapX.get(x).setZ(z);
                    int XvectorIndex = mapZ.get(delZ).indexOf(x);
                    if (mapZ.get(delZ).size() == 1) {// ArrayList contains only one value. remove all node
                        mapZ.remove(delZ);
                        addXtoZ(x, z);// add X to the new Z value
                    } else {// ArrayList contains more than one vlaue. remove X from Vector
                        mapZ.get(delZ).remove(XvectorIndex);
                        addXtoZ(x, z);
                    }

                } else {
                    //error, y does not match
                    System.err.println("cache Error:insert, x exsist, y does not match old Y ");
                }
            }

        }

        private void addXtoZ(int x, int z) {
            ArrayList<Integer> temp;
            if (mapZ.get(z) == null) {// z size does not exsist. adds new z;
                temp = new ArrayList<Integer>();
                temp.add(x);
                mapZ.put(z, temp);
            } else {//z size exsists
                mapZ.get(z).add(x);
            }
        }

        private void removeLowZ() {
            Entry<Integer, ArrayList<Integer>> del = mapZ.firstEntry();
            if (del.getValue().size() == 1) {// only one x in this Z size, remove all Z entry
                //int Xdel = del.getValue().firstElement();
                int Xdel = del.getValue().get(0);
                mapZ.remove(del.getKey());
                mapX.remove(Xdel);

            } else {// more than one x in this Z size, remove first X
                int Xdel = del.getValue().get(0);
                mapZ.firstEntry().getValue().remove(0);
                //  del.getValue().remove(Xdel);
                mapX.remove(Xdel);
            }
        }

        private int[] getYbyX(int x) {
            set returnAns = mapX.get(x);
            int[] ans = new int[2];
            if (returnAns == null) {
                ans[0] = -1;
                return ans;
            }
            ans[0] = returnAns.y;
            ans[1] = returnAns.z;
            return ans;
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
