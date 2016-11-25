/*
 * The MIT License
 *
 * Copyright 2016 .
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
