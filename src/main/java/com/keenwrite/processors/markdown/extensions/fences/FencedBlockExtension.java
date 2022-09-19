/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.keenwrite.preview.DiagramUrlGenerator;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.keenwrite.processors.r.RChunkEvaluator;
import com.keenwrite.processors.r.RVariableProcessor;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.whitemagicsoftware.keenquotes.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.processors.IdentityProcessor.IDENTITY;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.renderer.CoreNodeRenderer.CODE_CONTENT;
import static com.vladsch.flexmark.html.renderer.LinkType.LINK;
import static java.lang.String.format;

/**
 * Responsible for converting textual diagram descriptions into HTML image
 * elements.
 */
public final class FencedBlockExtension extends HtmlRendererAdapter {
  private final static String TEMP_DIR = System.getProperty( "java.io.tmpdir" );

  /**
   * Ensure that the device is always closed to prevent an out-of-resources
   * error, regardless of whether the R expression the user tries to evaluate
   * is valid by swallowing errors alongside a {@code finally} block.
   */
  private final static String R_SVG_EXPORT =
    "tryCatch({svg('%s'%s)%n%s%n},finally={dev.off()})%n";

  private final static String STYLE_DIAGRAM = "diagram-";
  private final static int STYLE_DIAGRAM_LEN = STYLE_DIAGRAM.length();

  private final static String STYLE_R_CHUNK = "{r";

  private final static class VerbatimRVariableProcessor
    extends RVariableProcessor {

    public VerbatimRVariableProcessor(
      final Processor<String> successor, final ProcessorContext context ) {
      super( successor, context );
    }

    @Override
    protected String processValue( final String value ) {
      return value;
    }
  }

  private final RChunkEvaluator mRChunkEvaluator;
  private final Function<String, String> mInlineEvaluator;

  private final Processor<String> mRVariableProcessor;
  private final ProcessorContext mContext;

  public FencedBlockExtension(
    final Processor<String> processor,
    final Function<String, String> evaluator,
    final ProcessorContext context ) {
    assert processor != null;
    assert context != null;
    mContext = context;
    mRChunkEvaluator = new RChunkEvaluator();
    mInlineEvaluator = evaluator;
    mRVariableProcessor = new VerbatimRVariableProcessor( IDENTITY, context );
  }

  /**
   * Creates a new parser for fenced blocks. This calls out to a web service
   * to generate SVG files of text diagrams.
   * <p>
   * Internally, this creates a {@link VariableProcessor} to substitute
   * variable definitions. This is necessary because the order of processors
   * matters. If the {@link VariableProcessor} comes before an instance of
   * {@link MarkdownProcessor}, for example, then the caret position in the
   * preview pane will not align with the caret position in the editor
   * pane. The {@link MarkdownProcessor} must come before all else. However,
   * when parsing fenced blocks, the variables within the block must be
   * interpolated before being sent to the diagram web service.
   * </p>
   *
   * @param processor Used to pre-process the text.
   * @return A new {@link FencedBlockExtension} capable of shunting ASCII
   * diagrams to a service for conversion to SVG.
   */
  public static FencedBlockExtension create(
    final Processor<String> processor,
    final Function<String, String> evaluator,
    final ProcessorContext context ) {
    assert processor != null;
    assert context != null;
    return new FencedBlockExtension( processor, evaluator, context );
  }

  @Override
  public void extend(
    @NotNull final Builder builder, @NotNull final String rendererType ) {
    builder.nodeRendererFactory( new Factory() );
  }

  /**
   * Converts the given {@link BasedSequence} to a lowercase value.
   *
   * @param text The character string to convert to lowercase.
   * @return The lowercase text value, or the empty string for no text.
   */
  private static String sanitize( final BasedSequence text ) {
    assert text != null;
    return text.toString().toLowerCase();
  }

  /**
   * Responsible for generating images from a fenced block that contains a
   * diagram reference.
   */
  private class CustomRenderer implements NodeRenderer {

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      final var set = new HashSet<NodeRenderingHandler<?>>();

      set.add( new NodeRenderingHandler<>(
        FencedCodeBlock.class, ( node, context, html ) -> {
        final var style = sanitize( node.getInfo() );
        final Tuple<String, ResolvedLink> imagePair;

        if( style.startsWith( STYLE_DIAGRAM ) ) {
          imagePair = importTextDiagram( style, node, context );

          html.attr( "src", imagePair.item1() );
          html.withAttr( imagePair.item2() );
          html.tagVoid( "img" );
        }
        else if( style.startsWith( STYLE_R_CHUNK ) ) {
          imagePair = evaluateRChunk( node, context );

          html.attr( "src", imagePair.item1() );
          html.withAttr( imagePair.item2() );
          html.tagVoid( "img" );
        }
        else {
          // TODO: Revert to using context.delegateRender() after flexmark
          //   is updated to no longer trim blank lines up to the EOL.
          render( node, context, html );
        }
      } ) );

      return set;
    }

