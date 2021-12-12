package com.keenwrite.cmdline;

import com.keenwrite.ExportFormat;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorContext.Mutator;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@CommandLine.Command(
  name = "KeenWrite",
  mixinStandardHelpOptions = true,
  description = "Plain text editor for editing with variables."
)
@SuppressWarnings( "unused" )
public final class Arguments implements Callable<Integer> {
  @CommandLine.Option(
    names = {"-a", "--all"},
    description =
      "Concatenate files in directory before processing (${DEFAULT-VALUE}).",
    defaultValue = "false"
  )
  private boolean mAll;

  @CommandLine.Option(
    names = {"-d", "--debug"},
    description =
      "Enable logging to the console (${DEFAULT-VALUE}).",
    defaultValue = "false"
  )
  private boolean mDebug;

  @CommandLine.Option(
    names = {"-i", "--input"},
    description =
      "Set the file name to read.",
    paramLabel = "FILE",
    defaultValue = "stdin",
    required = true
  )
  private File mFileInput;

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
      "Map metadata keys to values, variable names allowed.",
    paramLabel = "key=value"
  )
  private Map<String, String> mMetadata;

  @CommandLine.Option(
    names = {"-o", "--output"},
    description =
      "Set the file name to write.",
    paramLabel = "FILE",
    defaultValue = "stdout",
    required = true
  )
  private File mFileOutput;

  @CommandLine.Option(
    names = {"-p", "--images-path"},
    description =
      "Absolute path to images directory",
    paramLabel = "PATH"
  )
  private Path mImages;

  @CommandLine.Option(
    names = {"-q", "--quiet"},
    description =
      "Suppress all status messages (${DEFAULT-VALUE}).",
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
      "Full theme name file path to use when exporting as a PDF file.",
    paramLabel = "PATH"
  )
  private String mThemeName;

  @CommandLine.Option(
    names = {"-x", "--image-extensions"},
    description =
      "Space-separated image file name extensions (${DEFAULT-VALUE}).",
    paramLabel = "String",
    defaultValue = "svg pdf png jpg tiff"
  )
  private Set<String> mExtensions;

  @CommandLine.Option(
    names = {"-v", "--variables"},
    description =
      "Set the file name containing variable definitions (${DEFAULT-VALUE}).",
    paramLabel = "FILE",
    defaultValue = "variables.yaml"
  )
  private String mFileVariables;

  private final Consumer<Arguments> mLauncher;

  public Arguments( final Consumer<Arguments> launcher ) {
    mLauncher = launcher;
  }

  public ProcessorContext createProcessorContext() {
    final var format = ExportFormat.valueFrom( mFormatType, mFormatSubtype );
    return ProcessorContext
      .builder()
      .with( Mutator::setInputPath, mFileInput )
      .with( Mutator::setOutputPath, mFileOutput )
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
}
