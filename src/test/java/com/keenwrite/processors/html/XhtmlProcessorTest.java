package com.keenwrite.processors.html;

import com.keenwrite.ExportFormat;
import com.keenwrite.editors.common.Caret;
import com.keenwrite.processors.ProcessorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

import static com.keenwrite.ExportFormat.HTML_TEX_DELIMITED;
import static com.keenwrite.ExportFormat.XHTML_TEX;
import static com.keenwrite.processors.ProcessorContext.builder;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class XhtmlProcessorTest {

  /**
   * Contains the thumbs up emoji.
   */
  private static final String EMOJI_MARKDOWN = "the \uD83D\uDC4D emoji";

  @ParameterizedTest
  @MethodSource( "formatParameters" )
  void test_Conversion_EmojiInput_EncodedEmoji(
    final ExportFormat format, final String expected ) {
    final var context = createProcessorContext( format );
    final var processor = createProcessors( context );
    final var actual = processor.apply( EMOJI_MARKDOWN );

    assertEquals( expected, actual );
  }

  private static ProcessorContext createProcessorContext(
    final ExportFormat format ) {
    final var caret = Caret.builder().build();
    return builder()
      .with( ProcessorContext.Mutator::setExportFormat, format )
      .with( ProcessorContext.Mutator::setSourcePath, Path.of( "f.md" ) )
      .with( ProcessorContext.Mutator::setDefinitions, HashMap::new )
      .with( ProcessorContext.Mutator::setLocale, () -> ENGLISH )
      .with( ProcessorContext.Mutator::setMetadata, HashMap::new )
      .with( ProcessorContext.Mutator::setThemeDir, () -> Path.of( "b" ) )
      .with( ProcessorContext.Mutator::setCaret, () -> caret )
      .with( ProcessorContext.Mutator::setImageDir, () -> new File( "i" ) )
      .with( ProcessorContext.Mutator::setImageOrder, () -> "" )
      .with( ProcessorContext.Mutator::setImageServer, () -> "" )
      .with( ProcessorContext.Mutator::setSigilBegan, () -> "" )
      .with( ProcessorContext.Mutator::setSigilEnded, () -> "" )
      .with( ProcessorContext.Mutator::setRScript, () -> "" )
      .with( ProcessorContext.Mutator::setRWorkingDir, () -> Path.of( "r" ) )
      .with( ProcessorContext.Mutator::setCurlQuotes, () -> true )
      .with( ProcessorContext.Mutator::setAutoRemove, () -> true )
      .build();
  }

  private static Stream<Arguments> formatParameters() {
    return Stream.of(
      Arguments.of(
        HTML_TEX_DELIMITED,
        """
          <html><head></head><body><p>the 👍 emoji</p>
          </body></html>"""
      ),
      Arguments.of(
        XHTML_TEX,
        """
          <html><head><title/><meta content="2" name="count"/></head><body><p>the 👍 emoji</p>
          </body></html>"""
      )
    );
  }
}
