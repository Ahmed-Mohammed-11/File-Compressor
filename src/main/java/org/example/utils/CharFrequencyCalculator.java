package org.example.utils;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class CharFrequencyCalculator {

    private final FileOperations fileOperations = new FileOperations();
    FileInputStream fileInputStream;

    public Map<ByteArray, Long> calculateCharFreq(String inputFilePath, int chunkLengthInBytes) {
        fileInputStream = fileOperations.createFileInputStream(inputFilePath);
        HashMap<ByteArray, Long> frequencyMap = new HashMap<>();
        FileOperations.setFileSize(inputFilePath);
        Long fileSize = FileOperations.getFileSize();
        // read bigger chunk from memory to avoid reading from disk multiple times (fast read)
        int chunkLengthToReadFromFile = FileOperations.calculateChunkLengthToRead(chunkLengthInBytes);

        // loop over the file and construct the chuck then add this chuck to the frequency map
        for (int i = 0; i < fileSize / chunkLengthToReadFromFile; i++) {
            handleChunk(frequencyMap, chunkLengthToReadFromFile, chunkLengthInBytes);
        }

        // handle if file size is less than predetermined chunk
        if (fileSize % chunkLengthToReadFromFile > 0)
            handleChunk(frequencyMap, (int) (fileSize % chunkLengthToReadFromFile), chunkLengthInBytes);

        System.out.println(frequencyMap.size());
        fileOperations.closeFileInputStream(fileInputStream);
        return frequencyMap;

    }

    private void handleChunk(HashMap<ByteArray, Long> frequencyMap, int chunkLengthToReadFromFile, int chunkLengthInBytes) {
        byte[] chuckFromFile;
        ByteArray chunk;
        chuckFromFile = fileOperations.readNBytes(fileInputStream, chunkLengthToReadFromFile);
        for (int j = 0; j < chunkLengthToReadFromFile / chunkLengthInBytes; j++) {
            //divide the chuck into smaller chunks of size chunkLengthInBytes in efficient way
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, j * chunkLengthInBytes, (j + 1) * chunkLengthInBytes));
            frequencyMap.put(chunk, frequencyMap.getOrDefault(chunk, 0L) + 1L);
        }
        //handle if file size is less than predetermined chunk
        if (chunkLengthToReadFromFile % chunkLengthInBytes > 0) {
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, chunkLengthToReadFromFile - chunkLengthToReadFromFile % chunkLengthInBytes, chunkLengthToReadFromFile));
            frequencyMap.put(chunk, frequencyMap.getOrDefault(chunk, 0L) + 1L);
        }
    }
}