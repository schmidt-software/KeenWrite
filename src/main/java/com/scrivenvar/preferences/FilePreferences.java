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
import java.util.*;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import static com.scrivenvar.StatusBarNotifier.alert;

/**
 * Preferences implementation that stores to a user-defined file. Local file
 * storage is preferred over a certain operating system's monolithic trash heap
 * called a registry. When the OS is locked down, the default Preferences
 * implementation will try to write to the registry and fail due to permissions
 * problems. This class sidesteps the issue entirely by writing to the user's
 * home directory, where permissions should be a bit more lax.
 */
public class FilePreferences extends AbstractPreferences {

  private final Map<String, String> mRoot = new TreeMap<>();
  private final Map<String, FilePreferences> mChildren = new TreeMap<>();
  private boolean mRemoved;

  private final Object mMutex = new Object();

  public FilePreferences(
      final AbstractPreferences parent, final String name ) {
    super( parent, name );

    try {
      sync();
    } catch( final BackingStoreException ex ) {
      alert( ex );
    }
  }

  @Override
  protected void putSpi( final String key, final String value ) {
    synchronized( mMutex ) {
      mRoot.put( key, value );
    }

    try {
      flush();
    } catch( final BackingStoreException ex ) {
      alert( ex );
    }
  }

  @Override
  protected String getSpi( final String key ) {
    synchronized( mMutex ) {
      return mRoot.get( key );
    }
  }

  @Override
  protected void removeSpi( final String key ) {
    synchronized( mMutex ) {
      mRoot.remove( key );
    }

    try {
      flush();
    } catch( final BackingStoreException ex ) {
      alert( ex );
    }
  }

  @Override
  protected void removeNodeSpi() throws BackingStoreException {
    mRemoved = true;
    flush();
  }

  @Override
  protected String[] keysSpi() {
    synchronized( mMutex ) {
      return mRoot.keySet().toArray( new String[ 0 ] );
    }
  }

  @Override
  protected String[] childrenNamesSpi() {
    return mChildren.keySet().toArray( new String[ 0 ] );
  }

  @Override
  protected FilePreferences childSpi( final String name ) {
    FilePreferences child = mChildren.get( name );

    if( child == null || child.isRemoved() ) {
      child = new FilePreferences( this, name );
      mChildren.put( name, child );
    }

    return child;
  }

  @Override
  protected void syncSpi() {
    if( isRemoved() ) {
      return;
    }

    final File file = FilePreferencesFactory.getPreferencesFile();

    if( !file.exists() ) {
      return;
    }

    synchronized( mMutex ) {
      final Properties p = new Properties();

      try( final var inputStream = new FileInputStream( file ) ) {
        p.load( inputStream );

        final String path = getPath();
        final Enumeration<?> propertyNames = p.propertyNames();

        while( propertyNames.hasMoreElements() ) {
          final String propKey = (String) propertyNames.nextElement();

          if( propKey.startsWith( path ) ) {
            final String subKey = propKey.substring( path.length() );

            // Only load immediate descendants
            if( subKey.indexOf( '.' ) == -1 ) {
              mRoot.put( subKey, p.getProperty( propKey ) );
            }
          }
        }
      } catch( final Exception ex ) {
        alert( ex );
      }
    }
  }

  private String getPath() {
    final FilePreferences parent = (FilePreferences) parent();

    return parent == null ? "" : parent.getPath() + name() + '.';
  }

  @Override
  protected void flushSpi() {
    final File file = FilePreferencesFactory.getPreferencesFile();

    synchronized( mMutex ) {
      final Properties p = new Properties();

      try {
        final String path = getPath();

        if( file.exists() ) {
          try( final var fis = new FileInputStream( file ) ) {
            p.load( fis );
          }

          final List<String> toRemove = new ArrayList<>();

          // Make a list of all direct children of this node to be removed
          final Enumeration<?> propertyNames = p.propertyNames();

          while( propertyNames.hasMoreElements() ) {
            final String propKey = (String) propertyNames.nextElement();
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
        if( !mRemoved ) {
          for( final String s : mRoot.keySet() ) {
            p.setProperty( path + s, mRoot.get( s ) );
          }
        }

        try( final var fos = new FileOutputStream( file ) ) {
          p.store( fos, "FilePreferences" );
        }
      } catch( final Exception ex ) {
        alert( ex );
      }
    }
  }
}
