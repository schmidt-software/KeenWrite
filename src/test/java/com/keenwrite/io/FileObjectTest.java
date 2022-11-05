package com.keenwrite.io;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.Disabled;
import org.renjin.eval.SessionBuilder;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static java.io.File.separator;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests file resource allocation.
 */
public class FileObjectTest {
  private static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );

  /**
   * Test that resources are not exhausted.
   *
   * Disabled because no issue was found and this test thrashes the I/O.
   */
  @Disabled
  void test_Open_MultipleFiles_NoResourcesExpire() throws FileSystemException {
    final var builder = new SessionBuilder();
    final var session = builder.build();

    for( int i = 0; i < 10000; i++ ) {
      final var filename = format( "%s%s%d.txt", TEMP_DIR, separator, i );
      final var fileObject = session
        .getFileSystemManager()
        .resolveFile( filename );

      try(
        final var stream = fileObject.getContent().getOutputStream();
        final var writer = new OutputStreamWriter( stream, UTF_8 ) ) {
        writer.write( "contents" );
      } catch( final IOException e ) {
        throw new FileSystemException( e );
      }

      fileObject.delete();
    }
  }
}
