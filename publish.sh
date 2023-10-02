#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# This script uploads the latest application release for all supported
# platforms.
# ---------------------------------------------------------------------------

readonly RELEASE=$(git describe --abbrev=0 --tags)
readonly APP_NAME=$(cut -d= -f2 ./src/main/resources/bootstrap.properties)
readonly PATH_TOKEN="tokens/${APP_NAME,,}.pat"

# ---------------------------------------------------------------------------
# Publishes a self-extracting installer to the repository.
#
# $1 - The relative path to the file to upload.
# ---------------------------------------------------------------------------
publish() {
  local -r PATH_ARCHIVE="${1}"

  if [ -f "${PATH_ARCHIVE}" ]; then
    glab release upload ${RELEASE} "${PATH_ARCHIVE}"
  else
    echo "Missing ${PATH_ARCHIVE}, continuing."
  fi
}

if [ -f "${PATH_TOKEN}" ]; then
  cat "${PATH_TOKEN}" | glab auth login --hostname gitlab.com --stdin

  publish "${APP_NAME,,}.jar"
  publish "${APP_NAME,,}.bin"
  publish "${APP_NAME,,}.app"
  publish "${APP_NAME}.exe"
else
  echo "Create ${PATH_TOKEN} before publishing the release."
fi

