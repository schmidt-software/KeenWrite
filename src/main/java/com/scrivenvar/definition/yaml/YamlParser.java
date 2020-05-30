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
package com.scrivenvar.definition.yaml;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.scrivenvar.decorators.VariableDecorator;
import com.scrivenvar.decorators.YamlVariableDecorator;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
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

  /**
   * Separates YAML variable nodes (e.g., the dots in
   * <code>$root.node.var$</code>).
   */
  public static final String SEPARATOR = ".";
  public static final char SEPARATOR_CHAR = SEPARATOR.charAt( 0 );

  private final static int GROUP_DELIMITED = 1;
  private final static int GROUP_REFERENCE = 2;

  private final static VariableDecorator VARIABLE_DECORATOR
      = new YamlVariableDecorator();

  private String mError;

  /**
   * Compiled version of DEFAULT_REGEX.
   */
  private final static Pattern REGEX_PATTERN
      = Pattern.compile( YamlVariableDecorator.REGEX );

  /**
   * Should be JsonPointer.SEPARATOR, but Jackson YAML uses magic values.
   */
  private final static char SEPARATOR_YAML = '/';

  /**
   * Start of the Universe (the YAML document node that contains all others).
   */
  private JsonNode documentRoot;

  /**
   * Map of references to dereferenced field values.
   */
  private Map<String, String> references;

  public YamlParser( final InputStream in ) throws IOException {
    process( in );
  }

  /**
   * Returns the given string with all the delimited references swapped with
   * their recursively resolved values.
   *
   * @param text The text to parse with zero or more delimited references to
   *             replace.
   * @return The substituted value.
   */
  public String substitute( String text ) {
    final Matcher matcher = patternMatch( text );
    final Map<String, String> map = getReferences();

    while( matcher.find() ) {
      final String key = matcher.group( GROUP_DELIMITED );
      final String value = map.get( key );

      if( value == null ) {
        missing( text );
      }
      else {
        text = text.replace( key, value );
      }
    }

    return text;
  }

  /**
   * Returns all the strings with their values resolved in a flat hierarchy.
   * This copies all the keys and resolved values into a new map.
   *
   * @return The new map created with all values having been resolved,
   * recursively.
   */
  public Map<String, String> createResolvedMap() {
    final Map<String, String> map = new HashMap<>( 1024 );

    resolve( getDocumentRoot(), "", map );

    return map;
  }

  /**
   * Iterate over a given root node (at any level of the tree) and adapt each
   * leaf node.
   *
   * @param rootNode A JSON node (YAML node) to adapt.
   * @param map      Container that associates definitions with values.
   */
  private void resolve(
      final JsonNode rootNode,
      final String path,
      final Map<String, String> map ) {

    if( rootNode != null ) {
      rootNode.fields().forEachRemaining(
          ( Entry<String, JsonNode> leaf ) -> resolve( leaf, path, map )
      );
    }
  }

  /**
   * Recursively adapt each rootNode to a corresponding rootItem.
   *
   * @param rootNode The node to adapt.
   */
  private void resolve(
      final Entry<String, JsonNode> rootNode,
      final String path,
      final Map<String, String> map ) {

    final JsonNode leafNode = rootNode.getValue();
    final String key = rootNode.getKey();


    if( leafNode.isValueNode() ) {
      final String value;

      if( leafNode instanceof NullNode ) {
        value = "";
      }
      else {
        value = rootNode.getValue().asText();
      }

      map.put( VARIABLE_DECORATOR.decorate( path + key ), substitute( value ) );
    }

    if( leafNode.isObject() ) {
      resolve( leafNode, path + key + SEPARATOR, map );
    }
  }

  /**
   * Reads the first document from the given stream of YAML data and returns a
   * corresponding object that represents the YAML hierarchy. The calling class
   * is responsible for closing the stream. Calling classes should use
   * <code>JsonNode.fields()</code> to walk through the YAML tree of fields.
   *
   * @param in The input stream containing YAML content.
   * @throws IOException Could not read the stream.
   */
  private void process( final InputStream in ) throws IOException {
    final ObjectNode root = (ObjectNode) getObjectMapper().readTree( in );
    setDocumentRoot( root );
    process( root );
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
    }
    else {
      final JsonNode fieldValue = field.getValue();

      // Only basic data types can be parsed into variable values. For
      // node structures, YAML has a built-in mechanism.
      if( fieldValue.isValueNode() ) {
        try {
          resolve( fieldValue.asText() );
        } catch( StackOverflowError e ) {
          setError( "Unresolvable: " + node.textValue() + " = " + fieldValue );
        }
      }
    }
  }

  /**
   * Inserts the delimited references and field values into the cache. This will
   * overwrite existing references.
   *
   * @param fieldValue YAML field containing zero or more delimited references.
   *                   If it contains a delimited reference, the parameter is
   *                   modified with the
   *                   dereferenced value before it is returned.
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
   * @param delimited    The variable name.
   * @param dereferenced The resolved value.
   */
  private void put( String delimited, String dereferenced ) {
    if( dereferenced.isEmpty() ) {
      missing( delimited );
    }
    else {
      getReferences().put( delimited, dereferenced );
    }
  }

  /**
   * Writes the modified YAML document to standard output.
   */
  @SuppressWarnings("unused")
  private void writeDocument() throws IOException {
    getObjectMapper().writeValue( System.out, getDocumentRoot() );
  }

  /**
   * Called when a delimited reference is dereferenced to an empty string. This
   * should produce a warning for the user.
   *
   * @param delimited Delimited reference with no derived value.
   */
  private void missing( final String delimited ) {
    setError( MessageFormat.format( "Missing value for '{0}'.", delimited ) );
  }

  /**
   * Returns a REGEX_PATTERN matcher for the given text.
   *
   * @param text The text that contains zero or more instances of a
   *             REGEX_PATTERN that can be found using the regular expression.
   */
  private Matcher patternMatch( String text ) {
    return getPattern().matcher( text );
  }

  /**
   * Finds the YAML value for a reference.
   *
   * @param reference References a value in the YAML document.
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
   * @return The reference with a leading slash and its separator characters
   * converted to slashes.
   */
  private String asPath( final String reference ) {
    return SEPARATOR_YAML + reference.replace( getDelimitedSeparator(),
                                               SEPARATOR_YAML );
  }

  /**
   * Sets the parent node for the entire YAML document tree.
   *
   * @param documentRoot The parent node.
   */
  private void setDocumentRoot( final ObjectNode documentRoot ) {
    this.documentRoot = documentRoot;
  }

  /**
   * Returns the parent node for the entire YAML document tree.
   *
   * @return The parent node.
   */
  protected JsonNode getDocumentRoot() {
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
   * @return The list of references mapped to dereferenced values.
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

  private final class ResolverYAMLFactory extends YAMLFactory {

    private static final long serialVersionUID = 1L;

    @Override
    protected YAMLGenerator _createGenerator(
        final Writer out, final IOContext ctxt ) throws IOException {

      return new ResolverYAMLGenerator(
          ctxt, _generatorFeatures, _yamlGeneratorFeatures, _objectCodec,
          out, _version );
    }
  }

  private class ResolverYAMLGenerator extends YAMLGenerator {

    public ResolverYAMLGenerator(
        final IOContext ctxt,
        final int jsonFeatures,
        final int yamlFeatures,
        final ObjectCodec codec,
        final Writer out,
        final DumperOptions.Version version ) throws IOException {

      super( ctxt, jsonFeatures, yamlFeatures, codec, out, version );
    }

    @Override
    public void writeString( final String text ) throws IOException {
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
    return SEPARATOR.charAt( 0 );
  }

  private void setError( final String error ) {
    mError = error;
  }

  /**
   * Returns the last error message, if any, that occurred during parsing.
   *
   * @return The error message or the empty string if no error occurred.
   */
  public String getError() {
    return mError == null ? "" : mError;
  }
}
