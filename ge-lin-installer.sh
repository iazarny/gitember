#!/bin/sh

#rm -rf app
#mkdir app
#cp target/gitember-2.5.8-spring-boot.jar app/gitember.jar

# Oracle JDK 21 only
# Open JDK 17, 18 jlink failed , but app ot modular. 21 packed ok but launcher has error

jpackage --input app/  --name Gitember --app-version 3.0 --vendor "Igor Azarny"  --main-jar gitember-3.0-SNAPSHOT-boot.jar --type "deb"  --icon src/main/resources/icon/gitember-512.png


#mv gitember2_1.0_amd64.deb Gitember2.5.deb