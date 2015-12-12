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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roy
 */
public class DataFiles {

    int divitionFactor;
    ArrayList<FileLockSet> files;
    int l;

    public DataFiles(int divitionFactor, int l) {
        this.divitionFactor = divitionFactor;
        this.files = new ArrayList<FileLockSet>();
        this.l = l;
    }

    public int query(int x) {
        int ans = -1;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int readX;
        int readY;
        int readZ;

        FileLockSet file = null;
        try {
            //     testFill(); //TEST           $%^$%^$%^$%^%$^$%^
            file = files.get(fileNum);
            if (file == null) {
                System.err.println("query(int x): file " + fileNum + " is null");
                return -2; // -2 means x doesn't exist

            }
        } catch (IndexOutOfBoundsException e) {
            FileLockSet fileToAdd;
            for (int i = files.size(); i <= fileNum; i++) {
                RandomAccessFile random;
                try {
                    random = new RandomAccessFile(i + ".dat", "rw");

                    fillFileJunk(random);
                    fileToAdd = new FileLockSet(random);
                    files.add(fileToAdd); //00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                    if (i == fileNum){
                        fileToAdd.wait = true;// lock reading until new entry will be written
                    }
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            return -3;// file not found. return -3 for anitiate writing mising entry
        }
        try {
            synchronized (file.mainLock) {
                while (file.wait) {
                    file.mainLock.wait();// block all reading threads if there's a writing task needed to be done
                }
                int pointer = location * 4 * 3;
                file.file.seek(pointer);
                readX = file.file.readInt();
                readY = file.file.readInt();

                if (readY == -1) {// if query doesn't exsist in DB
                    // halt all readers
                    file.wait = true;
                    return -4;
                    }
                    
                 else {// send back y
                    ans = readY;
                    file.mainLock.notify();// wake up next thread
                    return readY;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        //   file.latch.countDown();
        return ans;
    }

    public void writeNewEntry(int x, int y) {
        FileLockSet file = null;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int pointer = location * 4 * 3;
        try {
            //     testFill(); //TEST           $%^$%^$%^$%^%$^$%^
            file = files.get(fileNum);
            if (file == null) {// if pointer exist, but is null
//                RandomAccessFile emptyFileToAsign = new RandomAccessFile(fileNum + ".dat", "rw");
//                fillFileJunk(emptyFileToAsign);
//                files.set(fileNum, new FileLockSet(emptyFileToAsign));
                System.err.println("writeNewEntry(int x, int y): file " + fileNum + " is null");
                return;
            }
            synchronized (file.privilegeLock) {
                synchronized (file.mainLock) {
                    // write new entry in position
                    file.file.seek(pointer);
                    file.file.writeInt(x);
                    file.file.writeInt(y);
                    file.file.writeInt(1);

                    file.wait = false;
                    file.mainLock.notify();// writing complite, release lock for read threads
                }

               

            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("writeNewEntry(int x, int y): file " + fileNum + " out of bounds");
            return;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void incrementZ(int x) {
        FileLockSet file = null;
        int fileNum = x / divitionFactor;
        int location = x % divitionFactor;
        int pointer = location * 4 * 3;
        int readX = -2;
        int readZ;
        file = files.get(fileNum);
        try {
            synchronized (file.mainLock) {
                file.file.seek(pointer);
                readX = file.file.readInt();
                if (readX != x) {
                    // does not match!
                    System.err.println("Error ecore DataBase.incrementZ(int x), readX != x");
                }
                file.file.seek(pointer * 4 * 2);
                readZ = file.file.readInt();
                file.file.seek(pointer * 4 * 2);
                file.file.writeInt(readZ + 1);

            }
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
            FileLockSet random = new FileLockSet(new RandomAccessFile("0.dat", "rw"));
            files.add(random);
            for (int i = 0; i < 10; i++) {
                random.file.writeInt(i);
                random.file.writeInt(i + 1);
                random.file.writeInt(1);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class FileLockSet {

        RandomAccessFile file;
        Object mainLock;
        Object privilegeLock;
        boolean wait;
        //      CountDownLatch latch;

        private FileLockSet(RandomAccessFile file) {
            this.file = file;
            mainLock = new Object();
            privilegeLock = new Object();
            wait = false;
            //      latch = new CountDownLatch(1);
        }
    }

}
