#!/bin/sh
mkdir app
mv target/gitember-2.5.5-spring-boot.jar app

jpackage \
   --input app/  --name Gitember2 --vendor "Igor Azarny" \
   --main-jar gitember-2.5.5-spring-boot.jar  --app-version 2.5 \
   --icon src/main/resources/icon/gitember.icns \
   --type "dmg" \
   --mac-sign \
   --mac-package-signing-prefix "com.az.gitember" \
   --mac-signing-key-user-name "Igor Azarny (3H6449CVS8)"

mv Gitember2-2.5.dmg Gitember2.5.dmg

# .dmg signing is required for notarization.
#codesign --timestamp -s "Igor Azarny (3H6449CVS8)" "Gitember2.5.dmg"
#codesign -dv --verbose=4 Gitember2.5.dmg
#security find-identity -v -p codesigning
codesign --timestamp --sign D5D8A01CF3AA50AB7099527B727E97918FBADB6D  "Gitember2.5.dmg"
#codesign --timestamp -s "Developer ID Application: Igor Azarny (3H6449CVS8)" "Gitember2.5.dmg"

####### notarization #######
xcrun notarytool submit "Gitember2.5.dmg" --wait --keychain-profile "Igor Azarny"
xcrun stapler staple "Gitember2.5.dmg"
spctl -a -t open --context context:primary-signature -vv "Gitember2.5.dmg"

