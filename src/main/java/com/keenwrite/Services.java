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
package com.keenwrite;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Responsible for loading services. The services are treated as singleton
 * instances.
 */
public class Services {

  @SuppressWarnings("rawtypes")
  private static final Map<Class, Object> SINGLETONS = new HashMap<>();

  /**
   * Loads a service based on its interface definition. This will return an
   * existing instance if the class has already been instantiated.
   *
   * @param <T> The service to load.
   * @param api The interface definition for the service.
   * @return A class that implements the interface.
   */
  @SuppressWarnings("unchecked")
  public static <T> T load( final Class<T> api ) {
    final T o = (T) get( api );

    return o == null ? newInstance( api ) : o;
  }

  private static <T> T newInstance( final Class<T> api ) {
    final ServiceLoader<T> services = ServiceLoader.load( api );

    for( final T service : services ) {
      if( service != null ) {
        // Re-use the same instance the next time the class is loaded.
        put( api, service );
        return service;
      }
    }

    throw new RuntimeException( "No implementation for: " + api );
  }

  @SuppressWarnings("rawtypes")
  private static void put( final Class key, Object value ) {
    SINGLETONS.put( key, value );
  }

  @SuppressWarnings("rawtypes")
  private static Object get( final Class api ) {
    return SINGLETONS.get( api );
  }
}
