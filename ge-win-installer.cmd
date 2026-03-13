rem mkdir app

rem move target\gitember-3.0-SNAPSHOT-boot.jar app

rem jpackage --input app/  --name Gitember --win-menu-group Gitember --install-dir gitember --app-version 3.0  --vendor "Igor Azarny"  --main-jar gitember-3.0-SNAPSHOT-boot.jar  --win-menu --win-shortcut --type "msi"  --icon src\main\resources\icon\gitember.ico


jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar gitember-3.0-SNAPSHOT-boot.jar ^
 --app-version 3.0 ^
 --vendor "Igor Azarny" ^
 --runtime-image runtime ^
 --icon src\main\resources\icon\gitember.ico ^
 --win-menu ^
 --win-shortcut
 
rem move Gitember2-1.0.msi Gitember2.5.msi