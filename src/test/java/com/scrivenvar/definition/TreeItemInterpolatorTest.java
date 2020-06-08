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
package com.scrivenvar.definition;

import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeItemInterpolatorTest {

  private final static String AUTHOR_FIRST = "FirstName";
  private final static String AUTHOR_LAST = "LastName";
  private final static String AUTHOR_ALL = "$root.name.first$ $root.name.last$";

  /**
   * Test that a hierarchical relationship of {@link TreeItem} instances can
   * create a flat map with all string values containing key names interpolated.
   */
  @Test
  public void test_Resolve_ReferencesInTree_InterpolatedMap() {
    final var root = new TreeItem<>( "root" );
    final var name = new TreeItem<>( "name" );
    final var first = new TreeItem<>( "first" );
    final var authorFirst = new TreeItem<>( AUTHOR_FIRST );
    final var last = new TreeItem<>( "last" );
    final var authorLast = new TreeItem<>( AUTHOR_LAST );
    final var full = new TreeItem<>( "full" );
    final var expr = new TreeItem<>( AUTHOR_ALL );

    root.getChildren().add( name );
    name.getChildren().add( first );
    name.getChildren().add( last );
    name.getChildren().add( full );

    first.getChildren().add( authorFirst );
    last.getChildren().add( authorLast );
    full.getChildren().add( expr );

    final var map = TreeItemInterpolator.toMap( root );

    var actualAuthor = map.get( "$root.name.full$" );
    var expectedAuthor = AUTHOR_ALL;
    assertEquals( expectedAuthor, actualAuthor );

    TreeItemInterpolator.interpolate( map );
    actualAuthor = map.get( "$root.name.full$" );

    expectedAuthor = format( "%s %s", AUTHOR_FIRST, AUTHOR_LAST );
    assertEquals( expectedAuthor, actualAuthor );
  }
}
