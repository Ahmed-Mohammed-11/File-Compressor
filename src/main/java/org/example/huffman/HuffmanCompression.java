package org.example.huffman;

import org.example.utils.ByteArray;
import org.example.utils.ByteArrayOperations;
import org.example.utils.CharFrequencyCalculator;
import org.example.utils.FileOperations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

import static org.example.utils.Constants.FILE_WRITE_CHUNK_SIZE_THRESHOLD;


public class HuffmanCompression {
    private final CharFrequencyCalculator charFrequencyCalculator = new CharFrequencyCalculator();
    private final FileOperations fileOperations = new FileOperations();
    private final ByteArrayOperations byteArrayOperations = new ByteArrayOperations();
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public HuffmanCompression(String inputFilePath, int chunkLengthInBytes, String outputFilePath) {
        compress(inputFilePath, chunkLengthInBytes, outputFilePath);
    }

    private void compress(String inputFilePath, int chunkLengthInBytes, String outputFilePath) {
        Map<ByteArray, Long> freqMap = charFrequencyCalculator.calculateCharFreq(inputFilePath, chunkLengthInBytes);
        //min heap of CharFreqNode comparator based on freq
        Queue<CharFreqNode> queue = new PriorityQueue<>(Comparator.comparing(a -> a.freq));
        //add all the characters and their frequencies to the queue
        constructMinHeap(freqMap, queue);
        //build the huffman tree of frequencies and get the root
        CharFreqNode root = buildHuffmanTree(queue);
        //calculate the huffman codes for each characterSet
        Map<ByteArray, String> codes = new HashMap<>();
        //calculate the prefix codes for each characterSet
        calculatePrefixCodes(root, "", codes);
        //write the encoded text to the file
        writeEncodedTextToFile(inputFilePath, chunkLengthInBytes, codes, outputFilePath);
    }

    private void constructMinHeap(Map<ByteArray, Long> freqMap, Queue<CharFreqNode> queue) {
        for (Map.Entry<ByteArray, Long> entry : freqMap.entrySet()) {
            //create a new node for each chuckOfBytes and add it to the queue
            CharFreqNode charFreqNode = new CharFreqNode();
            charFreqNode.chunkOfBytes = entry.getKey();
            charFreqNode.freq = entry.getValue();
            queue.add(charFreqNode);
        }
    }

    private CharFreqNode buildHuffmanTree(Queue<CharFreqNode> queue) {
        CharFreqNode root = null;
        while (queue.size() > 1) {
            // extract the two nodes with the lowest frequencies
            // and create a new node with the sum of their frequencies
            CharFreqNode node1 = queue.poll();
            CharFreqNode node2 = queue.poll();
            CharFreqNode newNode = new CharFreqNode();
            newNode.chunkOfBytes = null;
            assert node2 != null;
            newNode.freq = node1.freq + node2.freq;
            newNode.left = node1;
            newNode.right = node2;
            root = newNode;
            queue.add(newNode);
        }
        return root;
    }

    private void calculatePrefixCodes(CharFreqNode root, String code, Map<ByteArray, String> codes) {
        if (root == null) return;
        if (root.left == null && root.right == null) {
            codes.put(root.chunkOfBytes, code);
            return;
        }
        assert root.left != null;
        calculatePrefixCodes(root.left, code + "0", codes);
        calculatePrefixCodes(root.right, code + "1", codes);
    }

    private void writeEncodedTextToFile(String inputFilePath, int chunkLengthInBytes, Map<ByteArray, String> codes, String outputFilePAth) {
        fileInputStream = fileOperations.createFileInputStream(inputFilePath);
        fileOutputStream = fileOperations.createFileOutputStream(outputFilePAth);
        FileOperations.setFileSize(inputFilePath);
        Long fileSize = FileOperations.getFileSize();
        // write the header information to the file including uncompressed file size, header size and the prefix codes tree
        writeHeaderToFile(fileOutputStream, codes, chunkLengthInBytes);
        // read bigger chunk from memory to avoid reading from disk multiple times (fast read)
        int chunkLengthToReadFromFile = FileOperations.calculateChunkLengthToRead(chunkLengthInBytes);
        //loop over the file and construct the chuck then add this chuck to the frequency map
        for (int i = 0; i < fileSize / chunkLengthToReadFromFile; i++) {
            handleChunk(chunkLengthToReadFromFile, chunkLengthInBytes, codes);
        }
        if (fileSize % chunkLengthToReadFromFile > 0)
            handleChunk((int) (fileSize % chunkLengthToReadFromFile), chunkLengthInBytes, codes);
        fileOperations.closeFileInputStream(fileInputStream);
        fileOperations.closeFileOutputStream(fileOutputStream);
    }

    private void writeHeaderToFile(FileOutputStream fileOutputStream, Map<ByteArray, String> codes, int chunkLengthInBytes) {
        //write the uncompressed file size to the file
        //write file size to the file
        fileOperations.writeByteArray(fileOutputStream , byteArrayOperations.convertLongToByteArray(FileOperations.getFileSize()));
        //write the header size to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertIntToByteArray(codes.size() * (chunkLengthInBytes + 1)));
        //write the prefix codes to the file
        for (Map.Entry<ByteArray, String> entry : codes.entrySet()) {
            fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertStringToByteArray(entry.getValue()));
        }
    }

    private void handleChunk(int chunkLengthToReadFromFile, int chunkLengthInBytes, Map<ByteArray, String> codes) {
        byte[] chuckFromFile;
        ByteArray chunk;
        StringBuilder code = new StringBuilder();
        chuckFromFile = fileOperations.readNBytes(fileInputStream, chunkLengthToReadFromFile);
        for (int j = 0; j < chunkLengthToReadFromFile / chunkLengthInBytes; j++) {
            //divide the chuck into smaller chunks of size chunkLengthInBytes in efficient way
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, j * chunkLengthInBytes, (j + 1) * chunkLengthInBytes));
            code.append(codes.get(chunk));
            if (code.length() > FILE_WRITE_CHUNK_SIZE_THRESHOLD) {
                String codeToWrite = code.substring(0, FILE_WRITE_CHUNK_SIZE_THRESHOLD);
                fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertStringToByteArray(codeToWrite));
                code = new StringBuilder(code.substring(FILE_WRITE_CHUNK_SIZE_THRESHOLD));
            } else if (code.length() == FILE_WRITE_CHUNK_SIZE_THRESHOLD) {
                fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertStringToByteArray(code.toString()));
                code = new StringBuilder();
            }
        }
        //handle if file size is less than predetermined chunk
        if (chunkLengthToReadFromFile % chunkLengthInBytes > 0) {
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, chunkLengthToReadFromFile - chunkLengthToReadFromFile % chunkLengthInBytes, chunkLengthToReadFromFile));
            code.append(codes.get(chunk));
            fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertStringToByteArray(code.toString()));
        }

        if (code.length() > 0)
            fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertStringToByteArray(code.toString()));
    }


    static class CharFreqNode {
        ByteArray chunkOfBytes;
        Long freq;
        CharFreqNode left;
        CharFreqNode right;
    }
}