package org.example.utils;

import java.nio.charset.StandardCharsets;

public class ByteArrayOperations {
    public String convertByteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public byte[] convertStringToByteArray(String string) {
        byte oneByte;
        int byteArrLength = string.length() / 8 + (string.length() % 8 == 0 ? 0 : 1);
        byte[] allBytesToWrite = new byte[byteArrLength];
        int k = 0;
        for (int i = 0; i < string.length() / 8; i += 1) {
            oneByte = (byte) Integer.parseInt(string.substring(i * 8, (i + 1) * 8), 2);
            allBytesToWrite[k++] = oneByte;
        }
        if (string.length() % 8 != 0) {
            oneByte = (byte) Integer.parseInt(string.substring(string.length() - string.length() % 8), 2);
            allBytesToWrite[k] = oneByte;
        }

        return allBytesToWrite;
    }

    public byte[] convertLongToByteArray(Long fileSize) {
        byte[] longInBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            longInBytes[i] = (byte) (fileSize >>> (i * 8));
        }
        return longInBytes;
    }

    public byte[] convertIntToByteArray(int i) {
        byte[] intInBytes = new byte[4];
        for (int j = 0; j < 4; j++) {
            intInBytes[j] = (byte) (i >>> (j * 8));
        }
        return intInBytes;
    }
}