#!/bin/sh

jpackage --verbose --input shade/  --name Gitember2 --vendor "Igor Azarny"  --main-jar gitember.jar  --app-version 2.5   --main-class com.az.gitember.GitemberLauncher   --type "dmg"     --icon src/main/resources/icon/gitember.icns
#jpackage --verbose --input build   --name i2Brain                           --main-jar i2brain.jar   --app-version 3.3.2                             --type app-image --runtime-image ./smalljre
mv Gitember2-1.0.dmg Gitember2.dmg


#export PATH="/Users/ec2-user/jdk-16.0.1.jdk/Contents/Home/bin:/Users/ec2-user/apache-maven-3.8.1/bin:$PATH"


