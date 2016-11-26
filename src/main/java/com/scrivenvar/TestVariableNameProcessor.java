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
package com.scrivenvar;

import com.scrivenvar.ui.VariableTreeItem;
import java.util.LinkedHashMap;
import java.util.Map;
import static java.util.concurrent.ThreadLocalRandom.current;
import static javafx.application.Application.launch;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import static org.apache.commons.lang.RandomStringUtils.randomAscii;

/**
 * Tests substituting variable definitions with their values in a swath of text.
 *
 * @author White Magic Software, Ltd.
 */
public class TestVariableNameProcessor extends TestHarness {

  private final static StringBuilder SOURCE
    = new StringBuilder( randomAscii( 1000 ) );

  public TestVariableNameProcessor() {
  }

  @Override
  public void start( final Stage stage ) throws Exception {
    super.start( stage );

    final TreeView<String> root = createTreeView();
    final LinkedHashMap<String, String> definitions = new LinkedHashMap<>();

    populate( createTreeView().getRoot(), definitions );
    injectVariables( definitions );
    
    System.out.println( SOURCE );
    System.exit( 0 );
  }

  private void injectVariables( final LinkedHashMap<String, String> definitions ) {
    for( int i = 5; i > 0; i-- ) {
      final int r = current().nextInt( 1, SOURCE.length() );
      SOURCE.insert( r, randomKey( definitions ) );
    }
  }

  private String randomKey( final LinkedHashMap<String, String> map ) {
    final int r = current().nextInt( 1, map.size() - 1 );
    return map.get( map.keySet().toArray()[ r ] );
  }

  private void populate( final TreeItem<String> parent, final Map<String, String> map ) {
    for( final TreeItem<String> child : parent.getChildren() ) {
      if( child.isLeaf() ) {
        map.put(
          asDefinition( ((VariableTreeItem<String>)child).toPath() ),
          child.getValue() );
      } else {
        populate( child, map );
      }
    }
  }

  private String asDefinition( final String variable ) {
    System.out.println( "VAR: " + variable );
    return String.format( "$%s$", variable );
  }
  
  public static void main( String[] args ) {
    launch( args );
  }
}
