package com.keenwrite.cmdline;

import com.keenwrite.MainApp;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.logging.LogManager;

@CommandLine.Command(
  name = "KeenWrite",
  mixinStandardHelpOptions = true,
  description = "Plain text editor that offers interpolated variables."
)
@SuppressWarnings( "unused" )
public final class Arguments implements Callable<Integer> {
  @CommandLine.Option(
    names = {"-d", "--debug"},
    description =
      "Enable logging to the console (${DEFAULT-VALUE}).",
    arity = "1",
    paramLabel = "Boolean",
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
    names = {"-m", "--meta-data"},
    description =
      "Set a meta-data value.",
    paramLabel = "Pair"
  )
  private String mMetaData;

  @CommandLine.Option(
    names = {"-v", "--variables"},
    description =
      "Set the file name containing variable definitions (${DEFAULT-VALUE}).",
    paramLabel = "FILE",
    defaultValue = "variables.yaml"
  )
  private String mFileVariables;

  private final String[] mArgs;

  public Arguments( final String[] args ) {
    mArgs = args;
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
    if( !mDebug ) {
      disableLogging();
    }

    MainApp.main( mArgs );
    return 0;
  }

  /**
   * Suppress logging to standard output and standard error.
   */
  private static void disableLogging() {
    LogManager.getLogManager().reset();
    System.err.close();
  }
}
