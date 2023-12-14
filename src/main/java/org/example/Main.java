package org.example;

import org.example.huffman.HuffmanCompression;
import org.example.huffman.HuffmanDecompression;
import org.example.utils.CharFrequecyCalculator;
import org.example.utils.Constants;

public class Main {
    public static void main(String[] args) {
        CharFrequecyCalculator charFrequecyCalculator = new CharFrequecyCalculator();

        // Check if the arguments are valid
        if (!((args.length == 2 && args[0].equals(Constants.DECOMPRESS_CHOICE)) || (args.length == 3 && args[0].equals(Constants.COMPRESS_CHOICE)))) {
            System.err.println(Constants.ARGUMENTS_FORMAT_COMPRESS + "\n" + Constants.ARGUMENTS_FORMAT_DECOMPRESS);
            System.exit(1);
        }

        // extract the choice and path from the arguments
        String choice = args[0];
        String path = args[1];

        // start operations based on the choice
        switch (choice) {
            case Constants.COMPRESS_CHOICE:
                Integer bytes = Integer.parseInt(args[2]);
                new HuffmanCompression(path, bytes);
                break;
            case Constants.DECOMPRESS_CHOICE:
                new HuffmanDecompression(path);
                break;
        }
    }
}