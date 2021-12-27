package com.keenwrite.preferences;

import com.keenwrite.dom.DocumentParser;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static javax.xml.xpath.XPathConstants.NODE;

/**
 * Responsible for managing XML documents, which includes reading, writing,
 * retrieving, and setting elements. This is an alternative to Apache
 * Commons Configuration, JAXB, and Jackson. All of them are heavyweight and
 * the latter are difficult to use with dynamic data (because they require
 * annotations).
 */
public class XmlStore {
  private static final String SEPARATOR = "/";

  private final Document mDocument;
  private final String mRoot;

  /**
   * Default constructor that creates an empty document with no root-level
   * element. This is meant as a stub for testing.
   */
  public XmlStore() {
    this( DocumentParser.newDocument(), "" );
  }

  /**
   * Loads the given configuration file into a document object model.
   * Clients of this class can set and retrieve elements via the requisite
   * access methods.
   *
   * @param config File containing persistent user preferences.
   * @param root   Name of the root element (empty, but never {@code null}).
   */
  public XmlStore( final File config, final String root ) {
    this( load( config ), root );
  }

  private XmlStore( final Document document, final String root ) {
    assert document != null;
    assert root != null;

    mDocument = document;
    mRoot = root;
  }

  private static Document load( final File config ) {
    try {
      return DocumentParser.parse( config );
    } catch( final Exception ignored ) {
      return DocumentParser.newDocument();
    }
  }

  /**
   * Returns the document value associated with the given key name.
   *
   * @param key {@link Key} name to retrieve.
   * @return The value associated with the key, or the empty string if the
   * key name could not be compiled.
   */
  public String getValue( final Key key ) {
    assert key != null;

    try {
      final var xpath = toXPath( key );
      final var expr = DocumentParser.compile( xpath );
      return expr.evaluate( mDocument );
    } catch( final XPathExpressionException ignored ) {
      // This exception is a programming error; return a default value.
      return "";
    }
  }

  /**
   * Returns a set of document values associated with the given key name. This
   * is suitable for basic sets, such as:
   * <pre>
   *   {@code
   *   <recent>
   *     <file>/tmp/filename.txt</file>
   *     <file>/home/username/document.md</file>
   *     <file>/usr/local/share/app/conf/help.Rmd</file>
   *   </recent>}
   * </pre>
   * <p>
   * The {@code file} element name can be ignored.
   *
   * @param key {@link Key} name to retrieve.
   * @return The values associated with the key, or an empty set if none found.
   */
  public Set<String> getSet( final Key key ) {
    assert key != null;

    final var set = new LinkedHashSet<String>();

    visit( key, node -> set.add( node.getTextContent() ) );

    return set;
  }

  /**
   * Returns a map of name/value pairs associated with the given key name.
   * This is suitable for mapped values, such as:
   * <pre>
   *   {@code
   *   <meta>
   *     <title>{{book.title}}</title>
   *     <author>{{book.author}}</author>
   *     <date>{{book.publish.date}}</date>
   *   </meta>}
   * </pre>
   * <p>
   * The element names under the {@code meta} node must be preserved along
   * with their values. Resolving the values based on the variable definitions
   * (in moustache syntax) is not a responsibility of this class.
   *
   * @param key {@link Key} name to retrieve (e.g., {@code meta}).
   * @return A map of element names to element values, or an empty map if
   * none found.
   */
  public Map<String, String> getMap( final Key key ) {
    assert key != null;

    // Create a new key that will match all child nodes under the given key,
    // extracting each element as a name/value pair for the resulting map.
    final var all = Key.key( key, "*" );
    final var map = new LinkedHashMap<String, String>();

    visit( all, node -> map.put( node.getNodeName(), node.getTextContent() ) );

    return map;
  }

  public void save( final File config ) {
    System.out.println( "SAVE TO: " + config );
    System.out.println( DocumentParser.toString( mDocument ) );
  }

