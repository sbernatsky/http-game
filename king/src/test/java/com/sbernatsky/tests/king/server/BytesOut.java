package com.sbernatsky.tests.king.server;

import java.io.OutputStream;

public class BytesOut extends OutputStream {
    final byte[] data = new byte[256];
    int count = 0;

    @Override public void write(int b) {
        data[count++] = (byte) b;
    }
    @Override public void write(byte[] b) {
        write(b, 0, b.length);
    }
    @Override public void write(byte[] b, int off, int len) {
        System.arraycopy(b, off, data, count, len);
        count += len;
    }

    public String getSession() {
        String result = new String(data, 0, count);
        count = 0;
        return result;
    }
}
