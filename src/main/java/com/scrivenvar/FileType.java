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
package com.scrivenvar;

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
