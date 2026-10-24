
set APP_VERSION=3.6.0
set APP_JAR=gitember-%APP_VERSION%-SNAPSHOT.jar
set MSIX_NAME=Gitember-%APP_VERSION%.msix

rem JVM AOT cache: built by Gitember itself in the background on first start, at
rem $LOCALAPPDATA\Gitember\gitember.aot (expanded by the jpackage launcher, JDK 27+).
rem See ge-win-zip.cmd and AotTraining.java.
set JAVA_OPTS=-XX:+UseSerialGC -Xms16m -Xmx512m -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:TieredStopAtLevel=1 -Xss256k -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m
set AOT_OPTS=-XX:AOTCache=$LOCALAPPDATA\Gitember\gitember.aot -Dgitember.aot.autotrain=true

rmdir /s /q Gitember

del %MSIX_NAME%

rem thin jar + lib\ instead of the boot jar, see ge-win-zip.cmd
if exist app rmdir /s /q app
mkdir app
copy /y target\%APP_JAR% app\
xcopy /e /i /q /y target\lib app\lib

jpackage ^
 --type app-image ^
 --input app ^
 --name Gitember ^
 --main-jar %APP_JAR% ^
 --app-version %APP_VERSION% ^
 --vendor "Igor Azarny" ^
 --icon src\main\resources\icon\gitember.ico ^
 --jlink-options "--strip-debug --no-man-pages --no-header-files" ^
 --java-options "%JAVA_OPTS% %AOT_OPTS%"

rem keep java.exe / javaw.exe for the background AOT training run
for %%f in (Gitember\runtime\bin\*.exe) do (
  if /i not "%%~nxf"=="java.exe" if /i not "%%~nxf"=="javaw.exe" del /q "%%f"
)


mkdir Gitember
mkdir Gitember\inst


copy inst\AppxManifest.xml Gitember


copy inst\*.png Gitember\inst

makeappx pack ^
  /d Gitember ^
  /p %MSIX_NAME%
 
