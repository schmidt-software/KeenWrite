/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import com.keenwrite.typesetting.container.api.Container;

import static com.keenwrite.typesetting.installer.TypesetterInstaller.createContainerOutputPanel;

public final class CommonContainerInitializationPane {
  private static final String PREFIX =
    "Wizard.typesetter.all.3.install.container";

  static InstallPane create() {
    return createContainerOutputPanel(
      PREFIX + ".header",
      PREFIX + ".correct",
      PREFIX + ".missing",
      Container::start,
      35
    );
  }
}
