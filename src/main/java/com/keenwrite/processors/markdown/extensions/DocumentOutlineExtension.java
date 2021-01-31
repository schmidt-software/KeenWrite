package com.keenwrite.processors.markdown.extensions;

import com.keenwrite.processors.Processor;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser.Builder;
import com.vladsch.flexmark.parser.Parser.ParserExtension;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static com.keenwrite.events.ParseHeadingEvent.fireNewHeadingEvent;
import static com.keenwrite.events.ParseHeadingEvent.fireNewOutlineEvent;

public final class DocumentOutlineExtension implements ParserExtension {
  private static final Pattern sRegex = Pattern.compile( "^(#+)" );

  private final Processor<String> mProcessor;

  private DocumentOutlineExtension( final Processor<String> processor ) {
    mProcessor = processor;
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {}

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
      final var matcher = sRegex.matcher( heading );

      if( matcher.find() ) {
        final var level = matcher.group().length();
        final var text = heading.substring( level );
        final var offset = node.getStartOffset();
        fireNewHeadingEvent( level, text, offset );
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
