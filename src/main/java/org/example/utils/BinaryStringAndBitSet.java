package org.example.utils;

import java.util.BitSet;

public class BinaryStringAndBitSet {
    private BitSet fromString(String binary) {
        BitSet bitset = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                bitset.set(i);
            }
        }
        return bitset;
    }

    public String toBinaryString(BitSet bs) {
        StringBuilder sb = new StringBuilder(bs.length());
        for (int i = bs.length() - 1; i >= 0; i--)
            sb.append(bs.get(i) ? 1 : 0);
        return sb.toString();
    }
}
