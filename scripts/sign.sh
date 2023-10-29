#!/usr/bin/env bash

osslsigncode sign \
  -pkcs12 code-sign-cert.pfx \
  -askpass \
  -n "KeenWrite" \
  -i https://www.keenwrite.com \
  -in KeenWrite.exe \
  -out KeenWrite-signed.exe

