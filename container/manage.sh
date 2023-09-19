#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# Copyright 2022 White Magic Software, Ltd.
#
# This script manages the container configured to run ConTeXt.
# ---------------------------------------------------------------------------

source ../scripts/build-template

# Reads the value of a property from a properties file.
#
# $1 - The key name to obtain.
function property {
  grep "^${1}" "${PROPERTIES}" | cut -d'=' -f2
}

readonly BUILD_DIR=build
readonly PROPERTIES="${SCRIPT_DIR}/../src/main/resources/com/keenwrite/messages.properties"

readonly CONTAINER_EXE=podman
readonly CONTAINER_SHORTNAME=$(property Wizard.typesetter.container.image.name)
readonly CONTAINER_VERSION=$(property Wizard.typesetter.container.image.version)
readonly CONTAINER_NETWORK=host
readonly CONTAINER_FILE="${CONTAINER_SHORTNAME}-${CONTAINER_VERSION}"
readonly CONTAINER_ARCHIVE_FILE="${CONTAINER_FILE}.tar"
readonly CONTAINER_ARCHIVE_PATH="${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}"
readonly CONTAINER_COMPRESSED_FILE="${CONTAINER_ARCHIVE_FILE}.gz"
readonly CONTAINER_COMPRESSED_PATH="${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}.gz"
readonly CONTAINER_IMAGE_FILE="${BUILD_DIR}/${CONTAINER_FILE}"
readonly CONTAINER_DIR_SOURCE="/root/source"
readonly CONTAINER_DIR_TARGET="/root/target"
readonly CONTAINER_DIR_IMAGES="/root/images"
readonly CONTAINER_DIR_FONTS="/root/fonts"

ARG_CONTAINER_NAME="${CONTAINER_SHORTNAME}:${CONTAINER_VERSION}"
ARG_CONTAINER_COMMAND="context --version"
ARG_MOUNTPOINT_SOURCE=""
ARG_MOUNTPOINT_TARGET="."
ARG_MOUNTPOINT_IMAGES=""
ARG_MOUNTPOINT_FONTS="${HOME}/.fonts"

DEPENDENCIES=(
  "podman,https://podman.io"
  "tar,https://www.gnu.org/software/tar"
  "bzip2,https://gitlab.com/bzip2/bzip2"
)

ARGUMENTS+=(
  "b,build,Build container"
  "c,connect,Connect to container"
  "d,delete,Remove all containers"
  "s,source,Set mount point for input document (before typesetting)"
  "t,target,Set mount point for output file (after typesetting)"
  "i,images,Set mount point for image files (to typeset)"
  "f,fonts,Set mount point for font files (during typesetting)"
  "l,load,Load container (${CONTAINER_COMPRESSED_PATH})"
  "p,publish,Publish the container"
  "r,run,Run a command in the container (\"${ARG_CONTAINER_COMMAND}\")"
  "x,export,Save container (${CONTAINER_COMPRESSED_PATH})"
)

# ---------------------------------------------------------------------------
# Manages the container.
# ---------------------------------------------------------------------------
execute() {
  $do_delete
  $do_build
  $do_publish
  $do_export
  $do_load
  $do_execute
  $do_connect

  return 1
}

# ---------------------------------------------------------------------------
# Deletes all containers.
# ---------------------------------------------------------------------------
utile_delete() {
  $log "Deleting all containers"

  ${CONTAINER_EXE} rmi --all --force > /dev/null

  $log "Containers deleted"
}

# ---------------------------------------------------------------------------
# Builds the container file in the current working directory.
# ---------------------------------------------------------------------------
utile_build() {
  $log "Building"

  # Show what commands are run while building, but not the commands' output.
  ${CONTAINER_EXE} build \
    --network="${CONTAINER_NETWORK}" \
    --squash \
    -t "${ARG_CONTAINER_NAME}" . | \
  grep ^STEP
}

# ---------------------------------------------------------------------------
# Publishes the container to the repository.
# ---------------------------------------------------------------------------
utile_publish() {
  local -r TOKEN_FILE="token.txt"

  if [[ -f "${TOKEN_FILE}" ]]; then
    local -r repository=$(cat ${TOKEN_FILE})
    local -r remote_file="${CONTAINER_SHORTNAME}:${CONTAINER_VERSION}"
    local -r remote_path="${repository}/${remote_file}"

    $log "Publishing to ${remote_path}"

    # Path to the repository.
    scp -q "${CONTAINER_IMAGE_FILE}" "${remote_path}"
  else
    error "Create ${TOKEN_FILE} with publish credentials"
  fi
}

# ---------------------------------------------------------------------------
# Creates the command-line option for a read-only mountpoint.
#
# $1 - The host directory.
# $2 - The guest (container) directory.
# $3 - The file system permissions (set to 1 for read-write).
# ---------------------------------------------------------------------------
get_mountpoint() {
  local result=""
  local binding="ro"

  if [ ! -z "${3+x}" ]; then
    binding="Z"
  fi

  if [ ! -z "${1}" ]; then
    result="-v ${1}:${2}:${binding}"
  fi

  echo "${result}"
}

