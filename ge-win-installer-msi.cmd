
jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --win-menu-group Gitember ^
 --install-dir gitember  ^
 --main-jar gitember-3.5.1-SNAPSHOT-boot.jar ^
 --app-version 3.5.1 ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --win-menu ^
 --win-shortcut ^
 --type "msi" ^
 --java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "

