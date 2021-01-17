/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import static com.keenwrite.preferences.Key.key;

/**
 * Responsible for defining constants used throughout the application that
 * represent persisted preferences.
 */
public final class WorkspaceKeys {
  //@formatter:off
  private static final Key KEY_ROOT = key( "workspace" );

  public static final Key KEY_META = key( KEY_ROOT, "meta" );
  public static final Key KEY_META_NAME = key( KEY_META, "name" );
  public static final Key KEY_META_VERSION = key( KEY_META, "version" );

  public static final Key KEY_R = key( KEY_ROOT, "r" );
  public static final Key KEY_R_SCRIPT = key( KEY_R, "script" );
  public static final Key KEY_R_DIR = key( KEY_R, "dir" );
  public static final Key KEY_R_DELIM = key( KEY_R, "delimiter" );
  public static final Key KEY_R_DELIM_BEGAN = key( KEY_R_DELIM, "began" );
  public static final Key KEY_R_DELIM_ENDED = key( KEY_R_DELIM, "ended" );

  public static final Key KEY_IMAGES = key( KEY_ROOT, "images" );
  public static final Key KEY_IMAGES_DIR = key( KEY_IMAGES, "dir" );
  public static final Key KEY_IMAGES_ORDER = key( KEY_IMAGES, "order" );

  public static final Key KEY_DEF = key( KEY_ROOT, "definition" );
  public static final Key KEY_DEF_PATH = key( KEY_DEF, "path" );
  public static final Key KEY_DEF_DELIM = key( KEY_DEF, "delimiter" );
  public static final Key KEY_DEF_DELIM_BEGAN = key( KEY_DEF_DELIM, "began" );
  public static final Key KEY_DEF_DELIM_ENDED = key( KEY_DEF_DELIM, "ended" );

  public static final Key KEY_UI = key( KEY_ROOT, "ui" );

  public static final Key KEY_UI_RECENT = key( KEY_UI, "recent" );
  public static final Key KEY_UI_RECENT_DIR = key( KEY_UI_RECENT, "dir" );
  public static final Key KEY_UI_RECENT_DOCUMENT = key( KEY_UI_RECENT, "document" );
  public static final Key KEY_UI_RECENT_DEFINITION = key( KEY_UI_RECENT, "definition" );

  public static final Key KEY_UI_FILES = key( KEY_UI, "files" );
  public static final Key KEY_UI_FILES_PATH = key( KEY_UI_FILES, "path" );

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

  public static final Key KEY_UI_WINDOW = key( KEY_UI, "window" );
  public static final Key KEY_UI_WINDOW_X = key( KEY_UI_WINDOW, "x" );
  public static final Key KEY_UI_WINDOW_Y = key( KEY_UI_WINDOW, "y" );
  public static final Key KEY_UI_WINDOW_W = key( KEY_UI_WINDOW, "width" );
  public static final Key KEY_UI_WINDOW_H = key( KEY_UI_WINDOW, "height" );
  public static final Key KEY_UI_WINDOW_MAX = key( KEY_UI_WINDOW, "maximized" );
  public static final Key KEY_UI_WINDOW_FULL = key( KEY_UI_WINDOW, "full" );

  public static final Key KEY_UI_THEME = key( KEY_UI, "theme" );
  public static final Key KEY_UI_THEME_SELECTION = key( KEY_UI_THEME, "selection" );

  public static final Key KEY_UI_THEME_CUSTOM = key( KEY_UI_THEME, "custom" );

//  public static final Key KEY_UI_THEME_CUSTOM = key( KEY_UI_THEME, "custom" );
//  public static final Key KEY_UI_THEME_CUSTOM_FONT = key( KEY_UI_THEME_CUSTOM, "font" );
//  public static final Key KEY_UI_THEME_CUSTOM_FONT_SIZE = key( KEY_UI_THEME_CUSTOM_FONT, "size" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS = key( KEY_UI_THEME_CUSTOM, "colours" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_BASE = key( KEY_UI_THEME_CUSTOM_COLOURS, "base" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_BG = key( KEY_UI_THEME_CUSTOM_COLOURS, "background" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_CONTROLS = key( KEY_UI_THEME_CUSTOM_COLOURS, "controls" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_ROW1 = key( KEY_UI_THEME_CUSTOM_COLOURS, "row" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_ROW2 = key( KEY_UI_THEME_CUSTOM_COLOURS, "row" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_FG = key( KEY_UI_THEME_CUSTOM_COLOURS, "foreground" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_FG_LIGHT = key( KEY_UI_THEME_CUSTOM_COLOURS_FG, "light" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_FG_MEDIUM = key( KEY_UI_THEME_CUSTOM_COLOURS_FG, "medium" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_FG_DARK = key( KEY_UI_THEME_CUSTOM_COLOURS_FG, "dark" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_ACCENT = key( KEY_UI_THEME_CUSTOM_COLOURS, "accent" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_UNFOCUSED = key( KEY_UI_THEME_CUSTOM_COLOURS, "unfocused" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR = key( KEY_UI_THEME_CUSTOM_COLOURS, "scrollbar" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON = key( KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR, "button" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON_RELEASED = key( KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON, "released" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON_PRESSED = key( KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON, "pressed" );
//  public static final Key KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON_HOVER = key( KEY_UI_THEME_CUSTOM_COLOURS_SCROLLBAR_BUTTON, "hover" );

  public static final Key KEY_LANGUAGE = key( KEY_ROOT, "language" );
  public static final Key KEY_LANGUAGE_LOCALE = key( KEY_LANGUAGE, "locale" );
  //@formatter:on

  /**
   *
   */
  private WorkspaceKeys() { }
}
