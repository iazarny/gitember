
rmdir /s /q Gitember

del Gitember-3.2.msix

jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar gitember-3.2-SNAPSHOT-boot.jar ^
 --app-version 3.2 ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico 

rem jpackage --type app-image --input app --name Gitember  --main-jar gitember-3.2-SNAPSHOT-boot.jar --app-version 3.2  --vendor "Igor Azarny"  --icon src\main\resources\icon\gitember.ico 

mkdir Gitember
mkdir Gitember\inst


copy inst\AppxManifest.xml Gitember


copy inst\*.png Gitember\inst

makeappx pack ^
  /d Gitember ^
  /p Gitember-3.2.msix
 
