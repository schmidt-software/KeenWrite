/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.typesetting.container.api.Container;

/**
 * Responsible for initializing the container on all platforms except Linux.
 */
public final class ContainerInitializationPane extends ContainerOutputPane {

  private static final String PREFIX =
    "Wizard.typesetter.all.3.install.container";

  public ContainerInitializationPane() {
    super(
      PREFIX + ".correct",
      PREFIX + ".missing",
      Container::start,
      35
    );
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
