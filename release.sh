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
  $log "Remove distribution directory"
  rm -rf "${SCRIPT_DIR}/dist"

  $log "Remove stale binaries"
  rm -f "${application_title,,}.jar"
  rm -f "${application_title,,}.bin"
  rm -f "${application_title}.exe"

  $log "Build Java archive"
  gradle clean jar
  mv "build/libs/${application_title,,}.jar" .

  $log "Build Linux installer binary"
  ${BIN_INSTALLER} -o linux

  $log "Build MacOS installer binary"
  ${BIN_INSTALLER} -o macos

  $log "Build Windows installer binary"
  ${BIN_INSTALLER} -o windows
}

preprocess() {
  while IFS='=' read -r key value; do
    if [[ "${key}" = "" || "${key}" = "#"* ]]; then
      continue
    fi

    key=$(echo $key | tr '.' '_')
    eval ${key}=\${value}
  done < "${FILE_PROPERTIES}"

  application_title="${application_title}"

  return 1
}

main "$@"

