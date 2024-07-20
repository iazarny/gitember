#!/bin/sh

# Oracle JDK 21 only 
# Open JDK 17, 18 jlink failed , but app ot modular. 21 packed ok but launcher has error

jpackage --input app/  --name Gitember2 --vendor "Igor Azarny"  --main-jar gitember.jar   --type "deb"  --icon src/main/resources/icon/gitember.png


mv gitember2_1.0-1_amd64.deb Gitember2.5.deb


