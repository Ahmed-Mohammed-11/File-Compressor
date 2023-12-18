package org.example.utils;

import java.io.Serializable;
import java.util.Arrays;

public class ByteArray implements Serializable {
    public final byte[] bytes;

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArray b = (ByteArray) o;
        return Arrays.equals(bytes, b.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}