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
package com.scrivendor.yaml;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.DumperOptions;

/**
 * <p>
 * This program loads a YAML document into memory, scans for variable
 * declarations, then substitutes any self-referential values back into the
 * document. Its output is the given YAML document without any variables.
 * Variables in the YAML document are denoted using a bracketed dollar symbol
 * syntax. For example: $field.name$. Some nomenclature to keep from going
 * squirrely, consider:
 * </p>
 *
 * <pre>
 *   root:
 *     node:
 *       name: $field.name$
 *   field:
 *     name: Alan Turing
 * </pre>
 *
 * The various components of the given YAML are called:
 *
 * <ul>
 * <li><code>$field.name$</code> - delimited reference</li>
 * <li><code>field.name</code> - reference</li>
 * <li><code>name</code> - YAML field</li>
 * <li><code>Alan Turing</code> - (dereferenced) field value</li>
 * </ul>
 *
 * @author White Magic Software, Ltd.
 */
public class YamlParser {

  private final static int GROUP_DELIMITED = 1;
  private final static int GROUP_REFERENCE = 2;

  /**
   * Matches variables delimited by dollar symbols. The outer group is necessary
   * for substring replacement of delimited references.
   */
  private final static String DEFAULT_REGEX = "(\\$(.*?)\\$)";

  /**
   * Compiled version of DEFAULT_REGEX.
   */
  private final static Pattern REGEX_PATTERN = Pattern.compile( DEFAULT_REGEX );

  /**
   * Separates variable nodes (e.g., the dots in <code>$root.node.var$</code>).
   */
  private final static String SEPARATOR_VARIABLE = ".";

  /**
   * Should be JsonPointer.SEPARATOR, but Jackson YAML uses magic values.
   */
  private final static char SEPARATOR_YAML = '/';

  /**
   * Start of the Universe.
   */
  private ObjectNode documentRoot;

  /**
   * Map of references to dereferenced field values.
   */
  private Map<String, String> references;

  protected YamlParser() {
  }

  /**
   * Reads the first document from the given stream of YAML data and returns a
   * corresponding object that represents the YAML hierarchy. The calling class
   * is responsible for closing the stream. Calling classes should use
   * <code>JsonNode.fields()</code> to walk through the YAML tree of fields.
   *
   * @param in The input stream containing YAML content.
   *
   * @return An object hierarchy to represent the content.
   *
   * @throws IOException Could not read the stream.
   */
  public static JsonNode parse( final InputStream in ) throws IOException {
    return (new YamlParser()).process( in );
  }

  /**
   * Read and process the contents from an open stream. The stream remains open
   * after calling this method, regardless of success or error.
   *
   * @param in The stream with a YAML document to process.
   *
   * @throws IOException Could not read the file contents.
   */
  private JsonNode process( final InputStream in ) throws IOException {
    ObjectNode root = (ObjectNode)getObjectMapper().readTree( in );
    setDocumentRoot( root );
    process( root );
    return getDocumentRoot();
  }

  /**
   * Iterate over a given root node (at any level of the tree) and process each
   * leaf node.
   *
   * @param root A node to process.
   */
  private void process( final JsonNode root ) {
    root.fields().forEachRemaining( this::process );
  }

  /**
   * Process the given field, which is a named node. This is where the
   * application does the up-front work of mapping references to their fully
   * recursively dereferenced values.
   *
   * @param field The named node.
   */
  private void process( final Entry<String, JsonNode> field ) {
    final JsonNode node = field.getValue();

    if( node.isObject() ) {
      process( node );
    } else {
      final JsonNode fieldValue = field.getValue();

      // Only basic data types can be parsed into variable values. For
      // node structures, YAML has a built-in mechanism.
      if( fieldValue.isValueNode() ) {
        try {
          resolve( fieldValue.asText() );
        } catch( StackOverflowError e ) {
          throw new IllegalArgumentException(
            "Unresolvable: " + node.textValue() + " = " + fieldValue );
        }
      }
    }
  }

  /**
   * Inserts the delimited references and field values into the cache. This will
   * overwrite existing references.
   *
   * @param fieldValue YAML field containing zero or more delimited references.
   * If it contains a delimited reference, the parameter is modified with the
   * dereferenced value before it is returned.
   *
   * @return fieldValue without delimited references.
   */
  private String resolve( String fieldValue ) {
    final Matcher matcher = patternMatch( fieldValue );

    while( matcher.find() ) {
      final String delimited = matcher.group( GROUP_DELIMITED );
      final String reference = matcher.group( GROUP_REFERENCE );
      final String dereference = resolve( lookup( reference ) );

      fieldValue = fieldValue.replace( delimited, dereference );

      // This will perform some superfluous calls by overwriting existing
      // items in the delimited reference map.
      put( delimited, dereference );
    }

    return fieldValue;
  }

