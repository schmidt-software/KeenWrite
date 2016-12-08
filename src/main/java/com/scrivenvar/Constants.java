/*
 * Copyright 2016 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar;

/**
 * @author White Magic Software, Ltd.
 */
public class Constants {

  /**
   * Prevent instantiation.
   */
  private Constants() {
  }
  
  public static final String BUNDLE_NAME = "com.scrivenvar.messages";
  public static final String SETTINGS_NAME = "/com/scrivenvar/settings.properties";

  public static final String STYLESHEET_PREVIEW = "com/scrivenvar/scene.css";
  public static final String STYLESHEET_EDITOR = "com/scrivenvar/editor/Markdown.css";

  public static final String LOGO_32 = "com/scrivenvar/logo32.png";
  public static final String LOGO_16 = "com/scrivenvar/logo16.png";
  public static final String LOGO_128 = "com/scrivenvar/logo128.png";
  public static final String LOGO_256 = "com/scrivenvar/logo256.png";
  public static final String LOGO_512 = "com/scrivenvar/logo512.png";
  
  /**
   * Separates YAML variable nodes (e.g., the dots in <code>$root.node.var$</code>).
   */
  public static final String SEPARATOR = ".";
  
  public static final String CARET_POSITION = "CARETPOSITION";
  public static final String MD_CARET_POSITION = "${" + CARET_POSITION + "}";
  public static final String XML_CARET_POSITION = "<![CDATA[" + MD_CARET_POSITION + "]]>";
}
