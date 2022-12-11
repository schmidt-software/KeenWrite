#!/usr/bin/env bash

readonly CONTAINER_NAME=typesetter

readonly CONTAINER_NETWORK=host

# Force clean
podman rmi --all --force

# Build from Containerfile
podman build \
  --network=${CONTAINER_NETWORK} \
  --tag ${CONTAINER_NAME} .

# Connect and mount images
podman run \
  --network=${CONTAINER_NETWORK} \
  --rm \
  -i \
  -t ${CONTAINER_NAME} \
  /bin/sh --login -c 'context --version'

# -v ${IMAGES_DIR}:/root/images:ro \

# Create a persistent container
# podman create typesetter typesetter

# Create a long-running task
# podman create -ti typesetter /bin/sh

# Connect

# Export
# podman image save context -o typesetter.tar
# zip -9 -r typesetter.zip typesetter.tar

