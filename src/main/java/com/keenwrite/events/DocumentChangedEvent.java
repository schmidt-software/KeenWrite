/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

/**
 * Collates information about an HTML document that has changed.
 */
public class DocumentChangedEvent implements AppEvent {
  private final String mText;

  /**
   * Hash document (as plain text) so subscribers are notified upon changes.
   */
  private static int sHash;

  /**
   * Creates an event with the new plain text document, having all variables
   * substituted and all markup removed.
   *
   * @param text The document text that has changed since the last time this
   *             type of event was fired.
   */
  private DocumentChangedEvent( final String text ) {
    mText = text;
  }

  /**
   * When the given document may have changed. This will only fire a change
   * event if the given document has changed from the last time this
   * event was fired. The document is first converted to plain text before
   * the comparison is made.
   *
   * @param html The document that may have changed.
   */
  public static void fireDocumentChangedEvent( final String html ) {
    // Hashing the document text ignores caret position changes.
    final var hash = html.hashCode();

    if( hash != sHash ) {
      sHash = hash;
      new DocumentChangedEvent( html ).fire();
    }
  }

  /**
   * Returns the text that has changed.
   *
   * @return The new document text.
   */
  public String getDocument() {
    return mText;
  }

  /**
   * Returns the document.
   *
   * @return The value from {@link #getDocument()}.
   */
  @Override
  public String toString() {
    return getDocument();
  }
}
