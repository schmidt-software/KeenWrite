/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
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
