#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# Copyright 2022 White Magic Software, Ltd.
#
# This script helps manage a container. The container is configured to
# run ConTeXt.
# ---------------------------------------------------------------------------

source ../scripts/build-template

readonly BUILD_DIR=build

readonly CONTAINER_EXE=podman
readonly CONTAINER_SHORTNAME=typesetter
readonly CONTAINER_VERSION=$(git describe --abbrev=0)
readonly CONTAINER_NAME="${CONTAINER_SHORTNAME}:${CONTAINER_VERSION}"
readonly CONTAINER_NETWORK=host
readonly CONTAINER_FILE="${CONTAINER_SHORTNAME}"
readonly CONTAINER_ARCHIVE_FILE="${CONTAINER_FILE}.tar"
readonly CONTAINER_ARCHIVE_PATH="${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}"
readonly CONTAINER_COMPRESSED_FILE="${CONTAINER_ARCHIVE_FILE}.gz"
readonly CONTAINER_COMPRESSED_PATH="${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}.gz"
readonly CONTAINER_DIR_TEXT="/root/text"
readonly CONTAINER_DIR_IMAGES="/root/images"
readonly CONTAINER_DIR_OUTPUT="/root/output"
readonly CONTAINER_REPO=ghcr.io

ARG_CONTAINER_COMMAND="context --version"
ARG_MOUNTPOINT_TEXT=""
ARG_MOUNTPOINT_IMAGES=""
ARG_MOUNTPOINT_OUTPUT="."
ARG_ACCESS_TOKEN=""

DEPENDENCIES=(
  "podman,https://podman.io"
  "tar,https://www.gnu.org/software/tar"
  "bzip2,https://gitlab.com/bzip2/bzip2"
)

ARGUMENTS+=(
  "b,build,Build container (${CONTAINER_NAME})"
  "c,connect,Connect to container"
  "d,delete,Remove all containers"
  "i,images,Set mount point for image files (to typeset)"
  "k,token,Set personal access token (to publish)"
  "l,load,Load container (${CONTAINER_COMPRESSED_PATH})"
  "o,output,Set mount point for output files (after typesetting)"
  "p,publish,Publish the container (after logging in)"
  "r,run,Run a command in the container (\"${ARG_CONTAINER_COMMAND}\")"
  "s,save,Save container (${CONTAINER_COMPRESSED_PATH})"
  "t,text,Set mount point for text file (to typeset)"
)

# ---------------------------------------------------------------------------
# Manages the container.
# ---------------------------------------------------------------------------
execute() {
  $do_delete
  $do_build
  $do_publish
  $do_save
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
  $log "Building ${CONTAINER_NAME}"

  # Show what commands are run while building, but not the commands' output.
  ${CONTAINER_EXE} build \
    --network=${CONTAINER_NETWORK} \
    --squash \
    -t ${CONTAINER_NAME} . | \
  grep ^STEP
}

# ---------------------------------------------------------------------------
# Publishes the container to the repository.
# ---------------------------------------------------------------------------
utile_publish() {
  local -r username=$(git config user.name | tr '[A-Z]' '[a-z]')
  local -r repo="${CONTAINER_REPO}/${username}/${container}"

  if [ ! -z ${ARG_ACCESS_TOKEN} ]; then
    echo ${ARG_ACCESS_TOKEN} | \
      ${CONTAINER_EXE} login ghcr.io -u $(git config user.name) --password-stdin

    $log "Tagging ${CONTAINER_NAME}"

    ${CONTAINER_EXE} tag ${CONTAINER_NAME} ${repo}

    $log "Pushing ${CONTAINER_NAME} to ${CONTAINER_REPO}"

    ${CONTAINER_EXE} push ${repo}

    $log "Published ${CONTAINER_NAME} to ${CONTAINER_REPO}"
  else
    error "Provide a personal access token to publish."
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
  $log "Mounting ${1} as ${2}"

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

get_mountpoint_text() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_TEXT}" "${CONTAINER_DIR_TEXT}")
}

get_mountpoint_images() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_IMAGES}" "${CONTAINER_DIR_IMAGES}")
}

get_mountpoint_output() {
  echo $(get_mountpoint "${ARG_MOUNTPOINT_OUTPUT}" "${CONTAINER_DIR_OUTPUT}" 1)
}

# ---------------------------------------------------------------------------
# Connects to the container.
# ---------------------------------------------------------------------------
utile_connect() {
  $log "Connecting to container"

  declare -r mount_text=$(get_mountpoint_text)
  declare -r mount_images=$(get_mountpoint_images)
  declare -r mount_output=$(get_mountpoint_output)

  ${CONTAINER_EXE} run \
    --network="${CONTAINER_NETWORK}" \
    --rm \
    -it \
    ${mount_text} \
    ${mount_images} \
    ${mount_output} \
    "${CONTAINER_NAME}"
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
    -t "${CONTAINER_NAME}" \
    /bin/sh --login -c "${ARG_CONTAINER_COMMAND}"
}

# ---------------------------------------------------------------------------
# Saves the container to a file.
# ---------------------------------------------------------------------------
utile_save() {
  if [[ -f "${CONTAINER_COMPRESSED_PATH}" ]]; then
    warning "${CONTAINER_COMPRESSED_PATH} exists, delete before saving."
  else
    $log "Saving ${CONTAINER_SHORTNAME} image"

    mkdir -p "${BUILD_DIR}"

    ${CONTAINER_EXE} save \
      --quiet \
      -o "${BUILD_DIR}/${CONTAINER_ARCHIVE_FILE}" \
      "${CONTAINER_SHORTNAME}"

    $log "Compressing to ${CONTAINER_COMPRESSED_PATH}"
    gzip "${CONTAINER_ARCHIVE_PATH}"

    $log "Saved ${CONTAINER_SHORTNAME} image"
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
    warning "Missing ${CONTAINER_COMPRESSED_PATH}; use build follwed by save"
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
    -k|--token)
    if [ ! -z "${2+x}" ]; then
      ARG_ACCESS_TOKEN="$2"
      consume=2
    fi
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
    -o|--output)
    if [ ! -z "${2+x}" ]; then
      ARG_MOUNTPOINT_OUTPUT="$2"
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
    -s|--save)
    do_save=utile_save
    ;;
    -t|--text)
    if [ ! -z "${2+x}" ]; then
      ARG_MOUNTPOINT_TEXT="$2"
      consume=2
    fi
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
do_save=:

main "$@"

