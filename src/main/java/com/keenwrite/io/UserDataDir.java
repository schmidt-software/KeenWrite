/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.SystemUtils.*;

/**
 * Responsible for determining the directory to write application data, across
 * multiple platforms. See also:
 *
 * <ul>
 * <li>
 *   <a href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">
 *     Linux: XDG Base Directory Specification
 *   </a>
 * </li>
 * <li>
 *   <a href="https://learn.microsoft.com/en-us/windows/deployment/usmt/usmt-recognized-environment-variables">
 *     Windows: Recognized environment variables
 *   </a>
 * </li>
 * <li>
 *   <a href="https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/FileSystemProgrammingGuide/FileSystemOverview/FileSystemOverview.html">
 *     MacOS: File System Programming Guide
 *   </a>
 * </li>
 * </ul>
 * </p>
 */
public final class UserDataDir {

  private static final Path UNDEFINED = Path.of( "/" );

  private static final String PROP_USER_HOME = getProperty( "user.home" );
  private static final String PROP_OS_VERSION = getProperty( "os.version" );
  private static final String ENV_APPDATA = getenv( "AppData" );
  private static final String ENV_XDG_DATA_HOME = getenv( "XDG_DATA_HOME" );

  private UserDataDir() { }

  public static Path getAppPath( final String appName )
    throws FileNotFoundException {
    final var osPath = isWindows()
      ? getWinAppPath()
      : isMacOs()
      ? getMacAppPath()
      : isUnix()
      ? getUnixAppPath()
      : UNDEFINED;

    final var path = osPath.equals( UNDEFINED )
      ? getDefaultAppPath( appName )
      : osPath.resolve( appName );

    return ensureExists( path ) ? path : fail( path );
  }

  private static Path fail( final Path path ) throws FileNotFoundException {
    throw new FileNotFoundException( path.toString() );
  }

  private static Path getWinAppPath() {
    return isValid( ENV_APPDATA )
      ? Path.of( ENV_APPDATA )
      : home( getWinVerAppPath() );
  }

  /**
   * Gets the application path with respect to the Windows version.
   *
   * @return The directory name paths relative to the user's home directory.
   */
  private static String[] getWinVerAppPath() {
    return PROP_OS_VERSION.startsWith( "5." )
      ? new String[]{"Application Data"}
      : new String[]{"AppData", "Roaming"};
  }

  private static Path getMacAppPath() {
    final var path = home( "Library", "Application Support" );

    return ensureExists( path ) ? path : UNDEFINED;
  }

  private static Path getUnixAppPath() {
    // Fallback in case the XDG data directory is undefined.
    var path = home( ".local", "share" );

    if( isValid( ENV_XDG_DATA_HOME ) ) {
      final var xdgPath = Path.of( ENV_XDG_DATA_HOME );

      path = ensureExists( xdgPath ) ? xdgPath : path;
    }

    return path;
  }

  /**
   * Returns a hidden directory relative to the user's home directory.
   *
   * @param appName The application name.
   * @return A suitable directory for storing application files.
   */
  private static Path getDefaultAppPath( final String appName ) {
    return home( '.' + appName );
  }

  private static Path home( final String... paths ) {
    return Path.of( PROP_USER_HOME, paths );
  }

  /**
   * Verifies whether the path exists or was created.
   *
   * @param path The directory to verify.
   * @return {@code true} if the path already exists or was created,
   * {@code false} if the directory doesn't exist and couldn't be created.
   */
  private static boolean ensureExists( final Path path ) {
    final var file = path.toFile();
    return file.exists() || file.mkdirs();
  }

  /**
   * Answers whether the given string contains content.
   *
   * @param s The string to check, may be {@code null}.
   * @return {@code true} if the string is neither {@code null} nor blank.
   */
  private static boolean isValid( final String s ) {
    return !(s == null || s.isBlank());
  }

  private static boolean isWindows() {
    return IS_OS_WINDOWS;
  }

  private static boolean isMacOs() {
    return IS_OS_MAC;
  }

  private static boolean isUnix() {
    return IS_OS_UNIX;
  }
}
