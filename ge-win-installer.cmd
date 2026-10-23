
set APP_VERSION=3.5.1
set BOOT_JAR=gitember-%APP_VERSION%-SNAPSHOT-boot.jar
set MSIX_NAME=Gitember-%APP_VERSION%.msix

rmdir /s /q Gitember

del %MSIX_NAME%

jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar %BOOT_JAR% ^
 --app-version %APP_VERSION% ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "



mkdir Gitember
mkdir Gitember\inst


copy inst\AppxManifest.xml Gitember


copy inst\*.png Gitember\inst

makeappx pack ^
  /d Gitember ^
  /p %MSIX_NAME%
 