  public void setValue( final Key key, final String value ) {
    assert key != null;
    assert value != null;

    try {
      final var node = upsert( key, mDocument );

      node.setTextContent( value );
    } catch( final XPathExpressionException ignored ) {}
  }

  public void setSet( final Key key, final SetProperty<?> set ) {
    assert key != null;
    assert set != null;

    try {
      final var node = upsert( key, mDocument );

      // Add child nodes and values.

      System.out.printf( "%s = %s%n", key, set );
    } catch( final XPathExpressionException ignored ) {}
  }

  /**
   * @param key  The application key representing a user preference.
   * @param list List of {@link Entry} items.
   */
  public void setMap( final Key key, final ListProperty<?> list ) {
    assert key != null;
    assert list != null;

    for( final var item : list ) {
      if( item instanceof Entry entry ) {
        try {
          final var child = Key.key( key, entry.getKey().toString() );
          final var node = upsert( child, mDocument );

          node.setTextContent( entry.getValue().toString() );
        } catch( final XPathExpressionException ignored ) {}
      }
    }
  }

  /**
   * Finds the element in the document represented by the given {@link Key}.
   * If no element is found then the full path to the element is created.
   *
   * @param key The application key representing a user preference.
   * @param doc The document that may contain an xpath for the {@link Key}.
   * @return The existing or new element.
   */
  private Node upsert( final Key key, final Document doc )
    throws XPathExpressionException {
    final var missing = new Stack<Key>();
    Key visitor = key;
    Node parent = null;

    do {
      final var xpath = toXPath( visitor );
      final var expr = DocumentParser.compile( xpath );
      final var element = expr.evaluate( doc, NODE );

      // If an element exists on the first iteration, return it.
      if( element instanceof Node node ) {
        if( missing.isEmpty() ) {
          return node;
        }

        parent = node;
      }
      else {
        // Track the number of elements in the hierarchy that don't exist.
        missing.push( visitor );

        // Attempt to find the parent xpath in the document.
        visitor = visitor.parent();
      }
    }
    while( visitor.hasParent() && parent == null );

    // If the document is empty, start creating nodes at the document root.
    if( parent == null ) {
      parent = doc.getDocumentElement();
    }

    // Create the hierarchy.
    while( !missing.isEmpty() ) {
      visitor = missing.pop();

      final var child = doc.createElement( visitor.name() );
      parent.appendChild( child );
      parent = child;
    }

    return parent;
  }

  /**
   * Abstraction for functionality that requires iterating over multiple
   * nodes under a particular xpath.
   *
   * @param key      {@link #toXPath(Key) Compiled} into an {@link XPath}.
   * @param consumer Accepts each node that matches the {@link XPath}.
   */
  private void visit( final Key key, final Consumer<Node> consumer ) {
    try {
      final var xpath = toXPath( key );
      DocumentParser.visit( mDocument, xpath, consumer );
    } catch( final XPathExpressionException ignored ) {
      // Programming error. Maybe triggered loading a previous config version?
    }
  }

  /**
   * Creates an {@link XPathExpression} value based on the given {@link Key}.
   *
   * @param key The {@link Key} to convert to an xpath string.
   * @return The given {@link Key} compiled into an {@link XPathExpression}.
   * @throws XPathExpressionException Could not compile the {@link Key}.
   */
  private StringBuilder toXPath( final Key key )
    throws XPathExpressionException {
    final var sb = new StringBuilder( 128 );

    key.walk( sb::append, SEPARATOR );
    sb.insert( 0, SEPARATOR );

    if( !mRoot.isBlank() ) {
      sb.insert( 0, SEPARATOR + mRoot );
    }

    return sb;
  }

  /**
   * Pretty-prints the XML document into a string. Meant to be used for
   * debugging. To save the configuration, see {@link #save(File)}.
   *
   * @return The document in a well-formed, indented, string format.
   */
  @Override
  public String toString() {
    return DocumentParser.toString( mDocument );
  }
}
