package com.cnblogs.yjmyzz.langchain4j.study.util;

import java.nio.charset.StandardCharsets;

public class MurmurHash {

    public static int murmur3_32Hash(String str) {
        return murmur3_32(str.getBytes(StandardCharsets.UTF_8), 0);
    }

    // MurmurHash3 32位实现
    private static int murmur3_32(byte[] data, int seed) {
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        final int r1 = 15;
        final int r2 = 13;
        final int m = 5;
        final int n = 0xe6546b64;

        int hash = seed;
        int length = data.length;
        int roundedEnd = length & 0xfffffffc; // 4字节对齐

        for (int i = 0; i < roundedEnd; i += 4) {
            int k = (data[i] & 0xff) |
                    ((data[i + 1] & 0xff) << 8) |
                    ((data[i + 2] & 0xff) << 16) |
                    ((data[i + 3] & 0xff) << 24);
            k *= c1;
            k = (k << r1) | (k >>> (32 - r1));
            k *= c2;

            hash ^= k;
            hash = (hash << r2) | (hash >>> (32 - r2));
            hash = hash * m + n;
        }

        // 处理剩余字节
        int k = 0;
        switch (length & 0x03) {
            case 3:
                k = (data[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k |= (data[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k |= (data[roundedEnd] & 0xff);
                k *= c1;
                k = (k << r1) | (k >>> (32 - r1));
                k *= c2;
                hash ^= k;
        }

        // 最终处理
        hash ^= length;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);

        return hash;
    }
}
