#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# This script cross-compiles application launchers for different platforms.
#
# The application binaries are self-contained launchers that do not need
# to be installed.
# ---------------------------------------------------------------------------

source $HOME/bin/build-template

readonly APP_NAME=$(find "${SCRIPT_DIR}/src" -type f -name "settings.properties" -exec cat {} \; | grep "application.title=" | cut -d'=' -f2)
readonly FILE_APP_JAR="${APP_NAME}.jar"
readonly OPT_JAVA=$(cat << END_OF_ARGS
--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED \
--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
--add-opens=javafx.graphics/javafx.scene.text=ALL-UNNAMED \
--add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED \
--add-opens=javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
--add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED
END_OF_ARGS
)

ARG_JAVA_OS="linux"
ARG_JAVA_ARCH="amd64"
ARG_JAVA_VERSION="17"
ARG_JAVA_UPDATE="35"
ARG_JAVA_DIR="java"

ARG_DIR_DIST="dist"

FILE_DIST_EXEC="run.sh"

ARG_PATH_DIST_JAR="${SCRIPT_DIR}/build/libs/${FILE_APP_JAR}"

DEPENDENCIES=(
  "gradle,https://gradle.org"
  "warp-packer,https://github.com/dgiagio/warp"
  "tar,https://www.gnu.org/software/tar"
  "unzip,http://infozip.sourceforge.net"
)

ARGUMENTS+=(
  "a,arch,Target operating system architecture (amd64)"
  "o,os,Target operating system (linux, windows, mac)"
  "u,update,Java update version number (${ARG_JAVA_UPDATE})"
  "v,version,Full Java version (${ARG_JAVA_VERSION})"
)

ARCHIVE_EXT="tar.gz"
ARCHIVE_APP="tar xf"
APP_EXTENSION="bin"

# ---------------------------------------------------------------------------
# Generates
# ---------------------------------------------------------------------------
execute() {
  $do_configure_target
  $do_build
  $do_clean

  pushd "${ARG_DIR_DIST}" > /dev/null 2>&1

  $do_extract_java
  $do_create_launch_script
  $do_copy_archive

  popd > /dev/null 2>&1

  $do_create_launcher

  return 1
}

# ---------------------------------------------------------------------------
# Configure platform-specific commands and file names.
# ---------------------------------------------------------------------------
utile_configure_target() {
  if [ "${ARG_JAVA_OS}" = "windows" ]; then
    ARCHIVE_EXT="zip"
    ARCHIVE_APP="unzip -qq"
    FILE_DIST_EXEC="run.bat"
    APP_EXTENSION="exe"
    do_create_launch_script=utile_create_launch_script_windows
  fi
}

# ---------------------------------------------------------------------------
# Build platform-specific überjar.
# ---------------------------------------------------------------------------
utile_build() {
  $log "Delete ${ARG_PATH_DIST_JAR}"
  rm -f "${ARG_PATH_DIST_JAR}"

  $log "Build application for ${ARG_JAVA_OS}"
  gradle clean jar -PtargetOs="${ARG_JAVA_OS}"
}

# ---------------------------------------------------------------------------
# Purges the existing distribution directory to recreate the launcher.
# This refreshes the JRE from the downloaded archive.
# ---------------------------------------------------------------------------
utile_clean() {
  $log "Recreate ${ARG_DIR_DIST}"
  rm -rf "${ARG_DIR_DIST}"
  mkdir -p "${ARG_DIR_DIST}"
}

