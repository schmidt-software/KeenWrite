/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite;

import java.io.File;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Provides controls for processor behaviour when transforming input documents.
 */
public enum ExportFormat {

  /**
   * For HTML exports, encode TeX as SVG.
   */
  HTML_TEX_SVG( ".html" ),

  /**
   * For HTML exports, encode TeX using {@code $} delimiters, suitable for
   * rendering by an external TeX typesetting engine (or online with KaTeX).
   */
  HTML_TEX_DELIMITED( ".html" ),

  /**
   * Indicates that the processors should export to a Markdown format.
   */
  MARKDOWN_PLAIN( ".out.md" ),

  /**
   * Indicates no special export format is to be created. No extension is
   * applicable.
   */
  NONE( "" );

  /**
   * Preferred file name extension for the given file type.
   */
  private final String mExtension;

  private ExportFormat( final String extension ) {
    mExtension = extension;
  }

  public boolean isHtml() {
    return this == HTML_TEX_SVG || this == HTML_TEX_DELIMITED;
  }

  public boolean isMarkdown() {
    return this == MARKDOWN_PLAIN;
  }

  /**
   * Returns the given file renamed with the extension that matches this
   * {@link ExportFormat} extension.
   *
   * @param file The file to rename.
   * @return The renamed version of the given file.
   */
  public File toExportFilename( final File file ) {
    return new File( removeExtension( file.getName() ) + mExtension );
  }
}
