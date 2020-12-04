/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors;

import com.keenwrite.AbstractFileFactory;
import com.keenwrite.sigils.RSigilOperator;
import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.sigils.YamlSigilOperator;

import java.nio.file.Path;

/**
 * Responsible for creating a definition name decorator suited to a particular
 * file type.
 */
public class DefinitionDecoratorFactory extends AbstractFileFactory {

  /**
   * Prevent instantiation.
   */
  private DefinitionDecoratorFactory() {
  }

  public static SigilOperator newInstance( final Path path ) {
    return switch( lookup( path ) ) {
      case RMARKDOWN, RXML -> new RSigilOperator();
      default -> new YamlSigilOperator();
    };
  }
}
