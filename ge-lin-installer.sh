#!/bin/sh


jpackage --input app/  --name Gitember --app-version 3.1 --vendor "Igor Azarny"  --main-jar gitember-3.1-SNAPSHOT-boot.jar --type "deb"  --icon src/main/resources/icon/gitember-512.png

