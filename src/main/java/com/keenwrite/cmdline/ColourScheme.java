package com.keenwrite.cmdline;

import static picocli.CommandLine.Help.Ansi.Style.*;
import static picocli.CommandLine.Help.ColorScheme;
import static picocli.CommandLine.Help.ColorScheme.Builder;

/**
 * Responsible for creating the command-line parser's colour scheme.
 */
public class ColourScheme {
  public static ColorScheme create() {
    return new Builder()
      .commands( bold )
      .options( fg_blue, bold )
      .parameters( fg_blue )
      .optionParams( italic )
      .errors( fg_red, bold )
      .stackTraces( italic )
      .build();
  }
}