  /**
   * Inserts a key/value pair into the references map. The map retains
   * references and dereferenced values found in the YAML. If the reference
   * already exists, this will overwrite with a new value.
   *
   * @param delimited The variable name.
   * @param dereferenced The resolved value.
   */
  private void put( String delimited, String dereferenced ) {
    if( dereferenced.isEmpty() ) {
      missing( delimited );
    } else {
      getReferences().put( delimited, dereferenced );
    }
  }

  /**
   * Returns the given string with all the delimited references swapped with
   * their recursively resolved values.
   *
   * @param text The text to parse with zero or more delimited references to
   * replace.
   */
  private String substitute( String text ) {
    final Matcher matcher = patternMatch( text );
    final Map<String, String> map = getReferences();

    while( matcher.find() ) {
      final String key = matcher.group( GROUP_DELIMITED );
      final String value = map.get( key );

      if( value == null ) {
        missing( text );
      } else {
        text = text.replace( key, value );
      }
    }

    return text;
  }

  /**
   * Writes the modified YAML document to standard output.
   */
  private void writeDocument() throws IOException {
    getObjectMapper().writeValue( System.out, getDocumentRoot() );
  }

  /**
   * Called when a delimited reference is dereferenced to an empty string.
   * This should produce a warning for the user.
   *
   * @param delimited Delimited reference with no derived value.
   */
  private void missing( final String delimited ) {
    throw new InvalidParameterException(
      MessageFormat.format( "Missing value for '{0}'.", delimited ) );
  }

  /**
   * Returns a REGEX_PATTERN matcher for the given text.
   *
   * @param text The text that contains zero or more instances of a
   * REGEX_PATTERN that can be found using the regular expression.
   */
  private Matcher patternMatch( String text ) {
    return getPattern().matcher( text );
  }

  /**
   * Finds the YAML value for a reference.
   *
   * @param reference References a value in the YAML document.
   *
   * @return The dereferenced value.
   */
  private String lookup( final String reference ) {
    return getDocumentRoot().at( asPath( reference ) ).asText();
  }

  /**
   * Converts a reference (not delimited) to a path that can be used to find a
   * value that should exist inside the YAML document.
   *
   * @param reference The reference to convert to a YAML document path.
   *
   * @return The reference with a leading slash and its separator characters
   * converted to slashes.
   */
  private String asPath( final String reference ) {
    return SEPARATOR_YAML + reference.replace( getDelimitedSeparator(), SEPARATOR_YAML );
  }

  /**
   * Sets the parent node for the entire YAML document tree.
   *
   * @param documentRoot The parent node.
   */
  private void setDocumentRoot( ObjectNode documentRoot ) {
    this.documentRoot = documentRoot;
  }

  /**
   * Returns the parent node for the entire YAML document tree.
   *
   * @return The parent node.
   */
  private ObjectNode getDocumentRoot() {
    return this.documentRoot;
  }

  /**
   * Returns the compiled regular expression REGEX_PATTERN used to match
   * delimited references.
   *
   * @return A compiled regex for use with the Matcher.
   */
  private Pattern getPattern() {
    return REGEX_PATTERN;
  }

  /**
   * Returns the list of references mapped to dereferenced values.
   *
   * @return
   */
  private Map<String, String> getReferences() {
    if( this.references == null ) {
      this.references = createReferences();
    }

    return this.references;
  }

  /**
   * Subclasses can override this method to insert their own map.
   *
   * @return An empty HashMap, never null.
   */
  protected Map<String, String> createReferences() {
    return new HashMap<>();
  }

  private class ResolverYAMLFactory extends YAMLFactory {

    @Override
    protected YAMLGenerator _createGenerator(
      Writer out, IOContext ctxt ) throws IOException {
      return new ResolverYAMLGenerator(
        ctxt, _generatorFeatures, _yamlGeneratorFeatures, _objectCodec,
        out, _version );
    }
  }

  private class ResolverYAMLGenerator extends YAMLGenerator {

    public ResolverYAMLGenerator(
      IOContext ctxt,
      int jsonFeatures,
      int yamlFeatures,
      ObjectCodec codec,
      Writer out,
      DumperOptions.Version version ) throws IOException {
      super( ctxt, jsonFeatures, yamlFeatures, codec, out, version );
    }

    @Override
    public void writeString( String text )
      throws IOException, JsonGenerationException {
      super.writeString( substitute( text ) );
    }
  }

  private YAMLFactory getYAMLFactory() {
    return new ResolverYAMLFactory();
  }

  private ObjectMapper getObjectMapper() {
    return new ObjectMapper( getYAMLFactory() );
  }

  /**
   * Returns the character used to separate YAML paths within delimited
   * references. This will return only the first character of the command line
   * parameter, if the default is overridden.
   *
   * @return A period by default.
   */
  private char getDelimitedSeparator() {
    return SEPARATOR_VARIABLE.charAt( 0 );
  }
}
