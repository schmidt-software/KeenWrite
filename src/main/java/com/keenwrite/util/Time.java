/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.time.Duration;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.*;

/**
 * Responsible for time-related functionality.
 */
public final class Time {
  /**
   * Converts an elapsed time to a human-readable format (hours, minutes,
   * seconds, and milliseconds).
   *
   * @param duration An elapsed time.
   * @return Human-readable elapsed time.
   */
  public static String toElapsedTime( final Duration duration ) {
    final var elapsed = duration.toMillis();
    final var hours = MILLISECONDS.toHours( elapsed );
    final var eHours = elapsed - HOURS.toMillis( hours );
    final var minutes = MILLISECONDS.toMinutes( eHours );
    final var eMinutes = eHours - MINUTES.toMillis( minutes );
    final var seconds = MILLISECONDS.toSeconds( eMinutes );
    final var eSeconds = eMinutes - SECONDS.toMillis( seconds );
    final var milliseconds = MILLISECONDS.toMillis( eSeconds );

    return format( "%02d:%02d:%02d.%03d",
                   hours, minutes, seconds, milliseconds );
  }
}
