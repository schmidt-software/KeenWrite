/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keenwrite.util;

import java.util.Properties;

import static com.keenwrite.util.Strings.isEmpty;

/**
 * Helpers for {@code java.lang.System}.
 */
public class SystemUtils {

  // System property constants
  // -----------------------------------------------------------------------
  // These MUST be declared first. Other constants depend on this.

  /**
   * The System property name {@value}.
   */
  public static final String PROPERTY_OS_NAME = "os.name";

  /**
   * Gets the current value from the system properties map.
   * <p>
   * Returns {@code null} if the property cannot be read due to a
   * {@link SecurityException}.
   * </p>
   *
   * @return the current value from the system properties map.
   */
  @SuppressWarnings( "ConstantValue" )
  private static String getOsName() {
    assert PROPERTY_OS_NAME != null;
    assert !PROPERTY_OS_NAME.isBlank();

    try {
      final String value = System.getProperty( PROPERTY_OS_NAME );

      return isEmpty( value ) ? "" : value;
    } catch( final SecurityException ignore ) {}

    return "";
  }

  /**
   * The Operating System name, derived from Java's system properties.
   *
   * <p>
   * Defaults to empty if the runtime does not have security access to
   * read this property or the property does not exist.
   * </p>
   * <p>
   * This value is initialized when the class is loaded. If
   * {@link System#setProperty(String, String)} or
   * {@link System#setProperties(Properties)} is called after this
   * class is loaded, the value will be out of sync with that System property.
   * </p>
   */
  public static final String OS_NAME = getOsName();

  /**
   * Is {@code true} if this is AIX.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_AIX = osNameMatches( "AIX" );

  /**
   * Is {@code true} if this is HP-UX.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_HP_UX = osNameMatches( "HP-UX" );

  /**
   * Is {@code true} if this is Irix.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_IRIX = osNameMatches( "Irix" );

  /**
   * Is {@code true} if this is Linux.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_LINUX =
    osNameMatches( "Linux" ) ||
    osNameMatches( "LINUX" );

  /**
   * Is {@code true} if this is Mac.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_MAC = osNameMatches( "Mac" );

  /**
   * Is {@code true} if this is Mac.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_MAC_OSX = osNameMatches( "Mac OS X" );

  /**
   * Is {@code true} if this is FreeBSD.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_FREE_BSD = osNameMatches( "FreeBSD" );

  /**
   * Is {@code true} if this is OpenBSD.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_OPEN_BSD = osNameMatches( "OpenBSD" );

  /**
   * Is {@code true} if this is NetBSD.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_NET_BSD = osNameMatches( "NetBSD" );

  /**
   * Is {@code true} if this is Solaris.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_SOLARIS = osNameMatches( "Solaris" );

  /**
   * Is {@code true} if this is SunOS.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_SUN_OS = osNameMatches( "SunOS" );

  /**
   * Is {@code true} if this is a UNIX like system, as in any of AIX, HP-UX,
   * Irix, Linux, MacOSX, Solaris or SUN OS.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_UNIX =
    IS_OS_AIX ||
    IS_OS_HP_UX ||
    IS_OS_IRIX ||
    IS_OS_LINUX ||
    IS_OS_MAC_OSX ||
    IS_OS_SOLARIS ||
    IS_OS_SUN_OS ||
    IS_OS_FREE_BSD ||
    IS_OS_OPEN_BSD ||
    IS_OS_NET_BSD;

  /**
   * The prefix String for all Windows OS.
   */
  private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

  /**
   * Is {@code true} if this is Windows.
   *
   * <p>
   * The field will return {@code false} if {@code OS_NAME} is {@code null}.
   * </p>
   */
  public static final boolean IS_OS_WINDOWS =
    osNameMatches( OS_NAME_WINDOWS_PREFIX );

  /**
   * Decides if the operating system matches.
   * <p>
   * This method is package private instead of private to support unit test
   * invocation.
   * </p>
   *
   * @param prefix the prefix for the expected OS name
   * @return true if matches, or false if not or can't determine
   */
  private static boolean osNameMatches( final String prefix ) {
    return OS_NAME.startsWith( prefix );
  }
}
