#!/usr/bin/env bash

awk '{s+=$1} END {print s}' downloads/*-count.txt 2> /dev/null || echo 0

