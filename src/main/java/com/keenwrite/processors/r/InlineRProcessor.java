/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.processors.markdown.extensions.r.ROutputProcessor;
import com.keenwrite.util.InterpolatingMap;
import javafx.beans.property.Property;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.keenwrite.constants.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.KEY_R_DIR;
import static com.keenwrite.preferences.AppKeys.KEY_R_SCRIPT;
import static com.keenwrite.sigils.RSigilOperator.PREFIX;
import static com.keenwrite.sigils.RSigilOperator.SUFFIX;
import static java.lang.Math.min;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class InlineRProcessor extends VariableProcessor {
  private static final int PREFIX_LENGTH = PREFIX.length();

  /**
   * Converts the given string to HTML, trimming new lines, and inlining
   * the text if it is a paragraph. Otherwise, the resulting HTML is most likely
   * complex (e.g., a Markdown table) and should be rendered as its HTML
   * equivalent.
   */
  private final Processor<String> mPostProcessor = new ROutputProcessor();

  /**
   * Set to {@code true} when the R bootstrap script is loaded successfully.
   */
  private final AtomicBoolean mReady = new AtomicBoolean();

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
  }

  /**
   * Initializes the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   * <p>
   * If the R code to bootstrap contained variables, and they were all updated
   * successfully, this will update the internal ready flag to {@code true}.
   */
  public void init() {
    final var bootstrap = getBootstrapScript();

    if( !bootstrap.isBlank() ) {
      final var wd = getWorkingDirectory();
      final var dir = wd.toString().replace( '\\', '/' );
      final var definitions = getDefinitions();
      final var sigils = mWorkspace.createYamlSigilOperator();
      final var map = new InterpolatingMap( sigils, definitions );

      map.put( "application.r.working.directory", dir );
      map.put( "application.r.bootstrap", bootstrap );

      mReady.set( map.interpolate() == 0 );

      // If all existing variables were replaced---or there were no variables
      // to replace---initialize the R engine.
      if( mReady.get() ) {
        final var replaced = map.get( "application.r.bootstrap" );
        Engine.eval( replaced );
      }
    }
  }

  /**
   * Answers whether R has been initialized without failures.
   *
   * @return {@code true} the R engine is ready to process inline R statements.
   */
  public boolean isReady() {
    return mReady.get();
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
          sb.append( Engine.eval( r, mPostProcessor ) );
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
