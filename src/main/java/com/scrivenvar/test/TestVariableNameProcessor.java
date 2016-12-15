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
package com.scrivenvar.test;

import com.scrivenvar.definition.VariableTreeItem;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static java.util.concurrent.ThreadLocalRandom.current;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.application.Application.launch;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.ahocorasick.trie.*;
import org.ahocorasick.trie.Trie.TrieBuilder;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import org.apache.commons.lang3.StringUtils;

/**
 * Tests substituting variable definitions with their values in a swath of text.
 *
 * @author White Magic Software, Ltd.
 */
public class TestVariableNameProcessor extends TestHarness {

  private final static int TEXT_SIZE = 1000000;
  private final static int MATCHES_DIVISOR = 1000;

  private final static StringBuilder SOURCE
    = new StringBuilder( randomNumeric( TEXT_SIZE ) );

  private final static boolean DEBUG = false;

  public TestVariableNameProcessor() {
  }

  @Override
  public void start( final Stage stage ) throws Exception {
    super.start( stage );

    final TreeView<String> treeView = createTreeView();
    final Map<String, String> definitions = new HashMap<>();

    populate( treeView.getRoot(), definitions );
    injectVariables( definitions );

    final String text = SOURCE.toString();

    show( text );

    long duration = System.nanoTime();

    // TODO: Test replaceEach (with intercoluated variables) and replaceEachRepeatedly
    // (without intercoluation).
    final String result = testBorAhoCorasick( text, definitions );

    duration = System.nanoTime() - duration;

    show( result );
    System.out.println( elapsed( duration ) );

    System.exit( 0 );
  }

  private void show( final String s ) {
    if( DEBUG ) {
      System.out.printf( "%s\n\n", s );
    }
  }

  private String testBorAhoCorasick(
    final String text,
    final Map<String, String> definitions ) {
    // Create a buffer sufficiently large that re-allocations are minimized.
    final StringBuilder sb = new StringBuilder( text.length() << 1 );

    final TrieBuilder builder = Trie.builder();
    builder.onlyWholeWords();
    builder.removeOverlaps();

    final String[] keys = keys( definitions );

    for( final String key : keys ) {
      builder.addKeyword( key );
    }

    final Trie trie = builder.build();
    final Collection<Emit> emits = trie.parseText( text );

    int prevIndex = 0;

    for( final Emit emit : emits ) {
      final int matchIndex = emit.getStart();

      sb.append( text.substring( prevIndex, matchIndex ) );
      sb.append( definitions.get( emit.getKeyword() ) );
      prevIndex = emit.getEnd() + 1;
    }

    // Add the remainder of the string (contains no more matches).
    sb.append( text.substring( prevIndex ) );

    return sb.toString();
  }

  private String testStringUtils(
    final String text, final Map<String, String> definitions ) {
    final String[] keys = keys( definitions );
    final String[] values = values( definitions );

    return StringUtils.replaceEach( text, keys, values );
  }

  private String[] keys( final Map<String, String> definitions ) {
    final int size = definitions.size();
    return definitions.keySet().toArray( new String[ size ] );
  }

  private String[] values( final Map<String, String> definitions ) {
    final int size = definitions.size();
    return definitions.values().toArray( new String[ size ] );
  }

  /**
   * Decomposes a period of time into days, hours, minutes, seconds,
   * milliseconds, and nanoseconds.
   *
   * @param duration Time in nanoseconds.
   *
   * @return A non-null, comma-separated string (without newline).
   */
  public String elapsed( long duration ) {
    final TimeUnit scale = NANOSECONDS;

    long days = scale.toDays( duration );
    duration -= DAYS.toMillis( days );
    long hours = scale.toHours( duration );
    duration -= HOURS.toMillis( hours );
    long minutes = scale.toMinutes( duration );
    duration -= MINUTES.toMillis( minutes );
    long seconds = scale.toSeconds( duration );
    duration -= SECONDS.toMillis( seconds );
    long millis = scale.toMillis( duration );
    duration -= MILLISECONDS.toMillis( seconds );
    long nanos = scale.toNanos( duration );

    return String.format(
      "%d days, %d hours, %d minutes, %d seconds, %d millis, %d nanos",
      days, hours, minutes, seconds, millis, nanos
    );
  }

  private void injectVariables( final Map<String, String> definitions ) {
    for( int i = (SOURCE.length() / MATCHES_DIVISOR) + 1; i > 0; i-- ) {
      final int r = current().nextInt( 1, SOURCE.length() );
      SOURCE.insert( r, randomKey( definitions ) );
    }
  }

  private String randomKey( final Map<String, String> map ) {
    final Object[] keys = map.keySet().toArray();
    final int r = current().nextInt( keys.length );
    return keys[ r ].toString();
  }

  private void populate( final TreeItem<String> parent, final Map<String, String> map ) {
    for( final TreeItem<String> child : parent.getChildren() ) {
      if( child.isLeaf() ) {
        final String key = asDefinition( ((VariableTreeItem<String>)child).toPath() );
        final String value = child.getValue();

        map.put( key, value );
      } else {
        populate( child, map );
      }
    }
  }

  private String asDefinition( final String key ) {
    return "$" + key + "$";
  }

  public static void main( String[] args ) {
    launch( args );
  }
}
