@echo off
setlocal

set APP_VERSION=3.6.0
set APP_JAR=gitember-%APP_VERSION%-SNAPSHOT.jar

rem JVM AOT cache: the launcher points at $LOCALAPPDATA\Gitember\gitember.aot ($NAME is expanded
rem from the environment by the jpackage launcher at runtime, not by cmd).
rem Gitember builds that cache itself in the background on first start and after an upgrade
rem (see AotTraining.java): Windows installers and zip extraction do not keep the jar timestamps
rem a prebuilt cache is tied to. Needs jpackage from JDK 27+ for the variable expansion.
set JAVA_OPTS=-XX:+UseSerialGC -Xms16m -Xmx512m -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:TieredStopAtLevel=1 -Xss256k -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m
set AOT_OPTS=-XX:AOTCache=$LOCALAPPDATA\Gitember\gitember.aot -Dgitember.aot.autotrain=true

rem --- clean previous output ---
if exist Gitember rmdir /s /q Gitember
if exist Gitember-%APP_VERSION%.zip del /q Gitember-%APP_VERSION%.zip

rem --- stage the thin jar + lib\ for jpackage ---
rem The AOT cache only covers classes from the JVM's built-in class loaders, never those
rem nested inside the Spring Boot fat jar.
if exist app rmdir /s /q app
mkdir app
copy /y target\%APP_JAR% app\ >nul
if errorlevel 1 (
  echo Jar target\%APP_JAR% not found. Run "mvn package -DskipTests" first.
  exit /b 1
)
xcopy /e /i /q /y target\lib app\lib >nul
if errorlevel 1 (
  echo target\lib not found. Run "mvn package -DskipTests" first.
  exit /b 1
)

rem --- build the portable app-image (bundles a JRE, no install required) ---
rem The runtime keeps javaw.exe: the background AOT training run is started with it.
jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar %APP_JAR% ^
 --main-class com.az.gitember.App ^
 --app-version %APP_VERSION% ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --jlink-options "--strip-debug --no-man-pages --no-header-files" ^
 --java-options "%JAVA_OPTS% %AOT_OPTS%"

if errorlevel 1 (
  echo jpackage failed.
  exit /b 1
)

rem Only java.exe / javaw.exe are needed; the DLLs in runtime\bin stay.
for %%f in (Gitember\runtime\bin\*.exe) do (
  if /i not "%%~nxf"=="java.exe" if /i not "%%~nxf"=="javaw.exe" del /q "%%f"
)

rem --- compress the app-image into a portable zip ---
powershell -NoProfile -Command "Compress-Archive -Path 'Gitember' -DestinationPath 'Gitember-%APP_VERSION%.zip' -Force"
if errorlevel 1 (
  echo Compress-Archive failed.
  exit /b 1
)

echo Created Gitember-%APP_VERSION%.zip
endlocal
