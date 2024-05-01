package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import java.io.File;

import static com.keenwrite.io.MediaTypeExtension.valueFrom;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Responsible for testing that {@link MediaTypeSniffer} can return the
 * correct IANA-defined {@link MediaType} for known file types.
 */
class MediaTypeSnifferTest {
  @Test
  void test_Read_KnownFileTypes_MediaTypeReturned()
    throws Exception {
    final var clazz = getClass();
    final var pkgName = clazz.getPackageName();
    final var dir = pkgName.replace( '.', '/' );

    final var urls = clazz.getClassLoader().getResources( STR."\{dir}/images" );
    assertTrue( urls.hasMoreElements() );

    while( urls.hasMoreElements() ) {
      final var url = urls.nextElement();
      final var path = new File( url.toURI().getPath() );
      final var files = path.listFiles();
      assertNotNull( files );

      for( final var image : files ) {
        final var media = MediaTypeSniffer.getMediaType( image );
        final var actualExtension = valueFrom( media ).getExtension();
        final var expectedExtension = getExtension( image.toString() );
        System.out.println( STR."\{image} -> \{media}" );

        assertEquals( expectedExtension, actualExtension );
      }
    }
  }
}
