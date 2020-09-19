#!/bin/bash

INKSCAPE="/usr/bin/inkscape"
PNG_COMPRESS="optipng"
PNG_COMPRESS_OPTS="-o9 *png"
ICO_TOOL="icotool"
ICO_TOOL_OPTS="-c -o ../../../../../icons/logo.ico logo64.png"

declare -a SIZES=("16" "32" "64" "128" "256" "512")

for i in "${SIZES[@]}"; do
  # -y: export background opacity 0
  $INKSCAPE -y 0 -w "${i}" --export-overwrite --export-type=png -o "logo${i}.png" "logo.svg" 
done

# Compess the PNG images.
which $PNG_COMPRESS && $PNG_COMPRESS $PNG_COMPRESS_OPTS

# Generate an ICO file.
which $ICO_TOOL && $ICO_TOOL $ICO_TOOL_OPTS

