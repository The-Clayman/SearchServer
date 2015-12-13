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
public class YzSet {

    private int y, z;

    public YzSet(int y, int z) {
        this.y = y;
        this.z = z;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public class Lists {

        TreeMap<Integer, YzSet> mapX;

        public Lists() {
            this.mapX = new TreeMap<Integer, YzSet>();
        }

    }
}
