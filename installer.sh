#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# This script cross-compiles application launchers for different platforms.
#
# The application binaries are self-contained launchers that do not need
# to be installed.
# ---------------------------------------------------------------------------

source $HOME/bin/build-template

# Case sensitive application name.
readonly APP_NAME=$(cat \
  "${SCRIPT_DIR}/src/main/resources/bootstrap.properties" | \
  cut -d'=' -f2
)
# Lowercase application name.
readonly APP_NAME_LC=${APP_NAME,,}
readonly FILE_APP_JAR="${APP_NAME_LC}.jar"

readonly OPT_JAVA=$(cat << END_OF_ARGS
-Dprism.order=sw \
--enable-preview \
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
--add-exports=javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED
END_OF_ARGS
)

ARG_JAVA_OS="linux"
ARG_JAVA_ARCH="amd64"
ARG_JAVA_VERSION="21.0.2"
ARG_JAVA_UPDATE="14"
ARG_JAVA_DIR="java"

ARG_DIR_DIST="dist"

FILE_LAUNCHER_SCRIPT="run.sh"

ARG_PATH_DIST_JAR="${SCRIPT_DIR}/build/libs/${FILE_APP_JAR}"

DEPENDENCIES=(
  "gradle,https://gradle.org"
  "warp-packer,https://github.com/Reisz/warp/releases"
  "linux-x64.warp-packer,https://github.com/dgiagio/warp/releases"
  "osslsigncode,https://www.winehq.org"
  "tar,https://www.gnu.org/software/tar"
  "wine,https://www.winehq.org"
  "unzip,http://infozip.sourceforge.net"
)

ARGUMENTS+=(
  "a,arch,Target operating system architecture (amd64)"
  "o,os,Target operating system (linux, windows, macos)"
  "u,update,Java update version number (${ARG_JAVA_UPDATE})"
  "v,version,Full Java version (${ARG_JAVA_VERSION})"
)

ARCHIVE_EXT="tar.gz"
ARCHIVE_APP="tar xf"
APP_EXTENSION="bin"

# ---------------------------------------------------------------------------
# Generates an application binary as a self-extracting installer.
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

  $do_brand_windows
  $do_sign_windows

  return 1
}

# ---------------------------------------------------------------------------
# Configure platform-specific commands and file names.
# ---------------------------------------------------------------------------
utile_configure_target() {
  if [ "${ARG_JAVA_OS}" = "windows" ]; then
    ARCHIVE_EXT="zip"
    ARCHIVE_APP="unzip -qq"
    FILE_LAUNCHER_SCRIPT="run.bat"
    APP_EXTENSION="exe"
    do_create_launch_script=utile_create_launch_script_windows
    do_brand_windows=utile_brand_windows
    do_sign_windows=utile_sign_windows
  elif [ "${ARG_JAVA_OS}" = "macos" ]; then
    APP_EXTENSION="app"
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

  java_os="${ARG_JAVA_OS}"
  java_arch="${ARG_JAVA_ARCH}"
  archive_ext=""

  if [ "${ARG_JAVA_OS}" = "macos" ]; then
    archive_ext=".jre"
  fi

  local -r url_java="https://download.bell-sw.com/java/${java_version}/bellsoft-${java_vm}${java_version}-${java_os}-${java_arch}-full.${ARCHIVE_EXT}"

  local -r file_java="${java_vm}-${java_version}-${java_os}-${java_arch}.${ARCHIVE_EXT}"
  local -r path_java="/tmp/${file_java}"

  # File must have contents.
  if [ ! -s ${path_java} ]; then
    $log "Download ${url_java} to ${path_java}"
    wget -q "${url_java}" -O "${path_java}"
  fi

  $log "Unpack ${path_java}"
  $ARCHIVE_APP "${path_java}"

  local -r dir_java="${java_vm}-${ARG_JAVA_VERSION}-full${archive_ext}"

  $log "Rename ${dir_java} to ${ARG_JAVA_DIR}"
  mv "${dir_java}" "${ARG_JAVA_DIR}"
}

