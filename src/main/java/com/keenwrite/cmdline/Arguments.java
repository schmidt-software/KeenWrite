package com.keenwrite.cmdline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.keenwrite.ExportFormat;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorContext.Mutator;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.keenwrite.constants.Constants.DIAGRAM_SERVER_NAME;

/**
 * Responsible for mapping command-line arguments to keys that are used by
 * the application.
 */
@CommandLine.Command(
  name = "KeenWrite",
  mixinStandardHelpOptions = true,
  description = "Plain text editor for editing with variables"
)
@SuppressWarnings( "unused" )
public final class Arguments implements Callable<Integer> {
  @CommandLine.Option(
    names = {"--all"},
    description =
      "Concatenate files before processing (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mConcatenate;

  @CommandLine.Option(
    names = {"--keep-files"},
    description =
      "Retain temporary build files (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mKeepFiles;

  @CommandLine.Option(
    names = {"--curl-quotes"},
    description =
      "Replace straight quotes with curly quotes (${DEFAULT-VALUE})",
    defaultValue = "true"
  )
  private Boolean mCurlQuotes;

  @CommandLine.Option(
    names = {"-d", "--debug"},
    description =
      "Enable logging to the console (${DEFAULT-VALUE})",
    paramLabel = "Boolean",
    defaultValue = "false"
  )
  private boolean mDebug;

  @CommandLine.Option(
    names = {"-i", "--input"},
    description =
      "Source document file path",
    paramLabel = "PATH",
    defaultValue = "stdin",
    required = true
  )
  private Path mPathInput;

  @CommandLine.Option(
    names = {"--format-subtype"},
    description =
      "Export TeX subtype for HTML formats: svg, delimited",
    defaultValue = "svg",
    paramLabel = "String"
  )
  private String mFormatSubtype;

  @CommandLine.Option(
    names = {"--image-dir"},
    description =
      "Directory containing images",
    paramLabel = "DIR"
  )
  private File mImageDir;

  @CommandLine.Option(
    names = {"--image-order"},
    description =
      "Comma-separated image order (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "svg,pdf,png,jpg,tiff"
  )
  private String mImageOrder;

  @CommandLine.Option(
    names = {"--image-server"},
    description =
      "SVG diagram rendering service (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = DIAGRAM_SERVER_NAME
  )
  private String mImageServer;

  @CommandLine.Option(
    names = {"--locale"},
    description =
      "Set localization (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "en"
  )
  private String mLocale;

  @CommandLine.Option(
    names = {"-m", "--metadata"},
    description =
      "Map metadata keys to values, variable names allowed",
    paramLabel = "key=value"
  )
  private Map<String, String> mMetadata;

  @CommandLine.Option(
    names = {"-o", "--output"},
    description =
      "Destination document file path",
    paramLabel = "PATH",
    defaultValue = "stdout",
    required = true
  )
  private Path mPathOutput;

  @CommandLine.Option(
    names = {"-q", "--quiet"},
    description =
      "Suppress all status messages (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mQuiet;

  @CommandLine.Option(
    names = {"--r-dir"},
    description =
      "R working directory",
    paramLabel = "DIR"
  )
  private Path mRWorkingDir;

  @CommandLine.Option(
    names = {"--r-script"},
    description =
      "R bootstrap script file path",
    paramLabel = "PATH"
  )
  private Path mRScriptPath;

  @CommandLine.Option(
    names = {"--sigil-opening"},
    description =
      "Starting sigil for variable names (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "{{"
  )
  private String mSigilBegan;

  @CommandLine.Option(
    names = {"--sigil-closing"},
    description =
      "Ending sigil for variable names (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "}}"
  )
  private String mSigilEnded;

  @CommandLine.Option(
    names = {"--theme-dir"},
    description =
      "Theme directory",
    paramLabel = "DIR"
  )
  private Path mDirTheme;

  @CommandLine.Option(
    names = {"-v", "--variables"},
    description =
      "Variables file path",
    paramLabel = "PATH"
  )
  private Path mPathVariables;

  private final Consumer<Arguments> mLauncher;

  public Arguments( final Consumer<Arguments> launcher ) {
    mLauncher = launcher;
  }

  public ProcessorContext createProcessorContext()
    throws IOException {
    final var definitions = parse( mPathVariables );
    final var format = ExportFormat.valueFrom( mPathOutput, mFormatSubtype );
    final var locale = lookupLocale( mLocale );
    final var rScript = read( mRScriptPath );

    return ProcessorContext
      .builder()
      .with( Mutator::setInputPath, mPathInput )
      .with( Mutator::setOutputPath, mPathOutput )
      .with( Mutator::setExportFormat, format )
      .with( Mutator::setDefinitions, () -> definitions )
      .with( Mutator::setMetadata, () -> mMetadata )
      .with( Mutator::setLocale, () -> locale )
      .with( Mutator::setThemePath, () -> mDirTheme )
      .with( Mutator::setConcatenate, mConcatenate )
      .with( Mutator::setImageDir, () -> mImageDir )
      .with( Mutator::setImageServer, () -> mImageServer )
      .with( Mutator::setImageOrder, () -> mImageOrder )
      .with( Mutator::setSigilBegan, () -> mSigilBegan )
      .with( Mutator::setSigilEnded, () -> mSigilEnded )
      .with( Mutator::setRWorkingDir, () -> mRWorkingDir )
      .with( Mutator::setRScript, () -> rScript )
      .with( Mutator::setCurlQuotes, () -> mCurlQuotes )
      .with( Mutator::setAutoClean, () -> !mKeepFiles )
      .build();
  }

  public boolean quiet() {
    return mQuiet;
  }

  public boolean debug() {
    return mDebug;
  }

  /**
   * Launches the main application window. This is called when not running
   * in headless mode.
   *
   * @return {@code 0}
   * @throws Exception The application encountered an unrecoverable error.
   */
  @Override
  public Integer call() throws Exception {
    mLauncher.accept( this );
    return 0;
  }

  private static String read( final Path path ) throws IOException {
    return path == null ? "" : Files.readString( path );
  }

  /**
   * Parses the given YAML document into a map of key-value pairs.
   *
   * @param vars Variable definition file to read, may be {@code null} if no
   *             variables are specified.
   * @return A non-interpolated variable map, or an empty map.
   * @throws IOException Could not read the variable definition file
   */
  private static Map<String, String> parse( final Path vars )
    throws IOException {
    final var map = new HashMap<String, String>();

    if( vars != null ) {
      final var yaml = read( vars );
      final var factory = new YAMLFactory();
      final var json = new ObjectMapper( factory ).readTree( yaml );

      parse( json, "", map );
    }

    return map;
  }

  private static void parse(
    final JsonNode json, final String parent, final Map<String, String> map ) {
    assert json != null;
    assert parent != null;
    assert map != null;

    json.fields().forEachRemaining( node -> parse( node, parent, map ) );
  }

  private static void parse(
    final Entry<String, JsonNode> node,
    final String parent,
    final Map<String, String> map ) {
    assert node != null;
    assert parent != null;
    assert map != null;

    final var jsonNode = node.getValue();
    final var keyName = parent + "." + node.getKey();

    if( jsonNode.isValueNode() ) {
      // Trim the leading period, which is always present.
      map.put( keyName.substring( 1 ), node.getValue().asText() );
    }
    else if( jsonNode.isObject() ) {
      parse( jsonNode, keyName, map );
    }
  }

  private static Locale lookupLocale( final String locale ) {
    try {
      return Locale.forLanguageTag( locale );
    } catch( final Exception ex ) {
      return Locale.ENGLISH;
    }
  }
}
