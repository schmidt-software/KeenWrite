package com.keenwrite.cmdline;

import picocli.CommandLine;

import java.util.Map;
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
    defaultValue = "stdin"
  )
  private String mFileInput;

  @CommandLine.Option(
    names = {"-o", "--output"},
    description =
      "Set the file name to write.",
    paramLabel = "FILE",
    defaultValue = "stdout"
  )
  private String mFileOutput;

  @CommandLine.Option(
    names = {"-m", "--metadata"},
    description =
      "Map metadata keys to values.",
    paramLabel = "key=value"
  )
  private Map<String, String> mMetadata;

  @CommandLine.Option(
    names = {"-q", "--quiet"},
    description =
      "Suppress all status messages (${DEFAULT-VALUE}).",
    defaultValue = "false"
  )
  private boolean mQuiet;

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
