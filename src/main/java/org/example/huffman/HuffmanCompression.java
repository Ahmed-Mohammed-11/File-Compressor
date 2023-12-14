package org.example.huffman;

import org.example.utils.CharFrequecyCalculator;
import org.example.utils.FileOperations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class HuffmanCompression {
    private CharFrequecyCalculator charFrequecyCalculator = new CharFrequecyCalculator();
    private FileOperations fileOperations = new FileOperations();
    public HuffmanCompression(String path, Integer bytes) { compress(path, bytes); }

    class CharFreqNode {
        ByteBuffer chunkOfBytes;
        Long freq;
        CharFreqNode left;
        CharFreqNode right;
    }

    private void compress(String path, Integer chunkLengthInBytes) {
        HashMap freqMap = charFrequecyCalculator.calculateCharFreq(path, chunkLengthInBytes);
        //minheap of CharFreqNode comparator based on freq
        Queue<CharFreqNode> queue = new PriorityQueue<CharFreqNode>((a, b) -> a.freq.compareTo(b.freq));
        //add all the characters and their frequencies to the queue
        costructMinHeap(freqMap, queue);
        //build the huffman tree of frequencies and get the root
        CharFreqNode root = buildHuffmanTree(queue);
        //calculate the huffman codes for each characterSet
        TreeMap<ByteBuffer, String> codes = new TreeMap<ByteBuffer, String>();
        //calculate the prefix codes for each characterSet
        calculatePrefixCodes(root, "", codes);
        codes.forEach((k, v) -> System.out.println("key: " + Arrays.toString(k.array()) + " value: " + v));
        //write the encoded text to the file
        writeEncodedTextToFile(path, chunkLengthInBytes, codes);
    }

    private void writeEncodedTextToFile(String path, Integer chunkLengthInBytes, TreeMap<ByteBuffer, String> codes) {
        FileInputStream fileInputStream = fileOperations.createFileInputStream(path);
        FileOutputStream fileOutputStream = fileOperations.createFileOutputStream(path + ".huff");
        StringBuilder stringBuilder = new StringBuilder();
        Long numberOfChunks = fileOperations.getFileSize(path) / chunkLengthInBytes;
        for (int i = 0; i < numberOfChunks ; i++) {
            byte[] chuck = fileOperations.readNBytes(fileInputStream, chunkLengthInBytes);
            System.out.println("chuck: " + Arrays.toString(chuck));
            String code = codes.get(ByteBuffer.wrap(chuck));
            stringBuilder.append(code);
            if (stringBuilder.length() == 8) {
                byte encodedText = convertStringToByte(stringBuilder.toString());
                fileOperations.writeByte(fileOutputStream, encodedText);
                stringBuilder = new StringBuilder();
            }
            else if(stringBuilder.length() > 8) {
                String oldString = stringBuilder.toString();
                String first8Bits = stringBuilder.substring(0, 8);
                byte encodedText = convertStringToByte(first8Bits);
                fileOperations.writeByte(fileOutputStream, encodedText);
                stringBuilder = new StringBuilder();
                stringBuilder.append(oldString.substring(8));
            }
        }
        if (stringBuilder.length() > 0) {
            byte encodedText = convertStringToByte(stringBuilder.toString());
            fileOperations.writeByte(fileOutputStream, encodedText);
        }
        fileOperations.closeFileInputStream(fileInputStream);
        fileOperations.closeFileOutputStream(fileOutputStream);
    }

    private byte convertStringToByte(String string) {
        System.out.println("string: " + string);
        byte byteFromString = (byte) Integer.parseInt(string, 2);
        System.out.println("byteFromString: " + byteFromString);
        return byteFromString;
    }

    private void costructMinHeap(HashMap freqMap, Queue<CharFreqNode> queue) {
        for (Object c : freqMap.keySet()) {
            //create a new node for each chuckOfBytes and add it to the queue
            CharFreqNode charFreqNode = new CharFreqNode();
            charFreqNode.chunkOfBytes = (ByteBuffer) c;
            charFreqNode.freq = (Long) freqMap.get(c);
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
            newNode.freq = node1.freq + node2.freq;
            newNode.left = node1;
            newNode.right = node2;
            root = newNode;
            queue.add(newNode);
        }
        return root;
    }

    private void calculatePrefixCodes(CharFreqNode root, String code, TreeMap<ByteBuffer, String> codes) {
        if (root.left == null && root.right == null) {
            codes.put(root.chunkOfBytes, code);
            return;
        }
        calculatePrefixCodes(root.left, code + "0", codes);
        calculatePrefixCodes(root.right, code + "1", codes);
    }

    private void printTree(CharFreqNode root, int level) {
        if (root == null) return;
        for (int i = 0; i < level; i++) System.out.print("  ");
        printTree(root.left, level + 1);
        printTree(root.right, level + 1);
    }
}
