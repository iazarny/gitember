
jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --win-menu-group Gitember ^
 --install-dir gitember  ^
 --main-jar gitember-3.0-SNAPSHOT-boot.jar ^
 --app-version 3.0 ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --win-menu ^
 --win-shortcut ^
 --type "msi"