# ---------------------------------------------------------------------------
# Extract platform-specific Java Runtime Environment. This will download
# and cache the required Java Runtime Environment for the target platform.
# On subsequent runs, the cached version is used, instead of issuing another
# download.
# ---------------------------------------------------------------------------
utile_extract_java() {
  $log "Extract Java"
  local -r java_vm="jre"
  local -r java_version="${ARG_JAVA_VERSION}+${ARG_JAVA_UPDATE}"
  local -r url_java="https://download.bell-sw.com/java/${java_version}/bellsoft-${java_vm}${java_version}-${ARG_JAVA_OS}-${ARG_JAVA_ARCH}-full.${ARCHIVE_EXT}"

  local -r file_java="${java_vm}-${java_version}-${ARG_JAVA_OS}-${ARG_JAVA_ARCH}.${ARCHIVE_EXT}"
  local -r path_java="/tmp/${file_java}"

  # File must have contents.
  if [ ! -s ${path_java} ]; then
    $log "Download ${url_java} to ${path_java}"
    wget -q "${url_java}" -O "${path_java}"
  fi

  $log "Unpack ${path_java}"
  $ARCHIVE_APP "${path_java}"

  local -r dir_java="${java_vm}-${ARG_JAVA_VERSION}-full"

  $log "Rename ${dir_java} to ${ARG_JAVA_DIR}"
  mv "${dir_java}" "${ARG_JAVA_DIR}"
}

# ---------------------------------------------------------------------------
# Create Linux-specific launch script.
# ---------------------------------------------------------------------------
utile_create_launch_script_linux() {
  $log "Create Linux launch script"

  cat > "${FILE_DIST_EXEC}" << __EOT
#!/usr/bin/env bash

readonly SCRIPT_SRC="\$(dirname "\${BASH_SOURCE[\${#BASH_SOURCE[@]} - 1]}")"

"\${SCRIPT_SRC}/${ARG_JAVA_DIR}/bin/java" ${OPT_JAVA} -jar "\${SCRIPT_SRC}/${FILE_APP_JAR}" "\$@" 2>/dev/null &
__EOT

  chmod +x "${FILE_DIST_EXEC}"
}

# ---------------------------------------------------------------------------
# Create Windows-specific launch script.
# ---------------------------------------------------------------------------
utile_create_launch_script_windows() {
  $log "Create Windows launch script"

  cat > "${FILE_DIST_EXEC}" << __EOT
@echo off

set SCRIPT_DIR=%~dp0
"%SCRIPT_DIR%\\${ARG_JAVA_DIR}\\bin\\java" ${OPT_JAVA} -jar "%SCRIPT_DIR%\\${APP_NAME}.jar" %* 2>nul
__EOT

  # Convert Unix end of line characters (\n) to Windows format (\r\n).
  # This avoids any potential line conversion issues with the repository.
  sed -i 's/$/\r/' "${FILE_DIST_EXEC}"
}

# ---------------------------------------------------------------------------
# Copy application überjar.
# ---------------------------------------------------------------------------
utile_copy_archive() {
  $log "Create copy of ${FILE_APP_JAR}"
  cp "${ARG_PATH_DIST_JAR}" "${FILE_APP_JAR}"
}

# ---------------------------------------------------------------------------
# Create platform-specific launcher binary.
# ---------------------------------------------------------------------------
utile_create_launcher() {
  local -r FILE_APP_NAME="${APP_NAME}.${APP_EXTENSION}"
  $log "Create ${FILE_APP_NAME}"

  # Warp-packer does not seem to overwrite the file.
  rm -f "${FILE_APP_NAME}"

  # Download uses amd64, but warp-packer differs.
  if [ "${ARG_JAVA_ARCH}" = "amd64" ]; then
    ARG_JAVA_ARCH="x64"
  fi

  warp-packer \
    --arch "${ARG_JAVA_OS}-${ARG_JAVA_ARCH}" \
    --input_dir "${ARG_DIR_DIST}" \
    --exec "${FILE_DIST_EXEC}" \
    --output "${FILE_APP_NAME}" > /dev/null

  chmod +x "${FILE_APP_NAME}"
}

argument() {
  local consume=2

  case "$1" in
    -a|--arch)
    ARG_JAVA_ARCH="$2"
    ;;
    -o|--os)
    ARG_JAVA_OS="$2"
    ;;
    -u|--update)
    ARG_JAVA_UPDATE="$2"
    ;;
    -v|--version)
    ARG_JAVA_VERSION="$2"
    ;;
  esac

  return ${consume}
}

do_configure_target=utile_configure_target
do_build=utile_build
do_clean=utile_clean
do_extract_java=utile_extract_java
do_create_launch_script=utile_create_launch_script_linux
do_copy_archive=utile_copy_archive
do_create_launcher=utile_create_launcher

main "$@"

