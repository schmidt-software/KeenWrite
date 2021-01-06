/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.processors.DefinitionProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.r.ROutputProcessor;
import com.keenwrite.util.BoundedCache;
import javafx.beans.property.Property;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.keenwrite.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.preferences.Workspace.*;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static com.keenwrite.sigils.RSigilOperator.PREFIX;
import static com.keenwrite.sigils.RSigilOperator.SUFFIX;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class InlineRProcessor extends DefinitionProcessor {
  private final Processor<String> mPostProcessor = new ROutputProcessor();

  /**
   * Where to put document inline evaluated R expressions, constrained to
   * avoid running out of memory.
   */
  private final Map<String, String> mEvalCache =
    new BoundedCache<>( 512 );

  private static final ScriptEngine ENGINE =
    (new ScriptEngineManager()).getEngineByName( "Renjin" );

  private static final int PREFIX_LENGTH = PREFIX.length();

  private final AtomicBoolean mDirty = new AtomicBoolean( false );

  private final Workspace mWorkspace;

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

    mWorkspace = context.getWorkspace();

    bootstrapScriptProperty().addListener(
      ( __, oldScript, newScript ) -> setDirty( true ) );
    workingDirectoryProperty().addListener(
      ( __, oldScript, newScript ) -> setDirty( true ) );

    // TODO: Watch the "R" property keys in the workspace, directly.

    // If the user saves the preferences, make sure that any R-related settings
    // changes are applied.
//    getWorkspace().addSaveEventHandler( ( handler ) -> {
//      if( isDirty() ) {
//        init();
//        setDirty( false );
//      }
//    } );

    init();
  }

  /**
   * Initialises the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   *
   * @return {@code true} if initialization completed and all variables were
   * replaced; {@code false} if any variables remain.
   */
  public boolean init() {
    final var bootstrap = getBootstrapScript();

    if( !bootstrap.isBlank() ) {
      final var wd = getWorkingDirectory();
      final var dir = wd.toString().replace( '\\', '/' );
      final var map = getDefinitions();
      final var defBegan = mWorkspace.toString( KEY_DEF_DELIM_BEGAN );
      final var defEnded = mWorkspace.toString( KEY_DEF_DELIM_ENDED );

      map.put( defBegan + "application.r.working.directory" + defEnded, dir );

      final var replaced = replace( bootstrap, map );
      final var bIndex = replaced.indexOf( defBegan );

      // If there's a delimiter in the replaced text it means not all variables
      // are bound, which is an error.
      if( bIndex >= 0 ) {
        var eIndex = replaced.indexOf( defEnded );
        eIndex = (eIndex == -1) ? replaced.length() - 1 : max( bIndex, eIndex );

        final var def = replaced.substring(
          bIndex + defBegan.length(), eIndex );
        clue( "Main.status.error.bootstrap.eval",
              format( "%s%s%s", defBegan, def, defEnded ) );

        return false;
      }
      else {
        eval( replaced );
      }
    }

    return true;
  }

  /**
   * Empties the cache.
   */
  public void clear() {
    mEvalCache.clear();
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
        final var r = text.substring( prevIndex, currIndex );

        // Pass the R statement into the R engine for evaluation.
        try {
          // Append the string representation of the result into the text.
          sb.append( evalCached( r ) );
        } catch( final Exception ex ) {
          // Inform the user that there was a problem.
          clue( STATUS_PARSE_ERROR, ex.getMessage(), currIndex );

          // If the string couldn't be parsed using R, append the statement
          // that failed to parse, instead of its evaluated value.
          sb.append( PREFIX ).append( r ).append( SUFFIX );
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
    return mEvalCache.computeIfAbsent( r, __ -> evalHtml( r ) );
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
    return mPostProcessor.apply( eval( r ) );
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
      final var expr = r.substring( 0, min( r.length(), 50 ) );
      clue( get( "Main.status.error.r", expr, ex.getMessage() ), ex );
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
    return workingDirectoryProperty().getValue().toPath();
  }

  private Property<File> workingDirectoryProperty() {
    return getWorkspace().fileProperty( KEY_R_DIR );
  }

  /**
   * Loads the R init script from the application's persisted preferences.
   *
   * @return A non-null string, possibly empty.
   */
  private String getBootstrapScript() {
    return bootstrapScriptProperty().getValue();
  }

  private Property<String> bootstrapScriptProperty() {
    return getWorkspace().valuesProperty( KEY_R_SCRIPT );
  }

  private Workspace getWorkspace() {
    return mWorkspace;
  }
}
