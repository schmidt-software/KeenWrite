/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.ui;

import net.sf.saxon.expr.instruct.Copy;

/**
 * Responsible for abstracting how functionality is mapped to the application.
 * This allows users to customize accelerator keys and will provide pluggable
 * functionality so that different text markup languages can change documents
 * using their respective syntax.
 */
@SuppressWarnings("NonAsciiCharacters")
public class ApplicationAction {
  public void file‿new() {}
  public void file‿open() {}
  public void file‿close() {}
  public void file‿close_all() {}
  public void file‿save() {}
  public void file‿save_as() {}
  public void file‿save_all() {}
  public void file‿export‿html_svg() {}
  public void file‿export‿html_tex() {}
  public void file‿export‿markdown() {}
  public void file‿exit() {}
  public void edit‿undo() {}
  public void edit‿redo() {}
  public void edit‿cut() {}
  public void edit‿copy() {}
  public void edit‿paste() {}
  public void edit‿select_all() {}
  public void edit‿find() {}
  public void edit‿find_next() {}
  public void edit‿preferences() {}
  public void format‿bold() {}
  public void format‿italic() {}
  public void format‿superscript() {}
  public void format‿subscript() {}
  public void format‿strikethrough() {}
  public void insert‿blockquote() {}
  public void insert‿code() {}
  public void insert‿fenced_code_block() {}
  public void insert‿link() {}
  public void insert‿image() {}
  public void insert‿heading() {}
  public void insert‿heading_1() {}
  public void insert‿heading_2() {}
  public void insert‿heading_3() {}
  public void insert‿unordered_list() {}
  public void insert‿ordered_list() {}
  public void insert‿horizontal_rule() {}
  public void definition‿create() {}
  public void definition‿insert() {}
  public void view‿refresh() {}
  public void view‿preview() {}
  public void help‿about() {}
}
