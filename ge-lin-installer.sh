#!/bin/bash

# Builds the .deb with a JVM AOT cache (lib/app/gitember.aot) produced by a training run
# of the bundled runtime. The JVM rejects the cache when a jar's timestamp differs from the
# training run, and jpackage --type deb copies the image with fresh timestamps, so the deb
# is unpacked afterwards, the trained timestamps are restored and it is packed again.
# A rejected cache is not fatal: the app just starts without it.
#
# The training run opens a window: on a machine without a display use `xvfb-run ./ge-lin-installer.sh`.

set -e

APP_VERSION="3.6.0"
APP_JAR="gitember-${APP_VERSION}-SNAPSHOT.jar"
AOT_CACHE="gitember.aot"

# Training and production runs must use the same GC; keep the options identical.
JAVA_OPTS="-XX:+UseSerialGC -Xms16m -Xmx512m -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:TieredStopAtLevel=1 -Xss256k -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m"

# Thin jar + lib/ instead of the boot jar: the AOT cache only covers classes loaded by the
# JVM's built-in class loaders, never those nested inside the Spring Boot fat jar.
[ -f "target/${APP_JAR}" ] || { echo "target/${APP_JAR} not found. Run \`mvn package -DskipTests\` first."; exit 1; }

rm -rf app Gitember deb-out deb-work
mkdir app
cp "target/${APP_JAR}" app/
cp -rp target/lib app/lib

# --- app image; the runtime keeps bin/java for the training run ---
jpackage --input app/ \
   --name Gitember --app-version ${APP_VERSION} --vendor "Igor Azarny" \
   --main-jar ${APP_JAR} \
   --type app-image --icon src/main/resources/icon/gitember-512.png \
   --jlink-options "--strip-debug --no-man-pages --no-header-files" \
   --java-options "${JAVA_OPTS} -XX:AOTCache=\$APPDIR/${AOT_CACHE}"

find Gitember/lib/runtime/bin -type f ! -name java -delete

# --- AOT training: opens this repository, exits by itself, the JVM writes the cache ---
APP_DIR="$(pwd)/Gitember/lib/app"
BUNDLED_JAVA="$(pwd)/Gitember/lib/runtime/bin/java"
TRAIN_HOME=$(mktemp -d)

"$BUNDLED_JAVA" ${JAVA_OPTS} \
   -Duser.home="${TRAIN_HOME}" \
   -XX:AOTCacheOutput="${APP_DIR}/${AOT_CACHE}" \
   -jar "${APP_DIR}/${APP_JAR}" --aot-train "$(pwd)"

rm -rf "${TRAIN_HOME}"

# -XX:AOTMode=on makes the JVM exit with an error instead of silently ignoring the cache.
"$BUNDLED_JAVA" ${JAVA_OPTS} \
   -XX:AOTCache="${APP_DIR}/${AOT_CACHE}" -XX:AOTMode=on \
   -cp "${APP_DIR}/${APP_JAR}" -version >/dev/null 2>&1 \
   || { echo "AOT cache ${AOT_CACHE} is not usable"; exit 1; }

# --- deb ---
jpackage --app-image Gitember --dest deb-out \
   --name Gitember --app-version ${APP_VERSION} --vendor "Igor Azarny" \
   --linux-shortcut \
   --linux-menu-group "Development" \
   --license-file LICENSE-Gnu-3.0.txt \
   --type "deb" --icon src/main/resources/icon/gitember-512.png

# --- restore the trained timestamps of everything under lib/app, repack ---
DEB=$(ls deb-out/*.deb)
dpkg-deb -R "$DEB" deb-work
find deb-work -path '*/lib/app/*' -type f | while read -r f; do
    touch -r "${APP_DIR}/${f#*/lib/app/}" "$f"
done
dpkg-deb --root-owner-group -b deb-work "$DEB"
rm -rf deb-work

echo "Created $DEB"
