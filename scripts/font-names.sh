#!/usr/bin/env bash

# Outputs font names for all font files.

find src/main/resources/fonts -type f \( -name "*otf" -o -name "*ttf" \) -exec \
  fc-scan --format "%{foundry}: %{family}\n" {} \; | uniq | sort

