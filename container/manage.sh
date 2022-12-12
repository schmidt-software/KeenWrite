#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# Copyright 2022 White Magic Software, Ltd.
#
# This script helps manage a container. The container is configured to
# run ConTeXt.
# ---------------------------------------------------------------------------

source ../scripts/build-template

readonly CONTAINER_NAME=typesetter
readonly CONTAINER_NETWORK=host
readonly CONTAINER_FILE=${CONTAINER_NAME}
readonly CONTAINER_ARCHIVE_FILE=${CONTAINER_FILE}.tar
readonly CONTAINER_COMPRESSED_FILE=${CONTAINER_ARCHIVE_FILE}.gz
readonly CONTAINER_EXE=podman

DEPENDENCIES=(
  "podman,https://podman.io"
  "tar,https://www.gnu.org/software/tar"
  "bzip2,https://gitlab.com/bzip2/bzip2"
)

ARGUMENTS+=(
  "b,build,Build container image (${CONTAINER_NAME})"
  "c,connect,Connect to container image"
  "l,load,Load container image (${CONTAINER_COMPRESSED_FILE})"
  "r,remove,Remove all container images"
  "s,save,Save container image (${CONTAINER_COMPRESSED_FILE})"
)

# ---------------------------------------------------------------------------
# Manages the container image
# ---------------------------------------------------------------------------
execute() {
  $do_remove
  $do_build
  $do_save
  $do_load
  $do_execute
  $do_connect

  return 1
}

# ---------------------------------------------------------------------------
# Removes all container images
# ---------------------------------------------------------------------------
utile_remove() {
  $log "Removing all images"

  ${CONTAINER_EXE} rmi --all --force > /dev/null

  $log "Images removed"
}

# ---------------------------------------------------------------------------
# Builds the container file in the current working directory
# ---------------------------------------------------------------------------
utile_build() {
  ${CONTAINER_EXE} build \
    --network=${CONTAINER_NETWORK} \
    --tag ${CONTAINER_NAME} . | \
  grep ^STEP
}

# ---------------------------------------------------------------------------
# Connects to the container
# ---------------------------------------------------------------------------
utile_connect() {
  ${CONTAINER_EXE} run \
    --network=${CONTAINER_NETWORK} \
    --rm \
    -i \
    -t ${CONTAINER_NAME}
}

# ---------------------------------------------------------------------------
# Runs a command in the container
# ---------------------------------------------------------------------------
utile_execute() {
# -v ${IMAGES_DIR}:/root/images:ro \
  ${CONTAINER_EXE} run \
    --network=${CONTAINER_NETWORK} \
    --rm \
    -i \
    -t ${CONTAINER_NAME} \
    /bin/sh --login -c 'context --version'
}

# ---------------------------------------------------------------------------
# Saves the container to a file
# ---------------------------------------------------------------------------
utile_save() {
  if [[ -f "${CONTAINER_COMPRESSED_FILE}" ]]; then
    warning "${CONTAINER_COMPRESSED_FILE} exists, delete before saving."
  else
    $log "Saving ${CONTAINER_NAME} image ..."

    ${CONTAINER_EXE} save \
      --quiet \
      -o "${CONTAINER_ARCHIVE_FILE}" \
      "${CONTAINER_NAME}"

    $log "Compressing to ${CONTAINER_COMPRESSED_FILE} ..."
    gzip "${CONTAINER_ARCHIVE_FILE}"

    $log "Saved ${CONTAINER_NAME} image"
  fi
}

# ---------------------------------------------------------------------------
# Loads the container from a file
# ---------------------------------------------------------------------------
utile_load() {
  if [[ -f "${CONTAINER_COMPRESSED_FILE}" ]]; then
    $log "Loading ${CONTAINER_NAME} image ..."

    ${CONTAINER_EXE} load \
      --quiet \
      -i "${CONTAINER_COMPRESSED_FILE}"

    $log "Loaded ${CONTAINER_NAME} image"
  else
    warning "Missing ${CONTAINER_COMPRESSED_FILE}; use build follwed by save"
  fi
}

argument() {
  local consume=1

  case "$1" in
    -b|--build)
    do_remove=utile_build
    ;;
    -c|--connect)
    do_connect=utile_connect
    ;;
    -l|--load)
    do_load=utile_load
    ;;
    -r|--remove)
    do_remove=utile_remove
    ;;
    -s|--save)
    do_save=utile_save
    ;;
  esac

  return ${consume}
}

do_build=:
do_connect=:
do_execute=:
do_load=:
do_remove=:
do_save=:

main "$@"

