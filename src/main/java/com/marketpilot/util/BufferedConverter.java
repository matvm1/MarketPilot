package com.marketpilot.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class BufferedConverter {
    public static byte[] toBytes(char[] data) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(data));
        byteBuffer.flip();
        byte[] encoded = new byte[byteBuffer.remaining()];
        byteBuffer.get(encoded);

        fillZero(data);
        data = null;

        return encoded;
    }

    public static byte[] toBytes(String data) {
        char[] dataCharArr = data.toCharArray();
        data = null;

        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(dataCharArr));
        byte[] encoded = new byte[byteBuffer.remaining()];
        byteBuffer.get(encoded);

        fillZero(dataCharArr);
        dataCharArr = null;

        return encoded;
    }

    public static char[] toChars(byte[] data) {
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(data));
        char[] decoded = new char[charBuffer.remaining()];
        charBuffer.get(decoded);

        fillZero(data);
        data = null;

        return decoded;
    }

    private static void fillZero(char[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = '\0';
    }

    private static void fillZero(byte[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = (byte) 0;
    }
}
