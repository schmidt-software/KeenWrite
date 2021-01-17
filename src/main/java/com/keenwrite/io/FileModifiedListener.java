/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * Responsible for informing listeners when a file has been modified.
 */
public interface FileModifiedListener
  extends EventListener, Consumer<FileEvent> {
}
