package org.example.huffman;

import org.example.utils.ByteArrayOperations;
import org.example.utils.FileOperations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HuffmanDecompression {
    private final FileOperations fileOperations = new FileOperations();
    private final ByteArrayOperations byteArrayOperations = new ByteArrayOperations();
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private long bytesReadFromCompressedFile = 0L;


    public HuffmanDecompression(String inputFilePath, String outputFilePath) { decompress(inputFilePath, outputFilePath); }

    private void decompress(String inputFilePath, String outputFilePath) {
        fileInputStream = fileOperations.createFileInputStream(inputFilePath);
        fileOutputStream = fileOperations.createFileOutputStream(outputFilePath);
        FileOperations.setFileSize(inputFilePath);
        Headers headers = extractHeaders();
        writeDecompressedFile(headers);
        fileOperations.closeFileInputStream(fileInputStream);
        fileOperations.closeFileOutputStream(fileOutputStream);
    }

    private Headers extractHeaders() {
        byte[] uncompressedFileSize = fileOperations.readNBytes(fileInputStream, Long.SIZE / Byte.SIZE);
        byte[] numberOfBytesInCompressedFile = fileOperations.readNBytes(fileInputStream, Long.SIZE / Byte.SIZE);
        byte[] headerSize = fileOperations.readNBytes(fileInputStream, Long.SIZE / Byte.SIZE);
        byte[] chunkLengthInBytes = fileOperations.readNBytes(fileInputStream, Integer.SIZE / Byte.SIZE);
        byte[] entryNumberWithChunkLessThanChunkLength = fileOperations.readNBytes(fileInputStream, Integer.SIZE / Byte.SIZE);

        Headers headers = new Headers();
        headers.uncompressedFileSize = byteArrayOperations.convertByteArrayToLong(uncompressedFileSize);
        headers.numberOfBitsInCompressedFile = byteArrayOperations.convertByteArrayToLong(numberOfBytesInCompressedFile);
        headers.headerSize = byteArrayOperations.convertByteArrayToLong(headerSize);
        headers.chunkLengthInBytes = byteArrayOperations.convertByteArrayToInt(chunkLengthInBytes);
        headers.entryNumberWithChunkLessThanChunkLength = byteArrayOperations.convertByteArrayToInt(entryNumberWithChunkLessThanChunkLength);

        long huffmanCodesSize = headers.headerSize - 3 * Long.SIZE / Byte.SIZE - 2 * Integer.SIZE / Byte.SIZE;
        byte[] huffmanCodesBytes = fileOperations.readNBytes(fileInputStream, (int) huffmanCodesSize);

        //read huffman codes which is on the form of key(chunkLengthInBytes) , number of char in value(one byte) , value
        //then put them in the map in reverse order as the key now is the code word and the value is the original byte[]

        constructHuffmanCodesMap(headers, huffmanCodesBytes, chunkLengthInBytes);
        return headers;
    }

    private void constructHuffmanCodesMap(Headers headers, byte[] huffmanCodesBytes, byte[] chunkLengthInBytes) {
        int i = 0;
        byte[] originalMapKey;
        byte originalMapValueLength;
        byte[] originalMapValue;
        int smallChunkFinder = 0;
        while (i < huffmanCodesBytes.length) {
            smallChunkFinder++;
            if (smallChunkFinder == headers.entryNumberWithChunkLessThanChunkLength) {
                //read the chunk length of the chunk that is less than the chunk length
                headers.chunkLengthInBytes = byteArrayOperations.convertByteArrayToInt(Arrays.copyOfRange(huffmanCodesBytes, i, i + Integer.SIZE / Byte.SIZE));
                i += Integer.SIZE / Byte.SIZE;
            }
            originalMapKey = Arrays.copyOfRange(huffmanCodesBytes, i, i + headers.chunkLengthInBytes);
            i += headers.chunkLengthInBytes;
            originalMapValueLength = huffmanCodesBytes[i++];
            originalMapValue = Arrays.copyOfRange(huffmanCodesBytes, i, i + originalMapValueLength);
            i += originalMapValueLength;
            headers.huffmanCodes.put(byteArrayOperations.convertByteArrayToString(originalMapValue), originalMapKey);
            //reset the chunk length to the original chunk length
            headers.chunkLengthInBytes = byteArrayOperations.convertByteArrayToInt(chunkLengthInBytes);
        }
    }


    private void writeDecompressedFile(Headers headers) {
        long compressedFileSizeRemaining = FileOperations.getFileSize() - headers.headerSize;
        int chunkLengthToReadFromFile = fileOperations.calculateChunkLengthToRead(headers.chunkLengthInBytes, compressedFileSizeRemaining);
        if (chunkLengthToReadFromFile * 8 > Integer.MAX_VALUE / 8) {
            chunkLengthToReadFromFile = chunkLengthToReadFromFile / 64;
        }

        String currentPrefixCode = "";
        for (int i = 0; i < compressedFileSizeRemaining / chunkLengthToReadFromFile; i++) {
            currentPrefixCode = handleChunk(chunkLengthToReadFromFile, headers, currentPrefixCode);
        }

        if (compressedFileSizeRemaining % chunkLengthToReadFromFile > 0)
        {
            handleChunk((int) (compressedFileSizeRemaining % chunkLengthToReadFromFile), headers, currentPrefixCode);
        }
    }

    private String handleChunk(int chunkLengthToReadFromFile, Headers headers, String currentPrefixCode) {
        byte numberOfBitsPrependedOnLastByte;
        byte[] chuckFromFile;
        chuckFromFile = fileOperations.readNBytes(fileInputStream, chunkLengthToReadFromFile);
        StringBuilder code = new StringBuilder();
        code.append(currentPrefixCode);
        StringBuilder fileContentToWrite = new StringBuilder();
        byte[] fileBytes;
        String binaryStringOfChunkFromFile = byteArrayOperations.convertByteArrayToBinaryString(chuckFromFile);
        if (bytesReadFromCompressedFile + chunkLengthToReadFromFile == FileOperations.getFileSize() - headers.headerSize) {
            numberOfBitsPrependedOnLastByte = (byte) ((FileOperations.getFileSize() - headers.headerSize) * Byte.SIZE - headers.numberOfBitsInCompressedFile);
            if (numberOfBitsPrependedOnLastByte > 0) {
                String lastByte = binaryStringOfChunkFromFile.substring(binaryStringOfChunkFromFile.length() - Byte.SIZE);
                //remove prepended bits on last byte
                lastByte = lastByte.substring(numberOfBitsPrependedOnLastByte, Byte.SIZE);
                binaryStringOfChunkFromFile = binaryStringOfChunkFromFile.substring(0, binaryStringOfChunkFromFile.length() - Byte.SIZE) + lastByte;
            }
        }

        bytesReadFromCompressedFile += chunkLengthToReadFromFile;

        for (int i = 0; i < binaryStringOfChunkFromFile.length(); i++) {
            code.append(binaryStringOfChunkFromFile.charAt(i));
            if (headers.huffmanCodes.containsKey(code.toString())) {
                if (fileContentToWrite.length() == Integer.MAX_VALUE / 32 - Integer.MAX_VALUE / 32 % headers.chunkLengthInBytes) {
                    fileBytes = byteArrayOperations.convertBinaryStringToByteArray(fileContentToWrite.toString());
                    fileOperations.writeByteArray(fileOutputStream, fileBytes);
                    fileContentToWrite = new StringBuilder();
                }
                fileContentToWrite.append(byteArrayOperations.convertByteArrayToBinaryString(headers.huffmanCodes.get(code.toString())));
                code = new StringBuilder();
            }
        }

        if (fileContentToWrite.length() > 0) {
            fileBytes = byteArrayOperations.convertBinaryStringToByteArray(fileContentToWrite.toString());
            fileOperations.writeByteArray(fileOutputStream, fileBytes);
        }

        return code.toString();
    }

    private static class Headers {
        long uncompressedFileSize;
        long numberOfBitsInCompressedFile;
        long headerSize;
        int chunkLengthInBytes;
        int entryNumberWithChunkLessThanChunkLength;
        Map<String, byte[]> huffmanCodes = new HashMap<>();
    }
}
