#!/bin/sh

APP_VERSION="3.5.1"
BOOT_JAR="gitember-${APP_VERSION}-SNAPSHOT-boot.jar"

jpackage --input app/  \
   --name Gitember --app-version ${APP_VERSION} --vendor "Igor Azarny"  \
   --main-jar ${BOOT_JAR} \
   --linux-shortcut \
   --linux-menu-group "Development" \
   --license-file LICENSE-Gnu-3.0.txt \
   --type "deb"  --icon src/main/resources/icon/gitember-512.png \
   --java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "

