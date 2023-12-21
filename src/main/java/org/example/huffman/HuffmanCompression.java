package org.example.huffman;

import org.example.utils.ByteArray;
import org.example.utils.ByteArrayOperations;
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
        if (freqMap.size() == 1)
            codes.put(freqMap.keySet().iterator().next(), "0");
        //write the encoded text to the file
        writeEncodedTextToFile(inputFilePath, chunkLengthInBytes, codes, freqMap, outputFilePath);
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
        if (queue.size() == 1) {
            root = queue.poll();
            return root;
        }
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

    private void writeEncodedTextToFile(String inputFilePath, int chunkLengthInBytes, Map<ByteArray, String> codes, Map<ByteArray, Long> freqMap, String outputFilePAth) {
        fileInputStream = fileOperations.createFileInputStream(inputFilePath);
        fileOutputStream = fileOperations.createFileOutputStream(outputFilePAth);
        FileOperations.setFileSize(inputFilePath);
        Long fileSize = FileOperations.getFileSize();
        // write the header information to the file including uncompressed file size, header size and the prefix codes tree
        writeHeaderToFile(fileOutputStream, chunkLengthInBytes, codes, freqMap);
        // read bigger chunk from memory to avoid reading from disk multiple times (fast read)
        int chunkLengthToReadFromFile = fileOperations.calculateChunkLengthToRead(chunkLengthInBytes, fileSize);
        //loop over the file and construct the chuck then add this chuck to the frequency map
        String code = "";
        for (int i = 0; i < fileSize / chunkLengthToReadFromFile; i++) {
            code = handleChunk(chunkLengthToReadFromFile, chunkLengthInBytes, codes, code);
        }
        if (fileSize % chunkLengthToReadFromFile > 0)
            code = handleChunk((int) (fileSize % chunkLengthToReadFromFile), chunkLengthInBytes, codes, code);

        if (!code.isEmpty())
            fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertBinaryStringToByteArray(code));

        fileOperations.closeFileInputStream(fileInputStream);
        fileOperations.closeFileOutputStream(fileOutputStream);
    }

    private void writeHeaderToFile(FileOutputStream fileOutputStream, int chunkLengthInBytes, Map<ByteArray, String> codes, Map<ByteArray, Long> freqMap) {

        /*
        header contains the size of
        * 1- uncompressed file size
        * 2- header size
        * 3- chunk length
        * 4- huffman codes map entry that contains a chunk less than the chunk length
        * 5- huffman codes map
        * */

        //number of bits in compressed file
        long numberOfBitsInCompressedFile = 0;
        for (Map.Entry<ByteArray, Long> entry : freqMap.entrySet()) {
            numberOfBitsInCompressedFile += codes.get(entry.getKey()).length() * entry.getValue();
        }
        long codesMapSize = 0;
        int entryNumberWithChunkLessThanChunkLength = -1;
        int smallChunkFinder = 0;
        for (Map.Entry<ByteArray, String> entry : codes.entrySet()) {
            smallChunkFinder++;
            if (entry.getKey().bytes.length < chunkLengthInBytes) {
                entryNumberWithChunkLessThanChunkLength = smallChunkFinder;
                codesMapSize += Integer.SIZE / Byte.SIZE;
            }
            codesMapSize += entry.getKey().bytes.length + 1 + entry.getValue().length();
        }

        long headerSize = 3 * (Long.SIZE / Byte.SIZE) + 2 * (Integer.SIZE / Byte.SIZE) + codesMapSize;

        //write file size to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertLongToByteArray(FileOperations.getFileSize()));
        //write the number of bits in compressed file to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertLongToByteArray(numberOfBitsInCompressedFile));
        //write the header size to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertLongToByteArray(headerSize));
        //write the chunk length to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertIntToByteArray(chunkLengthInBytes));
        //write the entry number with chunk less than chunk length to the file
        fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertIntToByteArray(entryNumberWithChunkLessThanChunkLength));

        //write the hashmap of codes to the file efficiently

        smallChunkFinder = 0;
        for (Map.Entry<ByteArray, String> entry : codes.entrySet()) {
            smallChunkFinder++;
            if (smallChunkFinder == entryNumberWithChunkLessThanChunkLength) {
                byte[] smallChunkLength = byteArrayOperations.convertIntToByteArray(entry.getKey().bytes.length);
                fileOperations.writeByteArray(fileOutputStream, smallChunkLength);
            }
            fileOperations.writeByteArray(fileOutputStream, entry.getKey().bytes);
            byte huff = (byte) (entry.getValue().length());
            byte[] huffmanCodeLength = byteArrayOperations.convertByteToByteArray(huff);
            fileOperations.writeByteArray(fileOutputStream, huffmanCodeLength);
            fileOperations.writeByteArray(fileOutputStream, entry.getValue().getBytes());
        }
    }

    private String handleChunk(int chunkLengthToReadFromFile, int chunkLengthInBytes, Map<ByteArray, String> codes, String code) {
        byte[] chuckFromFile;
        ByteArray chunk;
        StringBuilder codeBuilder = new StringBuilder(code);
        chuckFromFile = fileOperations.readNBytes(fileInputStream, chunkLengthToReadFromFile);
        for (int j = 0; j < chunkLengthToReadFromFile / chunkLengthInBytes; j++) {
            //divide the chuck into smaller chunks of size chunkLengthInBytes in efficient way
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, j * chunkLengthInBytes, (j + 1) * chunkLengthInBytes));
            codeBuilder.append(codes.get(chunk));
            if (codeBuilder.length() > FILE_WRITE_CHUNK_SIZE_THRESHOLD) {
                String codeToWrite = codeBuilder.substring(0, FILE_WRITE_CHUNK_SIZE_THRESHOLD);
                fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertBinaryStringToByteArray(codeToWrite));
                codeBuilder = new StringBuilder(codeBuilder.substring(FILE_WRITE_CHUNK_SIZE_THRESHOLD));
            } else if (codeBuilder.length() == FILE_WRITE_CHUNK_SIZE_THRESHOLD) {
                fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertBinaryStringToByteArray(codeBuilder.toString()));
                codeBuilder = new StringBuilder();
            }
        }
        //handle if file size is less than predetermined chunk
        if (chunkLengthToReadFromFile % chunkLengthInBytes > 0) {
            chunk = new ByteArray(Arrays.copyOfRange(chuckFromFile, chunkLengthToReadFromFile - chunkLengthToReadFromFile % chunkLengthInBytes, chunkLengthToReadFromFile));
            codeBuilder.append(codes.get(chunk));
            fileOperations.writeByteArray(fileOutputStream, byteArrayOperations.convertBinaryStringToByteArray(code));
        }
        return codeBuilder.toString();
    }

    static class CharFreqNode {
        ByteArray chunkOfBytes;
        Long freq;
        CharFreqNode left;
        CharFreqNode right;
    }
}