get_mountpoint_source() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_SOURCE}" "${CONTAINER_DIR_SOURCE}")
}

get_mountpoint_target() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_TARGET}" "${CONTAINER_DIR_TARGET}" 1)
}

get_mountpoint_images() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_IMAGES}" "${CONTAINER_DIR_IMAGES}")
}

get_mountpoint_fonts() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_FONTS}" "${CONTAINER_DIR_FONTS}")
}

# ---------------------------------------------------------------------------
# Connects to the container.
# ---------------------------------------------------------------------------
utile_connect() {
  $log "Connecting to container"

  declare -r mount_source=$(get_mountpoint_source)
  declare -r mount_target=$(get_mountpoint_target)
  declare -r mount_images=$(get_mountpoint_images)
  declare -r mount_fonts=$(get_mountpoint_fonts)

  $log "mount_source = '${mount_source}'"
  $log "mount_target = '${mount_target}'"
  $log "mount_images = '${mount_images}'"
  $log "mount_fonts = '${mount_fonts}'"

  ${CONTAINER_EXE} run \
    --network="${CONTAINER_NETWORK}" \
    --rm \
    -it \
    ${mount_source} \
    ${mount_target} \
    ${mount_images} \
    ${mount_fonts} \
    "${ARG_CONTAINER_NAME}"
}

# ---------------------------------------------------------------------------
# Runs a command in the container.
#
# Examples:
#
#   ./manage.sh -r "ls /"
#   ./manage.sh -r "context --version"
# ---------------------------------------------------------------------------
utile_execute() {
  $log "Running \"${ARG_CONTAINER_COMMAND}\":"

  ${CONTAINER_EXE} run \
    --network=${CONTAINER_NETWORK} \
    --rm \
    -i \
    -t "${ARG_CONTAINER_NAME}" \
    /bin/sh --login -c "${ARG_CONTAINER_COMMAND}"
}

# ---------------------------------------------------------------------------
# Saves the container to a file.
# ---------------------------------------------------------------------------
utile_export() {
  if [[ -f "${CONTAINER_COMPRESSED_PATH}" ]]; then
    warning "${CONTAINER_COMPRESSED_PATH} exists, delete before saving."
  else
    $log "Saving ${CONTAINER_SHORTNAME} image"

    mkdir -p "${BUILD_DIR}"

    ${CONTAINER_EXE} save \
      --quiet \
      -o "${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}" \
      "${ARG_CONTAINER_NAME}"

    $log "Compressing to ${CONTAINER_COMPRESSED_PATH}"
    gzip "${CONTAINER_ARCHIVE_PATH}"

    $log "Renaming to ${CONTAINER_IMAGE_FILE}"
    mv "${CONTAINER_COMPRESSED_PATH}" "${CONTAINER_IMAGE_FILE}"

    $log "Saved ${CONTAINER_IMAGE_FILE} image"
  fi
}

# ---------------------------------------------------------------------------
# Loads the container from a file.
# ---------------------------------------------------------------------------
utile_load() {
  if [[ -f "${CONTAINER_COMPRESSED_PATH}" ]]; then
    $log "Loading ${CONTAINER_SHORTNAME} from ${CONTAINER_COMPRESSED_PATH}"

    ${CONTAINER_EXE} load \
      --quiet \
      -i "${CONTAINER_COMPRESSED_PATH}"

    $log "Loaded ${CONTAINER_SHORTNAME} image"
  else
    warning "Missing ${CONTAINER_COMPRESSED_PATH}; use build followed by save"
  fi
}

argument() {
  local consume=1

  case "$1" in
    -b|--build)
    do_build=utile_build
    ;;
    -c|--connect)
    do_connect=utile_connect
    ;;
    -d|--delete)
    do_delete=utile_delete
    ;;
    -l|--load)
    do_load=utile_load
    ;;
    -i|--images)
    if [ ! -z "${2+x}" ]; then
      ARG_MOUNTPOINT_IMAGES="$2"
      consume=2
    fi
    ;;
    -t|--target)
    if [ ! -z "${2+x}" ]; then
      ARG_MOUNTPOINT_TARGET="$2"
      consume=2
    fi
    ;;
    -p|--publish)
    do_publish=utile_publish
    ;;
    -r|--run)
    do_execute=utile_execute

    if [ ! -z "${2+x}" ]; then
      ARG_CONTAINER_COMMAND="$2"
      consume=2
    fi
    ;;
    -s|--source)
    if [ ! -z "${2+x}" ]; then
      ARG_MOUNTPOINT_SOURCE="$2"
      consume=2
    fi
    ;;
    -x|--export)
    do_export=utile_export
    ;;
  esac

  return ${consume}
}

do_build=:
do_connect=:
do_delete=:
do_execute=:
do_load=:
do_publish=:
do_export=:

main "$@"

