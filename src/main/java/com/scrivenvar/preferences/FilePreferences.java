/*
 * Copyright 2016 David Croft and White Magic Software, Ltd.
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
package com.scrivenvar.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences implementation that stores to a user-defined file. Local file
 * storage is preferred over a certain operating system's monolithic trash heap
 * called a registry. When the OS is locked down, the default Preferences
 * implementation will try to write to the registry and fail due to permissions
 * problems. This class sidesteps the issue entirely by writing to the user's
 * home directory, where permissions should be a bit more lax.
 * 
 * @see http://stackoverflow.com/q/208231/59087
 */
public class FilePreferences extends AbstractPreferences {

  private Map<String, String> root = new TreeMap<>();
  private Map<String, FilePreferences> children = new TreeMap<>();
  private boolean isRemoved;

  public FilePreferences( final AbstractPreferences parent, final String name ) {
    super( parent, name );

    try {
      sync();
    } catch( final BackingStoreException ex ) {
      problem( ex );
    }
  }

  @Override
  protected void putSpi( final String key, final String value ) {
    root.put( key, value );

    try {
      flush();
    } catch( final BackingStoreException ex ) {
      problem( ex );
    }
  }

  @Override
  protected String getSpi( final String key ) {
    return root.get( key );
  }

  @Override
  protected void removeSpi( final String key ) {
    root.remove( key );

    try {
      flush();
    } catch( final BackingStoreException ex ) {
      problem( ex );
    }
  }

  @Override
  protected void removeNodeSpi() throws BackingStoreException {
    isRemoved = true;
    flush();
  }

  @Override
  protected String[] keysSpi() throws BackingStoreException {
    return root.keySet().toArray( new String[ root.keySet().size() ] );
  }

  @Override
  protected String[] childrenNamesSpi() throws BackingStoreException {
    return children.keySet().toArray( new String[ children.keySet().size() ] );
  }

  @Override
  protected FilePreferences childSpi( final String name ) {
    FilePreferences child = children.get( name );

    if( child == null || child.isRemoved() ) {
      child = new FilePreferences( this, name );
      children.put( name, child );
    }

    return child;
  }

  @Override
  protected void syncSpi() throws BackingStoreException {
    if( isRemoved() ) {
      return;
    }

    final File file = FilePreferencesFactory.getPreferencesFile();

    if( !file.exists() ) {
      return;
    }

    synchronized( file ) {
      final Properties p = new Properties();

      try {
        p.load( new FileInputStream( file ) );

        final String path = getPath();
        final Enumeration<?> pnen = p.propertyNames();

        while( pnen.hasMoreElements() ) {
          final String propKey = (String)pnen.nextElement();

          if( propKey.startsWith( path ) ) {
            final String subKey = propKey.substring( path.length() );

            // Only load immediate descendants
            if( subKey.indexOf( '.' ) == -1 ) {
              root.put( subKey, p.getProperty( propKey ) );
            }
          }
        }
      } catch( final IOException e ) {
        throw new BackingStoreException( e );
      }
    }
  }

  private String getPath() {
    final FilePreferences parent = (FilePreferences)parent();

    return parent == null ? "" : parent.getPath() + name() + '.';
  }

  @Override
  protected void flushSpi() throws BackingStoreException {
    final File file = FilePreferencesFactory.getPreferencesFile();

    synchronized( file ) {
      final Properties p = new Properties();

      try {
        final String path = getPath();

        if( file.exists() ) {
          p.load( new FileInputStream( file ) );

          final List<String> toRemove = new ArrayList<>();

          // Make a list of all direct children of this node to be removed
          final Enumeration<?> pnen = p.propertyNames();

          while( pnen.hasMoreElements() ) {
            String propKey = (String)pnen.nextElement();
            if( propKey.startsWith( path ) ) {
              final String subKey = propKey.substring( path.length() );

              // Only do immediate descendants
              if( subKey.indexOf( '.' ) == -1 ) {
                toRemove.add( propKey );
              }
            }
          }

          // Remove them now that the enumeration is done with
          for( final String propKey : toRemove ) {
            p.remove( propKey );
          }
        }

        // If this node hasn't been removed, add back in any values
        if( !isRemoved ) {
          for( final String s : root.keySet() ) {
            p.setProperty( path + s, root.get( s ) );
          }
        }

        p.store( new FileOutputStream( file ), "FilePreferences" );
      } catch( final IOException e ) {
        throw new BackingStoreException( e );
      }
    }
  }

  private void problem( final BackingStoreException ex ) {
    throw new RuntimeException( ex );
  }
}
