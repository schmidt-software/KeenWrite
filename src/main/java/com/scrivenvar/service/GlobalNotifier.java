package com.scrivenvar.service;

import com.scrivenvar.Services;
import com.scrivenvar.service.events.Notifier;

import java.util.Observer;

/**
 * Responsible for passing notifications about exceptions (or other error
 * messages) through the application. Once the Event Bus is implemented, this
 * class can go away.
 */
public class GlobalNotifier {
  private static final Notifier sNotifier = Services.load( Notifier.class );

  public static void clearAlert() {
    getNotifier().clear();
  }

  public static void alert( final String msg ) {
    getNotifier().alert( msg );
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param ex The exception with a message that the user should know about.
   */
  public static void alert( final Exception ex ) {
    getNotifier().alert( ex );
  }

  /**
   * Adds an observer to the list of objects that receive notifications about
   * error messages to be presented to the user.
   *
   * @param observer The observer instance to notify.
   * @deprecated Use event bus instead.
   */
  public void addObserver( final Observer observer ) {
    getNotifier().addObserver( observer );
  }

  public static Notifier getNotifier() {
    return sNotifier;
  }
}
