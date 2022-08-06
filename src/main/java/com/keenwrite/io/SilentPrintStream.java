package com.keenwrite.io;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Responsible for silently discarding all data written to the stream.
 */
public final class SilentPrintStream extends PrintStream {

  private final static class SilentOutputStream extends OutputStream {
    public void write( final int b ) {}
  }

  public SilentPrintStream() {
    super( new SilentOutputStream() );
  }
}
