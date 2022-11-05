#!/usr/bin/env bash

if [ -z ${IMAGES_DIR} ]; then
  echo "Set IMAGES_DIR"
  exit 10
fi

readonly CONTAINER_NAME=typesetter

# Force clean
podman rmi --all --force

# Build from Containerfile
podman build --tag ${CONTAINER_NAME} .

# Connect and mount images
podman run \
  --rm \
  -i \
  -v ${IMAGES_DIR}:/root/images:ro \
  -t ${CONTAINER_NAME} \
  /bin/sh --login -c 'context --version'

# Create a persistent container
# podman create typesetter typesetter

# Create a long-running task
# podman create -ti typesetter /bin/sh

# Connect

# Export
# podman image save context -o typesetter.tar
# zip -9 -r typesetter.zip typesetter.tar

