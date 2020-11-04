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
package com.keenwrite.processors;

import com.keenwrite.preferences.UserPreferences;
import com.keenwrite.preferences.UserPreferencesView;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.keenwrite.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static com.keenwrite.sigils.RSigilOperator.PREFIX;
import static com.keenwrite.sigils.RSigilOperator.SUFFIX;
import static java.lang.Math.min;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class InlineRProcessor extends DefinitionProcessor {
  /**
   * Constrain memory when typing new R expressions into the document.
   */
  private static final int MAX_CACHED_R_STATEMENTS = 512;

  private final MarkdownProcessor mMarkdownProcessor;

  /**
   * Where to put document inline evaluated R expressions.
   */
  private final Map<String, String> mEvalCache = new LinkedHashMap<>() {
    @Override
    protected boolean removeEldestEntry(
        final Map.Entry<String, String> eldest ) {
      return size() > MAX_CACHED_R_STATEMENTS;
    }
  };

  /**
   * Only one editor is open at a time.
   */
  private static final ScriptEngine ENGINE =
      (new ScriptEngineManager()).getEngineByName( "Renjin" );

  private static final int PREFIX_LENGTH = PREFIX.length();

  private final AtomicBoolean mDirty = new AtomicBoolean( false );

  /**
   * Constructs a processor capable of evaluating R statements.
   *
   * @param successor Subsequent link in the processing chain.
   * @param context   Contains resolved definitions map.
   */
  public InlineRProcessor(
      final Processor<String> successor,
      final ProcessorContext context ) {
    super( successor, context );

    mMarkdownProcessor = MarkdownProcessor.create( context );

    bootstrapScriptProperty().addListener(
        ( __, oldScript, newScript ) -> setDirty( true ) );
    workingDirectoryProperty().addListener(
        ( __, oldScript, newScript ) -> setDirty( true ) );

    getUserPreferencesView().addSaveEventHandler( ( handler ) -> {
      if( isDirty() ) {
        init();
        setDirty( false );
      }
    } );

    init();
  }

  /**
   * Initialises the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   */
  private void init() {
    final var bootstrap = getBootstrapScript();

    if( !bootstrap.isBlank() ) {
      final var wd = getWorkingDirectory();
      final var dir = wd.toString().replace( '\\', '/' );
      final var map = getDefinitions();
      final var prefs = UserPreferences.getInstance();
      final var defBegan = prefs.getDefDelimiterBegan();
      final var defEnded = prefs.getDefDelimiterEnded();

      map.put( defBegan + "application.r.working.directory" + defEnded, dir );

      eval( replace( bootstrap, map ) );
    }
  }

  /**
   * Sets the dirty flag to indicate that the bootstrap script or working
   * directory has been modified. Upon saving the preferences, if this flag
   * is true, then {@link #init()} will be called to reload the R environment.
   *
   * @param dirty Set to true to reload changes upon closing preferences.
   */
  private void setDirty( final boolean dirty ) {
    mDirty.set( dirty );
  }

  /**
   * Answers whether R-related settings have been modified.
   *
   * @return {@code true} when the settings have changed.
   */
  private boolean isDirty() {
    return mDirty.get();
  }

  /**
   * Evaluates all R statements in the source document and inserts the
   * calculated value into the generated document.
   *
   * @param text The document text that includes variables that should be
   *             replaced with values when rendered as HTML.
   * @return The generated document with output from all R statements
   * substituted with value returned from their execution.
   */
  @Override
  public String apply( final String text ) {
    final int length = text.length();

    // The * 2 is a wild guess at the ratio of R statements to the length
    // of text produced by those statements.
    final StringBuilder sb = new StringBuilder( length * 2 );

    int prevIndex = 0;
    int currIndex = text.indexOf( PREFIX );

    while( currIndex >= 0 ) {
      // Copy everything up to, but not including, the opening token.
      sb.append( text, prevIndex, currIndex );

      // Jump to the start of the R statement.
      prevIndex = currIndex + PREFIX_LENGTH;

      // Find the closing token, without indexing past the text boundary.
      currIndex = text.indexOf( SUFFIX, min( currIndex + 1, length ) );

      // Only evaluate inline R statements that have end delimiters.
      if( currIndex > 1 ) {
        // Extract the inline R statement to be evaluated.
        final String r = text.substring( prevIndex, currIndex );

        // Pass the R statement into the R engine for evaluation.
        try {
          final var result = evalCached( r );

          // Append the string representation of the result into the text.
          sb.append( result );
        } catch( final Exception e ) {
          // If the string couldn't be parsed using R, append the statement
          // that failed to parse, instead of its evaluated value.
          sb.append( PREFIX ).append( r ).append( SUFFIX );

          // Tell the user that there was a problem.
          clue( STATUS_PARSE_ERROR,
                e.getMessage(),
                currIndex );
        }

        // Retain the R statement's ending position in the text.
        prevIndex = currIndex + 1;
      }

      // Find the start of the next inline R statement.
      currIndex = text.indexOf( PREFIX, min( currIndex + 1, length ) );
    }

    // Copy from the previous index to the end of the string.
    return sb.append( text.substring( min( prevIndex, length ) ) ).toString();
  }

  /**
   * Look up an R expression from the cache then return the resulting object.
   * If the R expression hasn't been cached, it'll first be evaluated.
   *
   * @param r The expression to evaluate.
   * @return The object resulting from the evaluation.
   */
  private String evalCached( final String r ) {
    return mEvalCache.computeIfAbsent( r, v -> evalHtml( r ) );
  }

  /**
   * Converts the given string to HTML, trimming new lines, and inlining
   * the text if it is a paragraph. Otherwise, the resulting HTML is most likely
   * complex (e.g., a Markdown table) and should be rendered as its HTML
   * equivalent.
   *
   * @param r The R expression to evaluate then convert to HTML.
   * @return The result from the R expression as an HTML element.
   */
  private String evalHtml( final String r ) {
    final var markdown = eval( r );
    var node = mMarkdownProcessor.toNode( markdown ).getFirstChild();

    if( node != null && node.isOrDescendantOfType( Paragraph.class ) ) {
      node = new Text( node.getChars() );
    }

    // Trimming prevents displaced commas and unwanted newlines.
    return mMarkdownProcessor.toHtml( node ).trim();
  }

  /**
   * Evaluate an R expression and return the resulting object.
   *
   * @param r The expression to evaluate.
   * @return The object resulting from the evaluation.
   */
  private String eval( final String r ) {
    try {
      return ENGINE.eval( r ).toString();
    } catch( final Exception ex ) {
      final var expr = r.substring( 0, min( r.length(), 30 ) );
      clue( "Main.status.error.r", expr, ex.getMessage() );
      return "";
    }
  }

  /**
   * Return the given path if not {@code null}, otherwise return the path to
   * the user's directory.
   *
   * @return A non-null path.
   */
  private Path getWorkingDirectory() {
    return getUserPreferencesView().rDirectoryProperty().getValue().toPath();
  }

  private ObjectProperty<File> workingDirectoryProperty() {
    return getUserPreferencesView().rDirectoryProperty();
  }

  /**
   * Loads the R init script from the application's persisted preferences.
   *
   * @return A non-null string, possibly empty.
   */
  private String getBootstrapScript() {
    return getUserPreferencesView().rScriptProperty().getValue();
  }

  private StringProperty bootstrapScriptProperty() {
    return getUserPreferencesView().rScriptProperty();
  }

  private UserPreferencesView getUserPreferencesView() {
    return UserPreferencesView.getInstance();
  }
}
