/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.PreferencesFx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

import static com.keenwrite.Constants.*;

/**
 * Responsible for user preferences that can be changed from the GUI. The
 * settings are displayed and persisted using {@link PreferencesFx}.
 */
public final class UserPreferences {
  /**
   * Implementation of the initialization-on-demand holder design pattern
   * for a lazily-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification.
   */
  private static class Container {
    private static final UserPreferences INSTANCE = new UserPreferences();
  }

  /**
   * Returns the singleton instance for user preferences.
   *
   * @return A non-null instance, loaded, configured, and ready to persist.
   */
  public static UserPreferences getInstance() {
    return Container.INSTANCE;
  }

  private final ObjectProperty<File> mPropDefinitionPath;
  private final StringProperty mPropRDelimBegan;
  private final StringProperty mPropRDelimEnded;
  private final StringProperty mPropDefDelimBegan;
  private final StringProperty mPropDefDelimEnded;

  private UserPreferences() {
    mPropDefinitionPath = new SimpleObjectProperty<>( DEFINITION_DEFAULT );
    mPropDefDelimBegan = new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT );
    mPropDefDelimEnded = new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT );

    mPropRDelimBegan = new SimpleStringProperty( R_DELIM_BEGAN_DEFAULT );
    mPropRDelimEnded = new SimpleStringProperty( R_DELIM_ENDED_DEFAULT );
  }

  public ObjectProperty<File> definitionPathProperty() {
    return mPropDefinitionPath;
  }

  public StringProperty defDelimiterBeganProperty() {
    return mPropDefDelimBegan;
  }

  public String getDefDelimiterBegan() {
    return defDelimiterBeganProperty().get();
  }

  public StringProperty defDelimiterEndedProperty() {
    return mPropDefDelimEnded;
  }

  public String getDefDelimiterEnded() {
    return defDelimiterEndedProperty().get();
  }

  public StringProperty rDelimiterBeganProperty() {
    return mPropRDelimBegan;
  }

  public StringProperty rDelimiterEndedProperty() {
    return mPropRDelimEnded;
  }
}
