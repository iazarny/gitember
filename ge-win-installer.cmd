
rmdir /s /q Gitember

del Gitember-3.2.msix

jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar gitember-3.1-SNAPSHOT-boot.jar ^
 --app-version 3.2 ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico 

copy inst\AppxManifest.xml Gitember

mkdir Gitember\inst

copy inst\*.png Gitember\inst

makeappx pack ^
  /d Gitember ^
  /p Gitember-3.2.msix
 
