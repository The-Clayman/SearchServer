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

    public Object query(int x) {
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
                return -3; // -3 means x doesn't exist

            }
        } catch (IndexOutOfBoundsException e) {
//            RandomAccessFile random = new RandomAccessFile(fileNum + ".dat", "rw");
//            fillFileJunk(random);
//            if (files.size() <= fileNum) {// add missing spaces in fiels. empty one will be filled with null;
//                for (int i = files.size(); i <= fileNum; i++) {
//                    files.add(null);
//                }
//            }
//            files.add(new FileLockSet(random));
            for (int i = files.size(); i <= fileNum; i++) {
                RandomAccessFile random;
                try {
                    random = new RandomAccessFile(i + ".dat", "rw");

                    fillFileJunk(random);
                    FileLockSet fileToAdd = new FileLockSet(random);
                    files.add(fileToAdd); //00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                    return fileToAdd.latch;// sends back latch. for realeasing latch after writing missing x
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DataFiles.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        try {

            file.latch.await();// stops all reading threads incase new entry needed to be added

            synchronized (file.lock) {
                int pointer = location * 4 * 3;
                file.file.seek(pointer);
                readX = file.file.readInt();
                readY = file.file.readInt();

//            if (x != readX) {
//            //    System.err.println("error ecore DataBase.query(int x), x!=readX");
//                return -4;
//            }
                if (readY == -1) {// if query doesn't exsist in DB
                    // halt all readers
                    if (file.latch.getCount() == 0) {
                        file.latch = new CountDownLatch(1);
                    }
                    return file.latch;
                } else {// send back y
                    ans = readY;
                    //file.latch.countDown();// free the latch. now need to write new entry;
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
            synchronized (file.lock) {
                // write new entry in position
                file.file.seek(pointer);
                file.file.writeInt(x);
                file.file.writeInt(y);
                file.file.writeInt(1);
                if (file.latch != null) {
                    file.latch.countDown();
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
//        if (file.latch != null)
//        file.latch.countDown();
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
            synchronized (file.lock) {
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
        Object lock;
        CountDownLatch latch;

        private FileLockSet(RandomAccessFile file) {
            this.file = file;
            lock = new Object();
            latch = new CountDownLatch(1);
        }
    }

}
