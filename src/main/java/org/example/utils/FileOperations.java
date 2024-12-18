package org.example.utils;

import java.io.*;

import static org.example.utils.Constants.FILE_READ_CHUNK_SIZE_THRESHOLD;

public class FileOperations {

    private static Long fileSize = 0L;

    public static Long getFileSize() {
        return fileSize;
    }

    public static void setFileSize(String path) {
        fileSize = new File(path).length();
    }

    public static String getFileName(File file) {
        return file.getName();
    }

    public int calculateChunkLengthToRead(int chunkLengthInBytes, long fileSize) {
        int chunkLengthToReadFromFile;
        chunkLengthToReadFromFile =
                (fileSize > FILE_READ_CHUNK_SIZE_THRESHOLD) ? (FILE_READ_CHUNK_SIZE_THRESHOLD) : (int) fileSize;
        if (chunkLengthToReadFromFile != fileSize && chunkLengthToReadFromFile % chunkLengthInBytes != 0)
            chunkLengthToReadFromFile -= chunkLengthToReadFromFile % chunkLengthInBytes;
        return chunkLengthToReadFromFile;
    }

    public FileInputStream createFileInputStream(String path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            throw new RuntimeException(e);
        }
    }

    public FileOutputStream createFileOutputStream(String path) {
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            throw new RuntimeException(e);
        }
    }

    public byte[] readNBytes(FileInputStream fileInputStream, int chunkSize) {
        byte[] chunk = new byte[chunkSize];

        try {
            int result = fileInputStream.read(chunk, 0, chunkSize);
            if (result == -1) {
                throw new RuntimeException("End of file reached");
            }
        } catch (IOException e) {
            System.err.println("Error reading file");
            throw new RuntimeException(e);
        }
        return chunk;
    }

    public void writeByteArray(FileOutputStream fileOutputStream, byte[] bytesToWrite) {
        try {
            fileOutputStream.write(bytesToWrite);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file");
        }
    }

    public void closeFileInputStream(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeFileOutputStream(FileOutputStream fileOutputStream) {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFilePath(File file) {
        String path = file.getPath();
        return path.substring(0, path.length() - file.getName().length());
    }

}
