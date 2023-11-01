#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# This script uploads the latest application release for all supported
# platforms.
# ---------------------------------------------------------------------------

readonly RELEASE=$(git describe --abbrev=0 --tags)
readonly APP_NAME=$(cut -d= -f2 ./src/main/resources/bootstrap.properties)
readonly APP_NAME_LC=${APP_NAME,,}
readonly PATH_TOKEN="tokens/${APP_NAME_LC}.pat"
readonly URL=$(cat "tokens/publish.url")

# ---------------------------------------------------------------------------
# Adds download URLs to a release.
#
# $1 - The system (Linux, WIndows, MacOS, Java)
# ---------------------------------------------------------------------------
release() {
  local -r OS="${1}"
  local ARCH=" (64-bit, x86)"
  local FILE_PREFIX="${APP_NAME_LC}"
  local FILE_SUFFIX="bin"

  case ${OS} in
    MacOS)
      FILE_SUFFIX="app"
    ;;
    Windows)
      FILE_PREFIX="${APP_NAME}"
      FILE_SUFFIX="exe"
    ;;
    Java)
      ARCH=""
      FILE_SUFFIX="jar"
    ;;
    *)
      # Linux, others
    ;;
  esac

  local -r BINARY="${FILE_PREFIX}.${FILE_SUFFIX}"

  publish "${BINARY}"

  glab release upload ${RELEASE} \
    --assets-links="[{
      \"name\":\"${APP_NAME} for ${OS}${ARCH}\",
      \"url\":\"https://${APP_NAME_LC}.com/downloads/${BINARY}\",
      \"link_type\":\"other\"
    }]"
}

# ---------------------------------------------------------------------------
# Publishes a self-extracting installer to the repository.
#
# $1 - The relative path to the file to upload.
# ---------------------------------------------------------------------------
publish() {
  local -r FILE_BINARY="${1}"

  if [ -f "${FILE_BINARY}" ]; then
    scp "${FILE_BINARY}" "${URL}"
  else
    echo "Missing ${FILE_BINARY}, continuing."
  fi
}

if [ -f "${PATH_TOKEN}" ]; then
  cat "${PATH_TOKEN}" | glab auth login --hostname gitlab.com --stdin

  release "Windows"
  release "MacOS"
  release "Linux"
  release "Java"
else
  echo "Create ${PATH_TOKEN} before publishing the release."
fi

