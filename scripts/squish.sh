#!/usr/bin/env bash

# TODO: This file does not work with Picocli and there are other issues.
# TODO: Revisit after replacing Picocli and using FastR instead of Renjin.

MODULES="${JAVA_HOME}/jmods/"
LIBS=$(ls -1 ../libs/*jar | sed 's/\(.*\)/-libraryjars \1/g')

java -jar ../tex/lib/proguard.jar \
  -libraryjars "${MODULES}java.base.jmod/(!**.jar;!module-info.class)" \
  -libraryjars "${MODULES}java.desktop.jmod/(!**.jar;!module-info.class)" \
  -libraryjars "${MODULES}java.xml.jmod/(!**.jar;!module-info.class)" \
  -libraryjars "${MODULES}javafx.controls.jmod/(!**.jar;!module-info.class)" \
  -libraryjars "${MODULES}javafx.graphics.jmod/(!**.jar;!module-info.class)" \
  ${LIBS} \
  -injars ../build/libs/keenwrite.jar \
  -outjars ../build/libs/keenwrite-min.jar \
  -keep 'class com.keenwrite.** { *; }' \
  -keep 'class com.whitemagicsoftware.tex.** { *; }' \
  -keep 'class org.renjin.** { *; }' \
  -keep 'class picocli.** { *; }' \
  -keep 'interface picocli.** { *; }' \
  -keep 'class picocli.CommandLine { *; }' \
  -keep 'class picocli.CommandLine$* { *; }' \
  -keepattributes '*Annotation*, Signature, Exception' \
  -keepclassmembers 'class * extends java.util.concurrent.Callable {
      public java.lang.Integer call();
  }' \
  -keepclassmembers 'class * {
      @javax.inject.Inject <init>(...);
      @picocli.CommandLine$Option *;
  }' \
  -keepclassmembers 'class * extends java.lang.Enum {
      <fields>;
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }' \
  -keepnames \
    'class org.apache.lucene.analysis.tokenattributes.KeywordAttributeImpl' \
  -dontnote \
  -dontwarn \
  -dontoptimize \
  -dontobfuscate

