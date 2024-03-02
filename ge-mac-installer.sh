#!/bin/sh
mkdir app
mv target/gitember-2.5-spring-boot.jar app

jpackage \
   --input app/  --name Gitember2 --vendor "Igor Azarny" \
   --main-jar gitember-2.5-spring-boot.jar  --app-version 2.5 \
   --icon src/main/resources/icon/gitember.icns \
   --type "dmg" \
   --mac-sign \
   --mac-package-signing-prefix "com.az.gitember" \
   --mac-signing-key-user-name "Igor Azarny (3........8)"

mv Gitember2-2.5.dmg Gitember2.5.dmg

# .dmg signing is required for notarization.
codesign --timestamp -s "3H6449CVS8" "Gitember2.5.dmg"

####### notarization #######
xcrun notarytool submit "Gitember2.5.dmg" --wait --keychain-profile "Igor Azarny"
xcrun stapler staple "Gitember2.5.dmg"
spctl -a -t open --context context:primary-signature -vv "Gitember2.5.dmg"

