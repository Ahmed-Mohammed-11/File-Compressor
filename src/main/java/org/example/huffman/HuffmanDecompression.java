package org.example.huffman;

import org.example.utils.ByteArray;
import org.example.utils.ByteArrayOperations;
import org.example.utils.FileOperations;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HuffmanDecompression {
    FileOperations fileOperations = new FileOperations();
    FileInputStream fileInputStream;
    FileOutputStream fileOutputStream;
    ByteArrayOperations byteArrayOperations = new ByteArrayOperations();
    public HuffmanDecompression(String path) {
        decompress(path);
    }

    private void decompress(String path) {
        fileInputStream = fileOperations.createFileInputStream(path);
        fileOutputStream = fileOperations.createFileOutputStream(path + ".txt");
        FileOperations.setFileSize(path);
        Headers headers = extractHeaders();
        System.out.println("headers.uncompressedFileSize = " + headers.uncompressedFileSize);
        System.out.println("headers.headerSize = " + headers.headerSize);
        System.out.println("headers.chunkLengthInBytes = " + headers.chunkLengthInBytes);
        System.out.println("headers.treeTraversedPostFix = " + headers.treeTraversedPostFix);
        Map<ByteArray, String> huffmanCodes = new HashMap<>();
        rebuildHuffmanCodes(headers.treeTraversedPostFix, huffmanCodes);
//        huffmanCodes.forEach((key, value) -> System.out.println("key = " + Arrays.toString(key.bytes) + " value = " + value));
        fileOperations.closeFileInputStream(fileInputStream);
    }

    private Headers extractHeaders(){
        byte[] fileSize = fileOperations.readNBytes(fileInputStream, 8);
        byte[] headerSize = fileOperations.readNBytes(fileInputStream, 8);
        byte[] chunkLengthInBytes = fileOperations.readNBytes(fileInputStream, 4);

        Headers headers = new Headers();
        headers.uncompressedFileSize = byteArrayOperations.convertByteArrayToLong(fileSize);
        headers.headerSize = byteArrayOperations.convertByteArrayToLong(headerSize);
        headers.chunkLengthInBytes = byteArrayOperations.convertByteArrayToInt(chunkLengthInBytes);

        int treeTraversedPostFixLength = (int) (headers.headerSize - 2 * (Long.SIZE / Byte.SIZE) - (Integer.SIZE / Byte.SIZE));
        byte[] treeTraversedPostFix = fileOperations.readNBytes(fileInputStream, treeTraversedPostFixLength);
        headers.treeTraversedPostFix = new String(treeTraversedPostFix);
        return headers;
    }

    private void rebuildHuffmanCodes(String treeTraversedPostFix, Map<ByteArray, String> huffmanCodes) {
        //rebuild the huffman tree from the tree traversed post fix string
        Node node = new Node();
        Node currentNode = node;
        for (int i = 0; i < treeTraversedPostFix.length(); i++) {
            if (treeTraversedPostFix.charAt(i) == '1') {
                if (currentNode.left == null) {
                    currentNode.left = new Node();
                }
                currentNode = currentNode.left;
            } else if(treeTraversedPostFix.charAt(i) == '0'){
                if (currentNode.right == null) {
                    currentNode.right = new Node();
                }
                currentNode = currentNode.right;
            } else {
                currentNode.chunkOfBytes = new ByteArray(new byte[]{(byte) treeTraversedPostFix.charAt(i)});
                currentNode = node;
            }
            if(currentNode.chunkOfBytes != null) {
                System.out.println("node = " + Arrays.toString(currentNode.chunkOfBytes.bytes));
            }
        }
        printTree(node, 0);
        //traverse the tree and build the huffman codes
        traverseTreeAndBuildHuffmanCodes(node, huffmanCodes, new StringBuilder());
    }

    private void traverseTreeAndBuildHuffmanCodes(Node node, Map<ByteArray, String> huffmanCodes, StringBuilder stringBuilder) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.chunkOfBytes, stringBuilder.toString());
            if(node.chunkOfBytes != null){
                System.out.println(huffmanCodes.get(node.chunkOfBytes) + " " + Arrays.toString(node.chunkOfBytes.bytes));
            }
            return;
        }
        traverseTreeAndBuildHuffmanCodes(node.left, huffmanCodes, stringBuilder.append("0"));
        traverseTreeAndBuildHuffmanCodes(node.right, huffmanCodes, stringBuilder.append("1"));
    }

    private void printTree(Node root, int level) {
        if (root == null) return;
        printTree(root.right, level + 1);
        if (root.chunkOfBytes != null)
            System.out.println(Arrays.toString(root.chunkOfBytes.bytes));
        printTree(root.left, level + 1);
    }

    private static class Node{
        Node left;
        Node right;
        ByteArray chunkOfBytes;
    }
    private static class Headers{
        long uncompressedFileSize;
        long headerSize;
        int chunkLengthInBytes;
        String treeTraversedPostFix;
    }
}
