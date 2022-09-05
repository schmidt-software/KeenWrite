#!/usr/bin/env bash

# Force clean
podman rmi --all --force

# Build from Containerfile
podman build --tag context .

# Connect
podman run --rm -i -t localhost/context --login

# Export
# podman image save context -o typesetter.tar
# zip -9 -r typesetter.zip typesetter.tar

