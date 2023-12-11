package org.example.utils;

import java.io.File;
import java.io.FileReader;

public class FileOperations {

    public FileReader readFile(String path) {
        FileReader reader = null;
        try{
            reader = new FileReader(path);
        }catch (Exception e){
            System.err.println("File not found");
            System.exit(1);
        }
        return reader;
    }

    public Character readByte(FileReader reader) {
        Character c = null;
        try{
            c = (char) reader.read();
        }catch (Exception e){
            System.err.println("Error reading file");
            System.exit(1);
        }
        return c;
    }

    public long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }
}
