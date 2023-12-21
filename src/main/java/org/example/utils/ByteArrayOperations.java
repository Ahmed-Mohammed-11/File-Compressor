package org.example.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteArrayOperations {
    public String convertByteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.US_ASCII);
    }

    public byte[] convertBinaryStringToByteArray(String string) {
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

    public String convertByteArrayToBinaryString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            char[] chars = convertByteToString(b);
            stringBuilder.append(chars);
        }
        return stringBuilder.toString();
    }

    public char[] convertByteToString(byte b) {
        char[] chars = new char[8];
        for (int i = 0; i < 8; i++) {
            chars[7 - i] = (char) (((b >> i) & 1) + '0');
        }
        return chars;
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

    public byte[] convertByteToByteArray(byte huff) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put(huff);
        return buffer.array();
    }
}