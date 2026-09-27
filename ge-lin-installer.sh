#!/bin/sh


jpackage --input app/  \
   --name Gitember --app-version 3.5.1 --vendor "Igor Azarny"  \
   --main-jar gitember-3.5.1SNAPSHOT-boot.jar \
   --type "deb"  --icon src/main/resources/icon/gitember-512.png \
   --java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "


