package org.example.huffman;

import org.example.utils.CharFrequecyCalculator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

public class Compress {
    class CharFreqNode{
        String characterSet;
        Long freq;
        CharFreqNode left;
        CharFreqNode right;
    }

    public Compress(String path, Integer bytes) {
        compress(path, bytes);
    }


    private CharFreqNode buildHuffmanTree(Queue<CharFreqNode> queue) {

        CharFreqNode root = null;
        while(queue.size() > 1){
            // extract the two nodes with the lowest frequencies
            // and create a new node with the sum of their frequencies
            CharFreqNode node1 = queue.poll();
            CharFreqNode node2 = queue.poll();
            CharFreqNode newNode = new CharFreqNode();
            newNode.characterSet = "";
            newNode.freq = node1.freq + node2.freq;
            newNode.left = node1;
            newNode.right = node2;
            root = newNode;
            queue.add(newNode);
        }
        return root;
    }

    private void printTree(CharFreqNode root, int level) {
        if(root == null) return;
        for(int i = 0; i < level; i++) System.out.print("  ");
        printTree(root.left, level+1);
        printTree(root.right, level+1);
    }

    private void calculateCodes(CharFreqNode root, String code, TreeMap<String, String> codes) {
        if(root.left == null && root.right == null){
            codes.put(root.characterSet, code);
            return;
        }

        calculateCodes(root.left, code + "0", codes);
        calculateCodes(root.right, code + "1", codes);
    }

    private void compress(String path, Integer bytes) {
        CharFrequecyCalculator charFrequecyCalculator = new CharFrequecyCalculator();
        HashMap freqMap = charFrequecyCalculator.calculateCharFreq(path, bytes);

        //minheap of CharFreqNode comparator based on freq
        Queue<CharFreqNode> queue = new PriorityQueue<CharFreqNode>((a, b) -> a.freq.compareTo(b.freq));

        //add all the characters and their frequencies to the queue
        for(Object c: freqMap.keySet()){
            CharFreqNode charFreqNode = new CharFreqNode();
            charFreqNode.characterSet = (String) c;
            charFreqNode.freq = (Long) freqMap.get(c);
            queue.add(charFreqNode);
        }


        //build the huffman tree of frequencies and get the root
        CharFreqNode root = buildHuffmanTree(queue);

        //calculate the huffman codes for each characterSet
        TreeMap<String, String> codes = new TreeMap<String, String>();
        calculateCodes(root, "", codes);

        //TODO: write the codes to the file
        //TODO: write the encoded text to the file
    }
}
