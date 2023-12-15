# Overview

This document describes how to maintain the containerized typesetting system.
The container is built locally then deployed to a web server capable of
serving static web pages.

## Installation wizard

The installation wizard is responsible for installing the containerization
software and the container image. The container manager class loads the
image from a URL. That URL is defined in the `messages.properties` file.

# Upgrade

Upgrade the containerization software (e.g., podman or docker) as follows:

1. Download the latest container version.

    wget -q $(\
      wget \
      -q -O- \
      https://api.github.com/repos/containers/podman/releases/latest | \
      jq \
      -r '.assets[] | select(.name | contains("exe")) | .browser_download_url')

1. Compute the SHA:

    sha256sum *exe | cut -f1 -d' '

1. Edit `src/main/resources/com/keenwrite/messages.properties`.
1. Set `Wizard.typesetter.container.version` to the latest version.
1. Set `Wizard.typesetter.container.checksum` to the Windows version checksum.
1. Set `Wizard.typesetter.container.image.version` to the latest image version.
1. Save the file.

The containerization software version is changed.

# Publish

Publish the changes to the container image as follows:

``` bash
./manage.sh --build
./manage.sh --export
./manage.sh --publish
```

The container image is published.

