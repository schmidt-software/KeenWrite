/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import static com.keenwrite.preferences.Key.key;

/**
 * Responsible for defining constants used throughout the application that
 * represent persisted preferences.
 */
public final class AppKeys {
  //@formatter:off
  private static final Key KEY_ROOT = key( "workspace" );

  public static final Key KEY_META = key( KEY_ROOT, "meta" );
  public static final Key KEY_META_NAME = key( KEY_META, "name" );
  public static final Key KEY_META_VERSION = key( KEY_META, "version" );

  public static final Key KEY_DOC = key( KEY_ROOT, "document" );
  public static final Key KEY_DOC_META = key( KEY_DOC, "meta" );

  public static final Key KEY_EDITOR = key( KEY_ROOT, "editor" );
  public static final Key KEY_EDITOR_AUTOSAVE = key( KEY_EDITOR, "autosave" );

  public static final Key KEY_R = key( KEY_ROOT, "r" );
  public static final Key KEY_R_SCRIPT = key( KEY_R, "script" );
  public static final Key KEY_R_DIR = key( KEY_R, "dir" );
  public static final Key KEY_R_DELIM = key( KEY_R, "delimiter" );
  public static final Key KEY_R_DELIM_BEGAN = key( KEY_R_DELIM, "began" );
  public static final Key KEY_R_DELIM_ENDED = key( KEY_R_DELIM, "ended" );


  public static final Key KEY_IMAGE = key( KEY_ROOT, "images" );
  public static final Key KEY_CACHE_DIR = key( KEY_IMAGE, "cache" );
  public static final Key KEY_IMAGE_DIR = key( KEY_IMAGE, "dir" );
  public static final Key KEY_IMAGE_ORDER = key( KEY_IMAGE, "order" );
  public static final Key KEY_IMAGE_RESIZE = key( KEY_IMAGE, "resize" );
  public static final Key KEY_IMAGE_SERVER = key( KEY_IMAGE, "server" );

  public static final Key KEY_DEF = key( KEY_ROOT, "definition" );
  public static final Key KEY_DEF_PATH = key( KEY_DEF, "path" );
  public static final Key KEY_DEF_DELIM = key( KEY_DEF, "delimiter" );
  public static final Key KEY_DEF_DELIM_BEGAN = key( KEY_DEF_DELIM, "began" );
  public static final Key KEY_DEF_DELIM_ENDED = key( KEY_DEF_DELIM, "ended" );

  public static final Key KEY_UI = key( KEY_ROOT, "ui" );

  public static final Key KEY_UI_RECENT = key( KEY_UI, "recent" );
  public static final Key KEY_UI_RECENT_DIR = key( KEY_UI_RECENT, "dir" );
  public static final Key KEY_UI_RECENT_DOCUMENT = key( KEY_UI_RECENT, "document" );
  public static final Key KEY_UI_RECENT_OFFSET = key( KEY_UI_RECENT, "offset" );
  public static final Key KEY_UI_RECENT_DEFINITION = key( KEY_UI_RECENT, "definition" );
  public static final Key KEY_UI_RECENT_EXPORT = key( KEY_UI_RECENT, "export" );
  public static final Key KEY_UI_RECENT_OPEN = key( KEY_UI_RECENT, "files" );
  public static final Key KEY_UI_RECENT_OPEN_PATH = key( KEY_UI_RECENT_OPEN, "path" );

