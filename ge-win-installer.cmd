mkdir app

move target\gitember-2.5-spring-boot.jar app

jpackage --input app/  --name Gitember2 --vendor "Igor Azarny"  --main-jar gitember-2.5-spring-boot.jar  --win-menu --win-shortcut --type "msi"  --icon src\main\resources\icon\gitember.ico

move Gitember2-1.0.msi Gitember2.5.msi