# ---------------------------------------------------------------------------
# Create Linux-specific launch script.
# ---------------------------------------------------------------------------
utile_create_launch_script_linux() {
  $log "Create Linux launch script"

  cat > "${FILE_LAUNCHER_SCRIPT}" << __EOT
#!/usr/bin/env bash

readonly SCRIPT_SRC="\$(dirname "\${BASH_SOURCE[\${#BASH_SOURCE[@]} - 1]}")"

"\${SCRIPT_SRC}/${ARG_JAVA_DIR}/bin/java" ${OPT_JAVA} -jar "\${SCRIPT_SRC}/${FILE_APP_JAR}" "\$@" 2>/dev/null
__EOT

  chmod +x "${FILE_LAUNCHER_SCRIPT}"
}

# ---------------------------------------------------------------------------
# Create Windows-specific launch script.
# ---------------------------------------------------------------------------
utile_create_launch_script_windows() {
  $log "Create Windows launch script"

  cat > "${FILE_LAUNCHER_SCRIPT}" << __EOT
@echo off

set SCRIPT_DIR=%~dp0
"%SCRIPT_DIR%\\${ARG_JAVA_DIR}\\bin\\java" ${OPT_JAVA} -jar "%SCRIPT_DIR%\\${FILE_APP_JAR}" %* 2>nul
__EOT

  # Convert Unix end of line characters (\n) to Windows format (\r\n).
  # This avoids any potential line conversion issues with the repository.
  sed -i 's/$/\r/' "${FILE_LAUNCHER_SCRIPT}"
}

# ---------------------------------------------------------------------------
# Modify the Windows binary to include icon and identifying information.
# ---------------------------------------------------------------------------
utile_brand_windows() {
  local -r BINARY="${APP_NAME}.exe"
  local -r BINARY_LC="${APP_NAME_LC}.exe"
  local -r VERSION=$(git describe --tags)
  local -r COMPANY="White Magic Software, Ltd."
  local -r YEAR=$(date +%Y)
  local -r DESCRIPTION="Markdown editor with live preview, variables, and math."
  local -r SIZE=$(stat --format="%s" ${BINARY_LC})

  $log "Brand ${BINARY_LC}"
  wine "${SCRIPT_DIR}/scripts/rcedit-x64.exe" "${BINARY_LC}" \
    --set-icon "scripts/logo.ico" \
    --set-version-string "OriginalFilename" "${BINARY}" \
    --set-version-string "CompanyName" "${COMPANY}" \
    --set-version-string "ProductName" "${APP_NAME}" \
    --set-version-string "LegalCopyright" "Copyright ${YEAR} ${COMPANY}" \
    --set-version-string "FileDescription" "${DESCRIPTION}" \
    --set-version-string "Size" "${DESCRIPTION}" \
    --set-product-version "${VERSION}" \
    --set-file-version "${VERSION}"

  $log "Rename ${BINARY_LC} to ${BINARY}"
  mv -f "${BINARY_LC}" "${BINARY}"
}

# ---------------------------------------------------------------------------
# Modify the Windows binary to include signed certificate information.
# ---------------------------------------------------------------------------
utile_sign_windows() {
  local -r FILE_CERTIFICATE="${SCRIPT_DIR}/tokens/code-sign-cert.pfx"
  local -r FILE_BINARY="${APP_NAME}.exe"
  local -r FILE_SIGNED_BINARY="signed-${FILE_BINARY}"

  rm -f "${FILE_SIGNED_BINARY}"

  $log "Sign ${FILE_BINARY}"
  osslsigncode sign \
    -pkcs12 "${FILE_CERTIFICATE}" \
    -askpass \
    -n "${APP_NAME}" \
    -i "https://www.${APP_NAME_LC}.com" \
    -in "${FILE_BINARY}" \
    -out "${FILE_SIGNED_BINARY}"

  $log "Rename ${FILE_SIGNED_BINARY} to ${FILE_BINARY}"
  mv -f "${FILE_SIGNED_BINARY}" "${FILE_BINARY}"
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
  packer=warp-packer
  packer_opt_pack="pack"
  packer_opt_input="input-dir"

  local -r FILE_APP_NAME="${APP_NAME_LC}.${APP_EXTENSION}"
  $log "Create ${FILE_APP_NAME}"

  # Warp-packer does not overwrite the file.
  rm -f "${FILE_APP_NAME}"

  # Download uses amd64, but warp-packer differs.
  if [ "${ARG_JAVA_ARCH}" = "amd64" ]; then
    ARG_JAVA_ARCH="x64"
  fi

  # The warp-packer fork that fixes Windows doesn't support MacOS.
  if [ "${ARG_JAVA_OS}" = "macos" ]; then
    packer=linux-x64.warp-packer
    packer_opt_pack=""
    packer_opt_input="input_dir"
  fi

  ${packer} \
    ${packer_opt_pack} \
    --arch "${ARG_JAVA_OS}-${ARG_JAVA_ARCH}" \
    --${packer_opt_input} "${ARG_DIR_DIST}" \
    --exec "${FILE_LAUNCHER_SCRIPT}" \
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
do_brand_windows=:
do_sign_windows=:

main "$@"

