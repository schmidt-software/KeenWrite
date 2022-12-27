/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import com.keenwrite.typesetting.container.api.Container;

import static com.keenwrite.typesetting.installer.TypesetterInstaller.createContainerOutputPanel;

public  final class CommonContainerInitializationPane {
  public static InstallPane create() {
    return createContainerOutputPanel(
      "Wizard.typesetter.all.3.install.container.header",
      "Wizard.typesetter.all.3.install.container.correct",
      "Wizard.typesetter.all.3.install.container.missing",
      Container::start,
      35
    );
  }
}
