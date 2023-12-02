#!/usr/bin/env bash

java \
  -Dprism.order=sw \
  --enable-preview \
  --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED \
  --add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
  --add-opens=javafx.graphics/javafx.scene.text=ALL-UNNAMED \
  --add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED \
  --add-opens=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
  --add-opens=javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
  --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED \
  -jar keenwrite.jar $@

