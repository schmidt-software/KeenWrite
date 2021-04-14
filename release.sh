#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# This script builds Windows, Linux, and Java archive binaries for a
# release.
# ---------------------------------------------------------------------------

source $HOME/bin/build-template

readonly FILE_PROPERTIES="${SCRIPT_DIR}/src/main/resources/bootstrap.properties"
readonly BIN_INSTALLER="${SCRIPT_DIR}/installer.sh"

DEPENDENCIES=(
  "gradle,https://gradle.org"
  "zip,http://infozip.sourceforge.net"
  "${FILE_PROPERTIES},File containing application name"
)

execute() {
  $log "Build Windows installer binary"
  ${BIN_INSTALLER} -o windows

  $log "Build Linux installer binary"
  ${BIN_INSTALLER} -o linux

  $log "Build Java archive"
  gradle clean jar
  mv "build/libs/${application_title}.jar" .

  $log "Create theme packs"
  rm -f theme-packs.zip
  zip -9 -r theme-packs.zip themes/
}

preprocess() {
  while IFS='=' read -r key value; do
    if [[ "${key}" = "" || "${key}" = "#"* ]]; then
      continue
    fi

    key=$(echo $key | tr '.' '_')
    eval ${key}=\${value}
  done < "${FILE_PROPERTIES}"

  application_title="${application_title,,}"

  return 1
}

main "$@"

