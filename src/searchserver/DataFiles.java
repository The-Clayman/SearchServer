/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
public class DataFiles {

    int divitionFactor;
    ArrayList<RandomAccessFile> files;
    int l;

    public DataFiles(int divitionFactor, int l) {
        this.divitionFactor = divitionFactor;
        this.files = new ArrayList<RandomAccessFile>();
        this.l = l;
    }

    public int query(int x) {
        int ans = -1;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int readX;
        int readY;
        int readZ;

        RandomAccessFile file = null;
        try {
       //     testFill(); //TEST           $%^$%^$%^$%^%$^$%^
            file = files.get(fileNum);
            if (file == null) {
                RandomAccessFile emptyFileToAsign = new RandomAccessFile(fileNum + ".dat", "rw");
                fillFileJunk(emptyFileToAsign);
            }
        } catch (IndexOutOfBoundsException e) {
            try {
                // file doesn't exsist create a file
                RandomAccessFile random = new RandomAccessFile(fileNum + ".dat", "rw");
                fillFileJunk(random);
                if (files.size() <= fileNum) {// add missing spaces in fiels. empty one will be filled with null;
                    for (int i = files.size(); i <= fileNum; i++) {
                        files.add(null);
                    }
                }
                files.set(fileNum, random);
                return query(x);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            int pointer = location * 4 * 3;
            file.seek(pointer);
            readX = file.readInt();
            readY = file.readInt();
            readZ = file.readInt();
            if (readY == -1) {// if query doesn't exsist in DB, send random num
                ans = (int)(Math.random()*(l-1))+1;
                //                                     needs to sends write request!;
            }
            else{
            ans = readY;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ans;
    }

    private void fillFileJunk(RandomAccessFile fileToFill) {
        for (int i = 0; i < divitionFactor * 3; i++) {
            try {
                fileToFill.writeInt(-1);
            } catch (IOException ex) {
                Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void testFill() {
        try {
            RandomAccessFile random = new RandomAccessFile("0.dat", "rw");
            files.add(random);
            for (int i = 0; i < 10; i++) {
                random.writeInt(i);
                random.writeInt(i + 1);
                random.writeInt(1);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
