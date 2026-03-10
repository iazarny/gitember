rem mkdir app

rem move target\gitember-3.0-SNAPSHOT-boot.jar app

jpackage --input app/  --name Gitember --app-version 3.0  --vendor "Igor Azarny"  --main-jar gitember-3.0-SNAPSHOT-boot.jar  --win-menu --win-shortcut --type "msi"  --icon src\main\resources\icon\gitember.ico

rem move Gitember2-1.0.msi Gitember2.5.msi