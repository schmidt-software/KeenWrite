#!/bin/bash

INKSCAPE=/usr/bin/inkscape

declare -a SIZES=("16" "32" "64" "128" "256" "512")

for i in "${SIZES[@]}"; do
  # -y: export background opacity 0
  $INKSCAPE -y 0 -z -f "logo.svg" -w "${i}" -e "logo${i}.png"
done

