package com.keenwrite.cmdline;

import com.keenwrite.ExportFormat;
import com.keenwrite.preferences.Key;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorContext.Mutator;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    names = {"--autoclean"},
    description =
      "Delete temporary build files (${DEFAULT-VALUE})",
    defaultValue = "true"
  )
  private boolean mAutoClean;

  @CommandLine.Option(
    names = {"--base-dir"},
    description =
      "Directories and paths relative to this one",
    paramLabel = "DIR"
  )
  private Path mBasePath;

  @CommandLine.Option(
    names = {"--curl-quotes"},
    description =
      "Replace straight quotes with curly quotes",
    paramLabel = "Boolean"
  )
  private Boolean mCurlQuotes;

  @CommandLine.Option(
    names = {"-d", "--debug"},
    description =
      "Enable logging to the console (${DEFAULT-VALUE})",
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
    names = {"--format-type"},
    description =
      "Export type: html, md, pdf, xml (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "pdf",
    required = true
  )
  private String mFormatType;

  @CommandLine.Option(
    names = {"--format-subtype-tex"},
    description =
      "Export subtype for HTML formats: svg, delimited",
    paramLabel = "String"
  )
  private String mFormatSubtype;

  @CommandLine.Option(
    names = {"--images-dir"},
    description =
      "Directory containing images",
    paramLabel = "DIR"
  )
  private File mImageDir;

  @CommandLine.Option(
    names = {"--image-extensions"},
    description =
      "Comma-separated image order (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "svg,pdf,png,jpg,tiff"
  )
  private String mImageOrder;

  @CommandLine.Option(
    names = {"--images-server"},
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
  private String mRWorkingDir;

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
      "Absolute theme directory, ignores base dir",
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

  private final Map<Key, Object> mValues = new HashMap<>();

  public Arguments( final Consumer<Arguments> launcher ) {
    mLauncher = launcher;
  }

  public ProcessorContext createProcessorContext() {
    final var definitions = interpolate( mPathVariables );
    final var format = ExportFormat.valueFrom( mFormatType, mFormatSubtype );
    final var locale = lookupLocale( mLocale );

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
      .with( Mutator::setCurlQuotes, () -> mCurlQuotes )
      .with( Mutator::setAutoClean, () -> mAutoClean )
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

  private static Map<String, String> interpolate( final Path vars ) {
    return new HashMap<>();
  }

  private static Locale lookupLocale( final String locale ) {
    try {
      return Locale.forLanguageTag( locale );
    } catch( final Exception ex ) {
      return Locale.ENGLISH;
    }
  }
}
