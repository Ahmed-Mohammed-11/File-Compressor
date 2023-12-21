package org.example;

import org.example.huffman.HuffmanCompression;
import org.example.huffman.HuffmanDecompression;
import org.example.utils.Constants;
import org.example.utils.FileOperations;

import java.io.File;

import static org.example.utils.Constants.*;

public class Main {
    public static void main(String[] args) {
        FileOperations fileOperations = new FileOperations();
        // Check if the arguments are valid
        if (!((args.length == 2 && args[0].equals(DECOMPRESS_CHOICE)) || (args.length == 3 && args[0].equals(COMPRESS_CHOICE)))) {
            System.err.println(Constants.ARGUMENTS_FORMAT_COMPRESS + "\n" + Constants.ARGUMENTS_FORMAT_DECOMPRESS);
            System.exit(1);
        }

        // extract the choice and path from the arguments
        String choice = args[0];

        // extract only the path from the arguments not the file name

        File file = new File(args[1]);
        String inputFilePath = args[1];
        String filePathWithoutFileName = fileOperations.getFilePath(file);
        String inputFileName = FileOperations.getFileName(file);
        String outputFilePath;


        //log time
        long startTime;
        long endTime;
        // start operations based on the choice
        switch (choice) {
            case COMPRESS_CHOICE:
                if(inputFilePath.endsWith(".hc")){
                    System.err.println("Invalid file type");
                    System.exit(1);
                }
                int chunkLengthInBytes = Integer.parseInt(args[2]);
                String outputFileName = ID + "." + chunkLengthInBytes + "." + inputFileName + ".hc";
                outputFilePath = filePathWithoutFileName + outputFileName;
                System.out.println("Compressing " + inputFilePath + " to " + outputFilePath + " ... ");
                startTime = System.currentTimeMillis();
                new HuffmanCompression(inputFilePath, chunkLengthInBytes, outputFilePath);
                endTime = System.currentTimeMillis();
                System.out.println("Time taken to compress the file: " + (endTime - startTime) / 1000.0 + " seconds");
                Long uncompressedFileSize = FileOperations.getFileSize();
                FileOperations.setFileSize(outputFilePath);
                Long compressedFileSize = FileOperations.getFileSize();
                System.out.println("Compression ratio: " + (double) compressedFileSize / uncompressedFileSize);
                break;
            case DECOMPRESS_CHOICE:
                if(!inputFilePath.endsWith(".hc")){
                    System.err.println("Invalid file type");
                    System.exit(1);
                }
                //remove .hc extension
                outputFilePath = filePathWithoutFileName + "extracted" + "." + inputFileName.substring(0, inputFileName.length() - COMPRESSED_FILE_EXTENSION.length());
                System.out.println("Decompressing " + inputFilePath + " to " + outputFilePath + " ... ");
                startTime = System.currentTimeMillis();
                new HuffmanDecompression(inputFilePath, outputFilePath);
                endTime = System.currentTimeMillis();
                System.out.println("Time taken to decompress the file: " + (endTime - startTime) / 1000.0 + " seconds");
                break;
            default:
                System.err.println("Invalid choice");
                System.exit(1);
        }
    }
}