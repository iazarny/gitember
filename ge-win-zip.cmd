@echo off
setlocal

set VERSION=3.3
set JAR=gitember-3.3-SNAPSHOT-boot.jar

rem --- clean previous output ---
if exist Gitember rmdir /s /q Gitember
if exist Gitember-%VERSION%.zip del /q Gitember-%VERSION%.zip

rem --- stage the runnable boot jar for jpackage ---
if exist app rmdir /s /q app
mkdir app
copy /y target\%JAR% app\ >nul
if errorlevel 1 (
  echo Boot jar target\%JAR% not found. Run "mvn package -DskipTests" first.
  exit /b 1
)

rem --- build the portable app-image (bundles a JRE, no install required) ---
jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar %JAR% ^
 --main-class org.springframework.boot.loader.launch.JarLauncher ^
 --app-version %VERSION% ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico
if errorlevel 1 (
  echo jpackage failed.
  exit /b 1
)

rem --- compress the app-image into a portable zip ---
powershell -NoProfile -Command "Compress-Archive -Path 'Gitember' -DestinationPath 'Gitember-%VERSION%.zip' -Force"
if errorlevel 1 (
  echo Compress-Archive failed.
  exit /b 1
)

echo Created Gitember-%VERSION%.zip
endlocal