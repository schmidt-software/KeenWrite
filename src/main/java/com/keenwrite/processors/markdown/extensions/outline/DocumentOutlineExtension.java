package com.keenwrite.processors.markdown.extensions.outline;

import com.keenwrite.events.ParseHeadingEvent;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.markdown.extensions.common.MarkdownParserExtension;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser.Builder;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static com.keenwrite.events.ParseHeadingEvent.fireNewOutlineEvent;

public final class DocumentOutlineExtension implements MarkdownParserExtension {
  private static final Pattern REGEX = Pattern.compile( "^(#+)" );

  private final Processor<String> mProcessor;

  private DocumentOutlineExtension( final Processor<String> processor ) {
    mProcessor = processor;
  }

  @Override
  public void extend( final Builder builder ) {
    builder.postProcessorFactory( new Factory() );
  }

  public static DocumentOutlineExtension create(
    final Processor<String> processor ) {
    return new DocumentOutlineExtension( processor );
  }

  private class HeadingNodePostProcessor extends NodePostProcessor {
    @Override
    public void process(
      @NotNull final NodeTracker state, @NotNull final Node node ) {
      final var heading = mProcessor.apply( node.getChars().toString() );
      final var matcher = REGEX.matcher( heading );

      if( matcher.find() ) {
        final var level = matcher.group().length();
        final var text = heading.substring( level );
        final var offset = node.getStartOffset();
        ParseHeadingEvent.fire( level, text, offset );
      }
    }
  }

  public class Factory extends NodePostProcessorFactory {
    public Factory() {
      super( false );
      addNodes( Heading.class );
    }

    @NotNull
    @Override
    public NodePostProcessor apply( @NotNull final Document document ) {
      fireNewOutlineEvent();
      return new HeadingNodePostProcessor();
    }
  }
}
