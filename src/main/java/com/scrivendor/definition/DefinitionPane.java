/*
 * Copyright 2016 White Magic Software, Inc.
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
package com.scrivendor.definition;

import com.scrivendor.Messages;
import com.scrivendor.ui.AbstractPane;
import static com.scrivendor.yaml.YamlTreeAdapter.adapt;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.Node;
import javafx.scene.control.TreeView;

/**
 * Provides a list of variables that can be referenced in the editor.
 *
 * @author White Magic Software, Ltd.
 */
public class DefinitionPane extends AbstractPane {

  private TreeView<String> treeView;

  public DefinitionPane() {
    try {
      setTreeView(
        adapt(
          asStream( "/com/scrivendor/variables.yaml" ),
          Messages.get( "Pane.defintion.node.root.title" )
        )
      );
    } catch( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  private InputStream asStream( String resource ) {
    return getClass().getResourceAsStream( resource );
  }

  public Node getNode() {
    return getTreeView();
  }

  public TreeView<String> getTreeView() {
    return treeView;
  }

  public final void setTreeView( TreeView<String> treeView ) {
    this.treeView = treeView;
  }
}
