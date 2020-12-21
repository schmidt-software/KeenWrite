/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.util.StorageHandler;
import javafx.collections.ObservableList;

import java.util.prefs.Preferences;

public class XmlStorageHandler implements StorageHandler {
  @Override
  public void saveSelectedCategory( final String breadcrumb ) { }

  @Override
  public String loadSelectedCategory() {
    return "";
  }

  @Override
  public void saveDividerPosition( final double dividerPosition ) {
  }

  @Override
  public double loadDividerPosition() {
    return 0;
  }

  @Override
  public void saveWindowWidth( final double windowWidth ) { }

  @Override
  public double loadWindowWidth() {
    return 0;
  }

  @Override
  public void saveWindowHeight( final double windowHeight ) { }

  @Override
  public double loadWindowHeight() {
    return 0;
  }

  @Override
  public void saveWindowPosX( final double windowPosX ) { }

  @Override
  public double loadWindowPosX() {
    return 0;
  }

  @Override
  public void saveWindowPosY( final double windowPosY ) { }

  @Override
  public double loadWindowPosY() {
    return 0;
  }

  @Override
  public void saveObject( final String breadcrumb, final Object object ) { }

  @Override
  public Object loadObject(
    final String breadcrumb, final Object defaultObject ) {
    return defaultObject;
  }

  @Override
  public <T> T loadObject(
    final String breadcrumb, final Class<T> type, final T defaultObject ) {
    return defaultObject;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public ObservableList loadObservableList(
    final String breadcrumb, final ObservableList defaultObservableList ) {
    return defaultObservableList;
  }

  @Override
  public <T> ObservableList<T> loadObservableList(
    final String breadcrumb,
    final Class<T> type,
    final ObservableList<T> defaultObservableList ) {
    return defaultObservableList;
  }

  @Override
  public boolean clearPreferences() {
    return false;
  }

  @Override
  public Preferences getPreferences() {
    return null;
  }
}
