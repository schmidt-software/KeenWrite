package com.scrivenvar.definition;

/**
 * Responsible for parsing structured document formats.
 *
 * @param <T> The type of "node" for the document's object model.
 */
public interface DocumentParser<T> {

  /**
   * Parses a document into a nested object hierarchy. The object returned
   * from this call must be the root node in the document tree.
   *
   * @return The document's root node, which may be empty but never null.
   */
  T parse();
}
