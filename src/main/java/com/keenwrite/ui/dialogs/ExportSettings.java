/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.dialogs;

import com.keenwrite.util.GenericBuilder;
import javafx.beans.property.StringProperty;

/**
 * Provides export settings such as the selected theme and chapter numbers
 * to include.
 */
public class ExportSettings {
  private final Mutator mMutator;

  public static class Mutator {
    private StringProperty mThemeProperty;
    private StringProperty mChaptersProperty;

    public void setTheme( final StringProperty theme ) {
      assert theme != null;
      mThemeProperty = theme;
    }

    public void setChapters( final StringProperty chapters ) {
      assert chapters != null;
      mChaptersProperty = chapters;
    }
  }

  /**
   * Force using the builder pattern.
   */
  private ExportSettings( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
  }

  public static GenericBuilder<Mutator, ExportSettings> builder() {
    return GenericBuilder.of(
      ExportSettings.Mutator::new, ExportSettings::new
    );
  }

  public StringProperty themeProperty() {
    return mMutator.mThemeProperty;
  }

  public StringProperty chaptersProperty() {
    return mMutator.mChaptersProperty;
  }
}

