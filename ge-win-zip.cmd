@echo off
setlocal

set APP_VERSION=3.5.1
set BOOT_JAR=gitember-%APP_VERSION%-SNAPSHOT-boot.jar

rem --- clean previous output ---
if exist Gitember rmdir /s /q Gitember
if exist Gitember-%APP_VERSION%.zip del /q Gitember-%APP_VERSION%.zip

rem --- stage the runnable boot jar for jpackage ---
if exist app rmdir /s /q app
mkdir app
copy /y target\%BOOT_JAR% app\ >nul
if errorlevel 1 (
  echo Boot jar target\%BOOT_JAR% not found. Run "mvn package -DskipTests" first.
  exit /b 1
)

rem --- build the portable app-image (bundles a JRE, no install required) ---
jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar %BOOT_JAR% ^
 --main-class org.springframework.boot.loader.launch.JarLauncher ^
 --app-version %APP_VERSION% ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "

if errorlevel 1 (
  echo jpackage failed.
  exit /b 1
)

rem --- compress the app-image into a portable zip ---
powershell -NoProfile -Command "Compress-Archive -Path 'Gitember' -DestinationPath 'Gitember-%APP_VERSION%.zip' -Force"
if errorlevel 1 (
  echo Compress-Archive failed.
  exit /b 1
)

echo Created Gitember-%APP_VERSION%.zip
endlocal