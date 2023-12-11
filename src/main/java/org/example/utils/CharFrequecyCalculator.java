package org.example.utils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import org.example.utils.Constants;

public class CharFrequecyCalculator {
    private FileOperations fileOperations = new FileOperations();
    public HashMap calculateCharFreq(String path, Integer bytes){
        FileReader reader = fileOperations.readFile(path);
        HashMap<String, Long> frequencyMap = new HashMap<String, Long>();
        Long fileSize = fileOperations.getFileSize(path);

        if(bytes > fileSize) bytes = fileSize.intValue();

        Long remainder = fileSize % bytes;
        for(int i = 0; i < fileSize / bytes; i++){
            String string = "";
            for(int j = 0; j < bytes; j++){
                Character character = fileOperations.readByte(reader);
                if(character == null) break;
                string += character;
            }
            frequencyMap.put(string, frequencyMap.getOrDefault(string, 0L) + 1);
        }


        // read the remaining bytes if any
        if(remainder > 0){
            String str = "";
            for(int i = 0; i < remainder; i++){
                Character character = fileOperations.readByte(reader);
                if(character == null) break;
                str += character;
            }
            frequencyMap.put(str, frequencyMap.getOrDefault(str, 0L) + 1);
        }
        return frequencyMap;
    }
}
