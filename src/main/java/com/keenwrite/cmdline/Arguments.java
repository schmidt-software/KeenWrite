package com.keenwrite.cmdline;

import com.keenwrite.ExportFormat;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.KeyConfiguration;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorContext.Mutator;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.keenwrite.preferences.AppKeys.*;

/**
 * Responsible for mapping command-line arguments to keys that are used by
 * the application. This class implements the {@link KeyConfiguration} as
 * an abstraction so that the CLI and GUI can reuse the same code, but without
 * the CLI needing to instantiate or initialize JavaFX.
 */
@CommandLine.Command(
  name = "KeenWrite",
  mixinStandardHelpOptions = true,
  description = "Plain text editor for editing with variables"
)
@SuppressWarnings( "unused" )
public final class Arguments implements Callable<Integer>, KeyConfiguration {
  @CommandLine.Option(
    names = {"-a", "--all"},
    description =
      "Concatenate files in directory before processing (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mAll;

  @CommandLine.Option(
    names = {"-b", "--base-path"},
    description =
      "Set all other paths relative to this path",
    paramLabel = "PATH"
  )
  private Path mBasePath;

  @CommandLine.Option(
    names = {"-k", "--keep-files"},
    description =
      "Keep temporary build files (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mKeepFiles;

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
      "Set the file name to read",
    paramLabel = "PATH",
    defaultValue = "stdin",
    required = true
  )
  private Path mPathInput;

  @CommandLine.Option(
    names = {"-f", "--format-type"},
    description =
      "Export type: html, md, pdf, xml (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "pdf",
    required = true
  )
  private String mFormatType;

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
      "Set the file name to write",
    paramLabel = "PATH",
    defaultValue = "stdout",
    required = true
  )
  private File mPathOutput;

  @CommandLine.Option(
    names = {"-p", "--images-path"},
    description =
      "Directory containing images",
    paramLabel = "PATH"
  )
  private Path mPathImages;

  @CommandLine.Option(
    names = {"-q", "--quiet"},
    description =
      "Suppress all status messages (${DEFAULT-VALUE})",
    defaultValue = "false"
  )
  private boolean mQuiet;

  @CommandLine.Option(
    names = {"-s", "--format-subtype-tex"},
    description =
      "Export subtype for HTML formats: svg, delimited",
    paramLabel = "String"
  )
  private String mFormatSubtype;

  @CommandLine.Option(
    names = {"-t", "--theme"},
    description =
      "File path to use when exporting as a PDF file",
    paramLabel = "PATH"
  )
  private Path mThemeName;

  @CommandLine.Option(
    names = {"-x", "--image-extensions"},
    description =
      "Space-separated image file name extensions (${DEFAULT-VALUE})",
    paramLabel = "String",
    defaultValue = "svg pdf png jpg tiff"
  )
  private Set<String> mImageExtensions;

  @CommandLine.Option(
    names = {"-v", "--variables"},
    description =
      "Set the file name containing variable definitions (${DEFAULT-VALUE})",
    paramLabel = "FILE",
    defaultValue = "variables.yaml"
  )
  private Path mPathVariables;

  private final Consumer<Arguments> mLauncher;

  private final Map<Key, Object> mValues = new HashMap<>();

  public Arguments( final Consumer<Arguments> launcher ) {
    mLauncher = launcher;
  }

  public ProcessorContext createProcessorContext() {
    mValues.put( KEY_UI_RECENT_DOCUMENT, mPathInput );
    mValues.put( KEY_UI_RECENT_DEFINITION, mPathVariables );
    mValues.put( KEY_UI_RECENT_EXPORT, mPathOutput );
    mValues.put( KEY_IMAGES_DIR, mPathImages );
    mValues.put( KEY_TYPESET_CONTEXT_THEMES_PATH, mThemeName.getParent() );
    mValues.put( KEY_TYPESET_CONTEXT_THEME_SELECTION,
                 mThemeName.getFileName() );
    mValues.put( KEY_TYPESET_CONTEXT_CLEAN, !mKeepFiles );

    final var format = ExportFormat.valueFrom( mFormatType, mFormatSubtype );

    return ProcessorContext
      .builder()
      .with( Mutator::setInputPath, mPathInput )
      .with( Mutator::setOutputPath, mPathOutput )
      .with( Mutator::setExportFormat, format )
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

  @Override
  public String getString( final Key key ) {
    return null;
  }

  @Override
  public boolean getBoolean( final Key key ) {
    return false;
  }

  @Override
  public int getInteger( final Key key ) {
    return 0;
  }

  @Override
  public double getDouble( final Key key ) {
    return 0;
  }

  @Override
  public File getFile( final Key key ) {
    return null;
  }
}
