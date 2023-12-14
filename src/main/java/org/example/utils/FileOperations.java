package org.example.utils;

import java.io.*;
import java.util.Arrays;

public class FileOperations {

    public FileInputStream createFileInputStream(String path) {
        try{
            return new FileInputStream(path);
        }catch (FileNotFoundException e) {
            System.err.println("File not found");
            throw new RuntimeException(e);
        }
    }

    public FileOutputStream createFileOutputStream(String path) {
        try{
            return new FileOutputStream(path);
        }catch (FileNotFoundException e) {
            System.err.println("File not found");
            throw new RuntimeException(e);
        }
    }

    public byte[] readNBytes(FileInputStream fileInputStream, Integer chunkLengthInBytes) {
        byte[] chunk = new byte[chunkLengthInBytes];
        try{
            fileInputStream.read(chunk, 0, chunkLengthInBytes);
        }catch (IOException e) {
            System.out.println("Error reading file");
            throw new RuntimeException(e);
        }
        return chunk;
    }

    public void writeByte(FileOutputStream fileOutputStream, byte byteToWrite) {
        try{
            fileOutputStream.write(byteToWrite);
        }catch (IOException e) {
            System.out.println("Error writing to file");
            throw new RuntimeException(e);
        }
    }

    public void closeFileInputStream(FileInputStream fileInputStream) {
        try{
            fileInputStream.close();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeFileOutputStream(FileOutputStream fileOutputStream) {
        try{
            fileOutputStream.close();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getFileSize(String path) { return new File(path).length(); }


}
