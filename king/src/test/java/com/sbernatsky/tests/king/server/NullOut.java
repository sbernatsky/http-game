package com.sbernatsky.tests.king.server;

import java.io.OutputStream;

public class NullOut extends OutputStream {

    @Override public void write(int b) {}
    @Override public void write(byte[] b) {}
    @Override public void write(byte[] b, int off, int len) { }
}
