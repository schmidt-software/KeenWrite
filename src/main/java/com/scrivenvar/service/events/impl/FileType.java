/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrivenvar.service.events.impl;

/**
 * Lists known file types for creating document processors via the factory.
 *
 * @author White Magic Software, Ltd.
 */
public enum FileType {
  MARKDOWN("md", "markdown", "mkdown", "mdown", "mkdn", "mkd", "mdwn", "mdtxt", "mdtext", "text", "txt"),
  R_MARKDOWN("Rmd"),
  XML("xml");

  private final String[] extensions;

  private FileType(final String... extensions) {
    this.extensions = extensions;
  }

  /**
   * Returns true if the given file type aligns with the extension for this
   * enumeration.
   *
   * @param filetype The file extension to compare against the internal list.
   * @return true The given filetype equals (case insensitive) the internal
   * type.
   */
  public boolean isType(final String filetype) {
    boolean result = false;

    for (final String extension : this.extensions) {
      if (extension.equalsIgnoreCase(filetype)) {
        result = true;
        break;
      }
    }

    return result;
  }
}
