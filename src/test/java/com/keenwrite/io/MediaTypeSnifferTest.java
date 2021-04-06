package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import static com.keenwrite.io.MediaTypeExtension.valueFrom;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    final var urls = clazz.getClassLoader().getResources( dir + "/images" );
    assertTrue( urls.hasMoreElements() );

    while( urls.hasMoreElements() ) {
      final var url = urls.nextElement();
      final var path = new File( url.toURI().getPath() );

      for( final var image : path.listFiles() ) {
        final var media = MediaTypeSniffer.getMediaType( image );
        final var actualExtension = valueFrom( media ).getExtension();
        final var expectedExtension = getExtension( image.toString() );
        assertEquals( expectedExtension, actualExtension );
      }
    }
  }
}
