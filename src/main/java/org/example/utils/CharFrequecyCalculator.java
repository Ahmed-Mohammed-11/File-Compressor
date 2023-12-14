package org.example.utils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class CharFrequecyCalculator {
    private final FileOperations fileOperations = new FileOperations();
    public HashMap calculateCharFreq(String path, Integer chunkLengthInBytes) {
        FileInputStream fileInputStream = fileOperations.createFileInputStream(path);
        //using Bytebuffer to be able to use it as a key in the hashmap (byte[] can't be used as a key as comparison is done by reference not by value)
        HashMap<ByteBuffer, Long> frequencyMap = new HashMap<ByteBuffer, Long>();
        Long fileSize = fileOperations.getFileSize(path);
        Long numberOfChuncks = fileSize / chunkLengthInBytes;
        Long remainingBytesCount = fileSize % chunkLengthInBytes;
        byte[] chunk = new byte[chunkLengthInBytes];

        //loop over the file and construct the chuck then add this chuck to the freqency map
        for (int i = 0 ; i < numberOfChuncks ; i ++) {
            chunk = fileOperations.readNBytes(fileInputStream, chunkLengthInBytes);
            frequencyMap.put(ByteBuffer.wrap(chunk), frequencyMap.getOrDefault(ByteBuffer.wrap(chunk), 0L) + 1L);
        }

        //if some bytes remain (file size is not divisible by chunk size)
        if(remainingBytesCount > 0){
            byte[] chunkOfRemainingBytes = new byte[remainingBytesCount.intValue()];
            chunkOfRemainingBytes = fileOperations.readNBytes(fileInputStream, remainingBytesCount.intValue());
            frequencyMap.put(ByteBuffer.wrap(chunkOfRemainingBytes), 1L);
        }

        fileOperations.closeFileInputStream(fileInputStream);
        return frequencyMap;
    }
}
