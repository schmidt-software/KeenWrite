#!/bin/bash

INKSCAPE="/usr/bin/inkscape"
PNG_COMPRESS="optipng"
PNG_COMPRESS_OPTS="-o9 *png"

declare -a SIZES=("16" "32" "64" "128" "256" "512")

for i in "${SIZES[@]}"; do
  # -y: export background opacity 0
  $INKSCAPE -y 0 -w "${i}" --export-overwrite --export-type=png -o "logo${i}.png" "logo.svg" 
done

# Compess the PNG images.
which $PNG_COMPRESS && $PNG_COMPRESS $PNG_COMPRESS_OPTS

