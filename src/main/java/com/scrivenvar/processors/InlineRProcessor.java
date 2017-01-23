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

import static com.scrivenvar.Constants.PERSIST_R_STARTUP;
import static com.scrivenvar.Constants.STATUS_PARSE_ERROR;
import static com.scrivenvar.Messages.get;
import com.scrivenvar.Services;
import static com.scrivenvar.decorators.RVariableDecorator.PREFIX;
import static com.scrivenvar.decorators.RVariableDecorator.SUFFIX;
import static com.scrivenvar.processors.text.TextReplacementFactory.replace;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.events.Notifier;
import java.io.IOException;
import static java.lang.Math.min;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Transforms a document containing R statements into Markdown.
 *
 * @author White Magic Software, Ltd.
 */
public final class InlineRProcessor extends DefaultVariableProcessor {

  private final Notifier notifier = Services.load( Notifier.class );
  private final Options options = Services.load( Options.class );

  private ScriptEngine engine;

  /**
   * Constructs a processor capable of evaluating R statements.
   *
   * @param processor Subsequent link in the processing chain.
   * @param map Resolved definitions map.
   * @param path Path to the file being edited so that its working directory can
   * be extracted. Must not be null.
   */
  public InlineRProcessor(
    final Processor<String> processor,
    final Map<String, String> map,
    final Path path ) {
    super( processor, map );
    init( path.getParent() );
  }

  /**
   *
   * @param workingDirectory
   */
  private void init( final Path workingDirectory ) {
    try {
      final Path wd = nullSafe( workingDirectory );
      final String dir = wd.toString().replace( '\\', '/' );
      final Map<String, String> definitions = getDefinitions();
      definitions.put( "$application.r.working.directory$", dir );

      final String initScript = getInitScript();
      System.out.println( "script = '" + initScript + "'" );

      if( !initScript.isEmpty() ) {
        final String rScript = replace( initScript, getDefinitions() );
        eval( rScript );
      }
    } catch( final IOException | ScriptException e ) {
      throw new RuntimeException( e );
    }
  }

  private String getInitScript() throws IOException {
    return getOptions().get( PERSIST_R_STARTUP, "" );
  }

  @Override
  public String processLink( final String text ) {
    final int length = text.length();
    final int prefixLength = PREFIX.length();

    // Pre-allocate the same amount of space. A calculation is longer to write
    // than its computed value inserted into the text.
    final StringBuilder sb = new StringBuilder( length );

    int prevIndex = 0;
    int currIndex = text.indexOf( PREFIX );

    while( currIndex >= 0 ) {
      // Copy everything up to, but not including, an R statement (`r#).
      sb.append( text.substring( prevIndex, currIndex ) );

      // Jump to the start of the R statement.
      prevIndex = currIndex + prefixLength;

      // Find the statement ending (`), without indexing past the text boundary.
      currIndex = text.indexOf( SUFFIX, min( currIndex + 1, length ) );

      // Only evalutate inline R statements that have end delimiters.
      if( currIndex > 1 ) {
        // Extract the inline R statement to be evaluated.
        final String r = text.substring( prevIndex, currIndex );

        // Pass the R statement into the R engine for evaluation.
        try {
          final Object result = eval( r );

          // Append the string representation of the result into the text.
          sb.append( result );
        } catch( final Exception e ) {
          // If the string couldn't be parsed using R, append the statement
          // that failed to parse, instead of its evaluated value.
          sb.append( PREFIX ).append( r ).append( SUFFIX );

          // Tell the user that there was a problem.
          getNotifier().notify( get( STATUS_PARSE_ERROR,
            e.getMessage(), currIndex )
          );
        }

        // Retain the R statement's ending position in the text.
        prevIndex = currIndex + 1;
      }
      else {
        // TODO: Implement this.
        // There was a starting prefix but no ending suffix. Ignore the
        // problem, copy to the end, and exit the loop.
        //sb.append()
      }

      // Find the start of the next inline R statement.
      currIndex = text.indexOf( PREFIX, min( currIndex + 1, length ) );
    }

    // Copy from the previous index to the end of the string.
    return sb.append( text.substring( min( prevIndex, length ) ) ).toString();
  }

  /**
   * Evaluate an R expression and return the resulting object.
   *
   * @param r The expression to evaluate.
   *
   * @return The object resulting from the evaluation.
   */
  private Object eval( final String r ) throws ScriptException {
    return getScriptEngine().eval( r );
  }

  private synchronized ScriptEngine getScriptEngine() {
    if( this.engine == null ) {
      this.engine = (new ScriptEngineManager()).getEngineByName( "Renjin" );
    }

    return this.engine;
  }

  private Notifier getNotifier() {
    return this.notifier;
  }

  private Options getOptions() {
    return this.options;
  }

  /**
   * This will return the given path if not null, otherwise it will return
   * Paths.get( System.getProperty( "user.dir" ) ).
   *
   * @param path The path to make null safe.
   *
   * @return A non-null path.
   */
  private Path nullSafe( final Path path ) {
    return path == null ? Paths.get( System.getProperty( "user.dir" ) ) : path;
  }
}
