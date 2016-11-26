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
package com.scrivenvar.processors;

import javafx.scene.control.TreeView;

/**
 * Processes variables in the document and inserts their values into the
 * post-processed text.
 *
 * @author White Magic Software, Ltd.
 */
public class VariableNameProcessor extends AbstractProcessor<String> {

  private TreeView<String> treeView;

  /**
   * Constructs a new Markdown processor that can create HTML documents.
   *
   * @param successor Usually the HTML Preview Processor.
   */
  private VariableNameProcessor( final Processor<String> successor ) {
    super( successor );
  }

  public VariableNameProcessor( final Processor<String> successor, final TreeView<String> root ) {
    this( successor );
    setTreeView( root );
  }

  @Override
  public String processLink( final String text ) {
    final TreeView<String> root = getTreeView();
    final StringBuilder sb = new StringBuilder( text.length() * 2 );

    sb.append( text.replaceAll( "\\$c\\.protagonist\\.name\\.First\\$", "Chloe" ) );

    return sb.toString();
  }

  private TreeView<String> getTreeView() {
    return this.treeView;
  }

  private void setTreeView( final TreeView<String> treeView ) {
    this.treeView = treeView;
  }
}
