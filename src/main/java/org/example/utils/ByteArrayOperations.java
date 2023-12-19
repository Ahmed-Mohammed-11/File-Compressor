package org.example.utils;

import java.nio.ByteBuffer;
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

    public byte[] convertLongToByteArray(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public long convertByteArrayToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public byte[] convertIntToByteArray(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        return buffer.array();
    }

    public int convertByteArrayToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }
}