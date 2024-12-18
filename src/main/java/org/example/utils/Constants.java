package org.example.utils;

public class Constants {
    public static final String ARGUMENTS_FORMAT_COMPRESS = "Program Arguments Format For Compression : c <input-file-path> [number of bytes]";
    public static final String ARGUMENTS_FORMAT_DECOMPRESS = "Program Arguments Format For Decompression: d <input-file-path>";
    public static final String COMPRESS_CHOICE = "c";
    public static final String DECOMPRESS_CHOICE = "d";
    public static final String COMPRESSED_FILE_EXTENSION = ".hc";
    public static final int FILE_READ_CHUNK_SIZE_THRESHOLD = Integer.MAX_VALUE / 32;
    public static final int FILE_WRITE_CHUNK_SIZE_THRESHOLD = (Integer.MAX_VALUE / 512 + 1) ;
    public static final String ID = "20010169";
    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
