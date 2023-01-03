#!/usr/bin/env bash

# This script modifies the Windows portable executable (PE) file with an icon
# and standard indentifiers.

# Requirements:
#
#   wine

# Read the properties file to get the application name.
while IFS='=' read -r key value
do
  key=$(echo $key | tr '.' '_')
  eval ${key}=\${value}
done < "../src/main/resources/bootstrap.properties"

readonly BINARY=${application_title,,}
readonly VERSION=$(git describe --tags)
readonly COMPANY="White Magic Software, Ltd."
readonly YEAR=$(date +%Y)
readonly DESCRIPTION="Markdown editor with live preview, variables, and math."
readonly SIZE=$(stat --format="%s" ../${BINARY}.exe)

wine rcedit-x64.exe "../${BINARY}.exe" \
  --set-icon "logo.ico" \
  --set-version-string "OriginalFilename" "${application_title}.exe" \
  --set-version-string "CompanyName" "${COMPANY}" \
  --set-version-string "ProductName" "${application_title}" \
  --set-version-string "LegalCopyright" "Copyright ${YEAR} ${COMPANY}" \
  --set-version-string "FileDescription" "${DESCRIPTION}" \
  --set-version-string "Size" "${DESCRIPTION}" \
  --set-product-version "${VERSION}" \
  --set-file-version "${VERSION}"

cp "../${BINARY}.exe" "../${application_title}.exe"