  public static final Key KEY_UI_FONT = key( KEY_UI, "font" );
  public static final Key KEY_UI_FONT_EDITOR = key( KEY_UI_FONT, "editor" );
  public static final Key KEY_UI_FONT_EDITOR_NAME = key( KEY_UI_FONT_EDITOR, "name" );
  public static final Key KEY_UI_FONT_EDITOR_SIZE = key( KEY_UI_FONT_EDITOR, "size" );
  public static final Key KEY_UI_FONT_PREVIEW = key( KEY_UI_FONT, "preview" );
  public static final Key KEY_UI_FONT_PREVIEW_NAME = key( KEY_UI_FONT_PREVIEW, "name" );
  public static final Key KEY_UI_FONT_PREVIEW_SIZE = key( KEY_UI_FONT_PREVIEW, "size" );
  public static final Key KEY_UI_FONT_PREVIEW_MONO = key( KEY_UI_FONT_PREVIEW, "mono" );
  public static final Key KEY_UI_FONT_PREVIEW_MONO_NAME = key( KEY_UI_FONT_PREVIEW_MONO, "name" );
  public static final Key KEY_UI_FONT_PREVIEW_MONO_SIZE = key( KEY_UI_FONT_PREVIEW_MONO, "size" );
  public static final Key KEY_UI_FONT_MATH = key( KEY_UI_FONT, "math" );
  public static final Key KEY_UI_FONT_MATH_SIZE = key( KEY_UI_FONT_MATH, "size" );

  public static final Key KEY_UI_WINDOW = key( KEY_UI, "window" );
  public static final Key KEY_UI_WINDOW_X = key( KEY_UI_WINDOW, "x" );
  public static final Key KEY_UI_WINDOW_Y = key( KEY_UI_WINDOW, "y" );
  public static final Key KEY_UI_WINDOW_W = key( KEY_UI_WINDOW, "width" );
  public static final Key KEY_UI_WINDOW_H = key( KEY_UI_WINDOW, "height" );
  public static final Key KEY_UI_WINDOW_MAX = key( KEY_UI_WINDOW, "maximized" );
  public static final Key KEY_UI_WINDOW_FULL = key( KEY_UI_WINDOW, "full" );

  public static final Key KEY_UI_SKIN = key( KEY_UI, "skin" );
  public static final Key KEY_UI_SKIN_SELECTION = key( KEY_UI_SKIN, "selection" );
  public static final Key KEY_UI_SKIN_CUSTOM = key( KEY_UI_SKIN, "custom" );

  public static final Key KEY_UI_PREVIEW = key( KEY_UI, "preview" );
  public static final Key KEY_UI_PREVIEW_STYLESHEET = key( KEY_UI_PREVIEW, "stylesheet" );

  public static final Key KEY_LANGUAGE = key( KEY_ROOT, "language" );
  public static final Key KEY_LANGUAGE_LOCALE = key( KEY_LANGUAGE, "locale" );

  public static final Key KEY_TYPESET = key( KEY_ROOT, "typeset" );
  public static final Key KEY_TYPESET_CONTEXT = key( KEY_TYPESET, "context" );
  public static final Key KEY_TYPESET_CONTEXT_FONTS = key( KEY_TYPESET_CONTEXT, "fonts" );
  public static final Key KEY_TYPESET_CONTEXT_FONTS_DIR = key( KEY_TYPESET_CONTEXT_FONTS, "dir" );
  public static final Key KEY_TYPESET_CONTEXT_THEMES = key( KEY_TYPESET_CONTEXT, "themes" );
  public static final Key KEY_TYPESET_CONTEXT_THEMES_PATH = key( KEY_TYPESET_CONTEXT_THEMES, "path" );
  public static final Key KEY_TYPESET_CONTEXT_THEME_SELECTION = key( KEY_TYPESET_CONTEXT_THEMES, "selection" );
  public static final Key KEY_TYPESET_CONTEXT_CLEAN = key( KEY_TYPESET_CONTEXT, "clean" );
  public static final Key KEY_TYPESET_CONTEXT_CHAPTERS = key( KEY_TYPESET_CONTEXT, "chapters" );
  public static final Key KEY_TYPESET_TYPOGRAPHY = key( KEY_TYPESET, "typography" );
  public static final Key KEY_TYPESET_TYPOGRAPHY_QUOTES = key( KEY_TYPESET_TYPOGRAPHY, "quotes" );
  public static final Key KEY_TYPESET_MODES = key( KEY_TYPESET, "modes" );
  public static final Key KEY_TYPESET_MODES_ENABLED = key( KEY_TYPESET_MODES, "enabled" );
  //@formatter:on

  /**
   * Only for constants, do not instantiate.
   */
  private AppKeys() { }
}
