package com.scrivenvar;

/**
 * Responsible for openning a local system file
 */
public interface FileNavigationListener {

  /**
   * Navigates to a File with a given path.
   * If the targeted file is already open, make the related tab become the active tab;
   * otherwise, the file is opened and becomes the active tab.
   *
   * @param path the file path
   */
  void navigateToFile(String path);
}