    private Tuple<String, ResolvedLink> importTextDiagram(
      final String style,
      final FencedCodeBlock node,
      final NodeRendererContext context ) {

      final var type = style.substring( STYLE_DIAGRAM_LEN );
      final var content = node.getContentChars().normalizeEOL();
      final var text = mInlineEvaluator.apply( content );
      final var server = mContext.getImageServer();
      final var source = DiagramUrlGenerator.toUrl( server, type, text );
      final var link = context.resolveLink( LINK, source, false );

      return new Tuple<>( source, link );
    }

    /**
     * Evaluates an R expression. This will take into consideration any
     * key/value pairs passed in from the document, such as width and height
     * attributes of the form: <code>{r width=5 height=5}</code>.
     *
     * @param node    The {@link FencedCodeBlock} to evaluate using R.
     * @param context Used to resolve the link that refers to any resulting
     *                image produced by the R chunk (such as a plot).
     * @return The SVG text string associated with the content produced by
     * the chunk (such as a graphical data plot).
     */
    @SuppressWarnings( "unused" )
    private Tuple<String, ResolvedLink> evaluateRChunk(
      final FencedCodeBlock node,
      final NodeRendererContext context ) {
      final var content = node.getContentChars().normalizeEOL().trim();
      final var text = mRVariableProcessor.apply( content );
      final var hash = Integer.toHexString( text.hashCode() );
      final var filename = format( "%s-%s.svg", APP_TITLE_LOWERCASE, hash );
      final var svg = Paths.get( TEMP_DIR, filename ).toString();
      final var link = context.resolveLink( LINK, svg, false );
      final var dimensions = getAttributes( node.getInfo() );
      final var r = format( R_SVG_EXPORT, svg, dimensions, text );
      final var result = mRChunkEvaluator.apply( r );

      return new Tuple<>( svg, link );
    }

    /**
     * Splits attributes of the form <code>{r key1=value2 key2=value2}</code>
     * into a comma-separated string containing only the key/value pairs,
     * such as <code>key1=value1,key2=value2</code>.
     *
     * @param bs The complete line after the fenced block demarcation.
     * @return A comma-separated string of name/value pairs.
     */
    private String getAttributes( final BasedSequence bs ) {
      final var result = new StringBuilder();
      final var split = bs.splitList( " " );
      final var splits = split.size();

      for( var i = 1; i < splits; i++ ) {
        final var based = split.get( i ).toString();
        final var attribute = based.replace( '}', ' ' );

        // The order of attribute evaluations is in order of performance.
        if( !attribute.isBlank() &&
          attribute.indexOf( '=' ) > 1 &&
          attribute.matches( ".*\\d.*" ) ) {

          // The comma will do double-duty for separating individual attributes
          // as well as being the comma that separates all attributes from the
          // SVG image file name.
          result.append( ',' ).append( attribute );
        }
      }

      return result.toString();
    }

    /**
     * This method is a stop-gap because blank lines that contain only
     * whitespace are collapsed into lines without any spaces. Consequently,
     * the typesetting software does not honour the blank lines, which
     * then would otherwise discard blank lines entirely.
     * <p>
     * Given the following:
     *
     * <pre>
     *   if( bool ) {
     *
     *
     *   }
     * </pre>
     * <p>
     * The typesetter would otherwise render this incorrectly as:
     *
     * <pre>
     *   if( bool ) {
     *   }
     * </pre>
     * <p>
     */
    private void render(
      final FencedCodeBlock node,
      final NodeRendererContext context,
      final HtmlWriter html ) {
      assert node != null;
      assert context != null;
      assert html != null;

      html.line();
      html.srcPosWithTrailingEOL( node.getChars() )
          .withAttr()
          .tag( "pre" )
          .openPre();

      final var options = context.getHtmlOptions();
      final var languageClass = lookupLanguageClass( node, options );

      if( !languageClass.isBlank() ) {
        html.attr( "class", languageClass );
      }

      html.srcPosWithEOL( node.getContentChars() )
          .withAttr( CODE_CONTENT )
          .tag( "code" );

      final var lines = node.getContentLines();

      for( final var line : lines ) {
        if( line.isBlank() ) {
          html.text( "    " );
        }

        html.text( line );
      }

      html.tag( "/code" );
      html.tag( "/pre" )
          .closePre();
      html.lineIf( options.htmlBlockCloseTagEol );
    }

    private String lookupLanguageClass(
      final FencedCodeBlock node,
      final HtmlRendererOptions options ) {
      assert node != null;
      assert options != null;

      final var info = node.getInfo();

      if( info.isNotNull() && !info.isBlank() ) {
        final var lang = node
          .getInfoDelimitedByAny( options.languageDelimiterSet )
          .unescape();
        return options
          .languageClassMap
          .getOrDefault( lang, options.languageClassPrefix + lang );
      }

      return options.noLanguageClass;
    }
  }

  private class Factory implements DelegatingNodeRendererFactory {
    public Factory() {}

    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return new CustomRenderer();
    }

    /**
     * Return {@code null} to indicate this may delegate to the core renderer.
     */
    @Override
    public Set<Class<?>> getDelegates() {
      return null;
    }
  }
}
