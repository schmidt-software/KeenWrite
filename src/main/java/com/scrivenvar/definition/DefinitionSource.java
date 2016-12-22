/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.definition;

import java.util.Map;
import javafx.scene.control.TreeView;

/**
 * Represents behaviours for reading and writing variable definitions.
 *
 * @author White Magic Software, Ltd.
 */
public interface DefinitionSource {

  /**
   * Creates a TreeView from this definition source. The definition source is
   * responsible for observing the TreeView instance for changes and persisting
   * them, if needed.
   *
   * @return A hierarchical tree suitable for displaying in the definition pane.
   */
  public TreeView<String> asTreeView();

  /**
   * Returns all the strings with their values resolved in a flat hierarchy.
   * This copies all the keys and resolved values into a new map.
   *
   * @return The new map created with all values having been resolved,
   * recursively.
   */
  public Map<String, String> getResolvedMap();

  /**
   * Must return a re-loadable path to the data source. For a file, this is the
   * absolute file path. For a database, this could be the JDBC connection. For
   * a web site, this might be the GET URL.
   *
   * @return A non-null, non-empty string.
   */
  @Override
  public String toString();
}
