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
                return -3; // -3 means x doesn't exist
            }
        } catch (IndexOutOfBoundsException e) {
            return -3;// -3 means x doesn't exist

        }
        try {
            int pointer = location * 4 * 3;
            file.seek(pointer);
            readX = file.readInt();
            readY = file.readInt();
            if (x != readX){
                System.err.println("error ecore DataBase.query(int x), x!=readX");
                return -4;
            }
            if (readY == -1) {// if query doesn't exsist in DB
                return -3;
            } else {// send back y
                ans = readY;
            }

        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ans;
    }

    public void writeNewEntry(int x, int y) {
        RandomAccessFile file = null;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int pointer = location * 4 * 3;
        try {
            //     testFill(); //TEST           $%^$%^$%^$%^%$^$%^
            file = files.get(fileNum);
            if (file == null) {// if pointer exist, buy is null
                RandomAccessFile emptyFileToAsign = new RandomAccessFile(fileNum + ".dat", "rw");
                fillFileJunk(emptyFileToAsign);
                files.set(fileNum, emptyFileToAsign);
            }
            // write new entry in position
            file.seek(pointer);
            file.writeInt(x);
            file.writeInt(y);
            file.writeInt(1);

        } catch (IndexOutOfBoundsException e) {
            try {
                // file doesn't exsist and files isn't full with files, create a file
                RandomAccessFile random = new RandomAccessFile(fileNum + ".dat", "rw");
                fillFileJunk(random);
                if (files.size() <= fileNum) {// add missing spaces in fiels. empty one will be filled with null;
                    for (int i = files.size(); i <= fileNum; i++) {
                        files.add(null);
                    }
                }
                files.set(fileNum, random);
                file.seek(pointer);
                file.writeInt(x);
                file.writeInt(y);
                file.writeInt(1);
                return;

            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void incrementZ(int x) {
        RandomAccessFile file = null;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int pointer = location * 4 * 3;
        int readX = -2;
        int readZ;
        file = files.get(fileNum);
        try {
            file.seek(pointer);
            readX = file.readInt();
            if (readX != x) {
                // does not match!
                System.err.println("Error ecore DataBase.incrementZ(int x), readX != x");
            }
            file.seek(pointer*4*2);
            readZ = file.readInt();
            file.seek(pointer*4*2);
            file.writeInt(readZ+1);
            

        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

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
