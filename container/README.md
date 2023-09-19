# Overview

This document describes how to maintain the containerized typesetting system.
Broadly, the container is built locally then deployed to a web server capable
of serving static web pages.

## Installation wizard

The installation wizard is responsible for installing the containerization
software and the container image. The container manager class loads the
image from a URL. That URL is defined in the `messages.properties` file.

# Upgrade

Upgrade the containerization software as follows:

1. Edit `src/main/resources/com/keenwrite/messages.properties`.
1. Set `Wizard.typesetter.container.version` to the latest version.
1. Set `Wizard.typesetter.container.checksum` to the Windows version checksum.
1. Set `Wizard.typesetter.container.image.version` to the latest image version.
1. Save the file.

The containerization software versions are changed.

# Publish

Publish the changes to the container image as follows:

``` bash
./manage.sh --build
./manage.sh --export
./manage.sh --publish
```

The container image is published.

