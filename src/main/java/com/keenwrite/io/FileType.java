/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

/**
 * Represents different file type classifications. These are high-level mappings
 * that correspond to the list of glob patterns found within {@code
 * settings.properties}.
 */
public enum FileType {

  ALL( "all" ),
  RMARKDOWN( "rmarkdown" ),
  RXML( "rxml" ),
  SOURCE( "source" ),
  DEFINITION( "definition" ),
  XML( "xml" ),
  CSV( "csv" ),
  JSON( "json" ),
  TOML( "toml" ),
  YAML( "yaml" ),
  PROPERTIES( "properties" ),
  UNKNOWN( "unknown" );

  private final String mType;

  /**
   * Default constructor for enumerated file type.
   *
   * @param type Human-readable name for the file type.
   */
  FileType( final String type ) {
    mType = type;
  }

  /**
   * Returns the file type that corresponds to the given string.
   *
   * @param type The string to compare against this enumeration of file types.
   * @return The corresponding File Type for the given string.
   * @throws IllegalArgumentException Type not found.
   */
  public static FileType from( final String type ) {
    for( final FileType fileType : FileType.values() ) {
      if( fileType.isType( type ) ) {
        return fileType;
      }
    }

    throw new IllegalArgumentException( type );
  }

  /**
   * Answers whether this file type matches the given string, case insensitive
   * comparison.
   *
   * @param type Presumably a file name extension to check against.
   * @return true The given extension corresponds to this enumerated type.
   */
  public boolean isType( final String type ) {
    return getType().equalsIgnoreCase( type );
  }

  /**
   * Answers whether this file type belongs to the set of file types that have
   * embedded R statements.
   *
   * @return {@code true} when the file type is either R Markdown or R XML.
   */
  public boolean isR() {
    return this == RMARKDOWN || this == RXML;
  }

  /**
   * Returns the human-readable name for the file type.
   *
   * @return A non-null instance.
   */
  private String getType() {
    return mType;
  }

  /**
   * Returns the lowercase version of the file name extension.
   *
   * @return The file name, in lower case.
   */
  @Override
  public String toString() {
    return getType();
  }
}
