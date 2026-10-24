#!/bin/bash

# ─────────────────────────────────────────────────────────────
# Gitember — macOS DMG packager, signer & notarizer
# Fixes FlatLaf dylib notarization issue
#
# Ships a JVM AOT cache (Contents/app/gitember.aot), produced by a
# training run of the bundled runtime: JEP 483/514/515 class loading,
# linking and profiles, plus AOT code (JEP 544) once the runtime is
# JDK 28+. The JVM rejects the cache when a jar's timestamp differs from
# the training run, so the order matters: unsigned app image -> training
# -> in-place signing -> DMG via hdiutil. jpackage --type dmg copies the
# image and resets jar timestamps, which would invalidate the cache.
# A rejected cache is not fatal: the app just starts without it.
#
# Homebrew OpenJDK cannot be used here. That build links
# libfontmanager.dylib (and other natives) to bottles under
# /opt/homebrew/opt — harfbuzz, freetype, giflib, jpeg-turbo,
# libpng, little-cms2. jpackage copies those dylibs as-is, so
# the DMG fails to launch on a Mac that does not have those
# Homebrew packages: UnsatisfiedLinkError / Failed to launch JVM.
# Use Oracle, Temurin, Corretto, or Zulu instead.
# ─────────────────────────────────────────────────────────────

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
RESET='\033[0m'

TOTAL_STEPS=7
CURRENT_STEP=0

APP_VERSION="3.6.0"
APP_JAR="gitember-${APP_VERSION}-SNAPSHOT.jar"
AOT_CACHE="gitember.aot"
DMG_NAME="Gitember-${APP_VERSION}.dmg"

CERT="Developer ID Application: Igor Azarny (3H6449CVS8)"

# Training and production runs must use the same GC; keep the options identical.
JAVA_OPTS="-XX:+UseSerialGC -Xms16m -Xmx512m -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:TieredStopAtLevel=1 -Xss256k -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m"

print_banner() {
echo ""
echo -e "${CYAN}${BOLD}  ╔══════════════════════════════════════════╗${RESET}"
echo -e "${CYAN}${BOLD}  ║        Gitember macOS Packager           ║${RESET}"
echo -e "${CYAN}${BOLD}  ║        v${APP_VERSION} • DMG + Notarize  ║${RESET}"
echo -e "${CYAN}${BOLD}  ╚══════════════════════════════════════════╝${RESET}"
echo ""
}

step() {
CURRENT_STEP=$((CURRENT_STEP + 1))
echo ""
echo -e "${CYAN}${BOLD}  ┌─ Step ${CURRENT_STEP}/${TOTAL_STEPS} ─────────────────────────────${RESET}"
echo -e "${CYAN}${BOLD}  │  $1${RESET}"
echo -e "${CYAN}${BOLD}  └────────────────────────────────────────────${RESET}"
echo ""
}

ok(){ echo -e "  ${GREEN}✔${RESET} $1"; }

fail(){
echo ""
echo -e "  ${RED}✘ ERROR: $1${RESET}"
echo ""
exit 1
}

run(){
echo -e "  ${DIM}» $*${RESET}"
"$@" || fail "Command failed: $*"
}

# Install names that live on the packaging Mac (Homebrew / MacPorts) rather than
# inside the JDK or the app bundle. Recipients will not have these files.
is_host_package_manager_path() {
    local path="$1"
    case "$path" in
        /opt/homebrew/*|/usr/local/opt/*|/usr/local/Cellar/*|/opt/local/*) return 0 ;;
        *) return 1 ;;
    esac
}

# Prints "dylib -> install_name" for every host package-manager dependency under root.
collect_host_package_manager_deps() {
    local root="$1"
    local dylib dep
    find "$root" -name '*.dylib' -print0 2>/dev/null | while IFS= read -r -d '' dylib; do
        otool -L "$dylib" 2>/dev/null | awk 'NR>1 {print $1}' | while read -r dep; do
            if is_host_package_manager_path "$dep"; then
                case "$dep" in
                    "$root"/*) ;;
                    *) printf '    %s\n      -> %s\n' "${dylib#"$root"/}" "$dep" ;;
                esac
            fi
        done
    done
}

jdk_is_distributable() {
    local home="$1"
    local deps
    [ -x "$home/bin/jpackage" ] || return 1
    [ -f "$home/lib/libfontmanager.dylib" ] || return 1
    deps="$(collect_host_package_manager_deps "$home")"
    [ -z "$deps" ]
}

# Prefer a JDK whose natives use @rpath (Oracle / Temurin / Corretto / Zulu).
# Homebrew OpenJDK is skipped: its libfontmanager.dylib points at
# /opt/homebrew/opt/harfbuzz and /opt/homebrew/opt/freetype.
select_packaging_jdk() {
    local home vendor
    local -a candidates=()

    while IFS= read -r home; do
        [ -n "$home" ] || continue
        candidates+=("$home")
    done < <(
        if [ -n "${JAVA_HOME:-}" ]; then
            printf '%s\n' "$JAVA_HOME"
        fi
        if [ -x /usr/libexec/java_home ]; then
            /usr/libexec/java_home -V 2>&1 | awk '/^[[:space:]]*[0-9]/ {print $NF}'
        fi
        ls -d /Library/Java/JavaVirtualMachines/*/Contents/Home 2>/dev/null || true
    )

    for home in "${candidates[@]}"; do
        case "$home" in
            /opt/homebrew/*|/usr/local/Cellar/*)
                echo -e "  ${DIM}Skipping Homebrew JDK: $home${RESET}"
                continue
                ;;
        esac
        if jdk_is_distributable "$home"; then
            vendor="$("$home/bin/java" -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.vendor / {print $2; exit}')"
            export JAVA_HOME="$home"
            export PATH="$JAVA_HOME/bin:$PATH"
            ok "Packaging JDK: $JAVA_HOME (${vendor:-unknown vendor})"
            return 0
        fi
        echo -e "  ${DIM}Skipping JDK with host-library deps: $home${RESET}"
    done

    fail "No distributable JDK found. Install Oracle JDK or Eclipse Temurin (not \`brew install openjdk\`) and re-run. Homebrew OpenJDK links libfontmanager.dylib to /opt/homebrew/opt/harfbuzz and /opt/homebrew/opt/freetype, which are not shipped in the DMG."
}

print_banner

# ─────────────────────────────────────────────
# Step 1 — Select a JDK that can be shipped
# ─────────────────────────────────────────────

step "Selecting a distributable packaging JDK"

select_packaging_jdk
run java -version

# ─────────────────────────────────────────────
# Step 2 — Prepare directories
# ─────────────────────────────────────────────
#
# The thin jar + lib/ (see maven-jar-plugin / maven-dependency-plugin in pom.xml) is
# shipped instead of the Spring Boot fat jar: the AOT cache only covers classes loaded
# by the JVM's built-in class loaders, never those nested inside the fat jar. As a plain
# jar on the class path BouncyCastle also keeps its JCE signature without any unpacking.

step "Preparing build directories"

[ -f "target/${APP_JAR}" ] || fail "target/${APP_JAR} not found. Run \`mvn package -DskipTests\` first."
[ -d target/lib ] || fail "target/lib not found. Run \`mvn package -DskipTests\` first."

rm -rf app3 Gitember.app "${DMG_NAME}"
mkdir app3

run cp "target/${APP_JAR}" app3/
run cp -R target/lib app3/lib

ok "Directories ready"

# ─────────────────────────────────────────────
# Step 3 — Sign the native libraries inside the dependency JARs
# ─────────────────────────────────────────────
#
# Only the entries that actually change are replaced inside each jar, with `zip`;
# the rest of the jar (manifest, service files, everything else) stays as shipped.

step "Signing native libraries in dependency JARs"

sign_natives_in_jar() {
    local jar_path="$1"
    local natives_dir native_count

    # Skip jars that contain no macOS native binaries.
    if ! unzip -Z1 "$jar_path" 2>/dev/null | grep -qE '\.(dylib|jnilib|so)$'; then
        return 0
    fi

    echo "  Signing natives in $(basename "$jar_path")"
    natives_dir=$(mktemp -d)

    # unzip warns (and reports failure) for patterns that match nothing, which is expected
    # when a jar ships only .dylib or only .so -- the extracted count is what matters.
    (cd "$natives_dir" && unzip -q "$jar_path" '*.dylib' '*.jnilib' '*.so' >/dev/null 2>&1 || true)
    native_count=$(find "$natives_dir" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) | wc -l | tr -d ' ')
    [ "$native_count" -gt 0 ] || fail "no native binaries extracted from $(basename "$jar_path")"

    while IFS= read -r -d '' native; do
        echo "    codesign ${native#"$natives_dir"/}"
        codesign \
            --force \
            --options runtime \
            --timestamp \
            --sign "$CERT" \
            "$native" || fail "codesign failed: $native"
    done < <(find "$natives_dir" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) -print0)

    (cd "$natives_dir" && find . -type f -print0 | xargs -0 zip -q "$jar_path") \
        || fail "cannot update natives inside $(basename "$jar_path")"

    rm -rf "$natives_dir"
}

for jar in "$(pwd)"/app3/lib/*.jar; do
    sign_natives_in_jar "$jar"
done

ok "All native libraries signed"

# ─────────────────────────────────────────────
# Step 4 — Build the (unsigned) app image
# ─────────────────────────────────────────────
#
# The runtime keeps bin/java (jpackage strips all native commands by default): the AOT
# training run in the next step must use exactly the runtime that ships.

step "Building app image with jpackage"

JPACKAGE="$JAVA_HOME/bin/jpackage"

run "$JPACKAGE" \
--input app3/ \
--name Gitember \
--vendor "Igor Azarny" \
--main-jar ${APP_JAR} \
--app-version ${APP_VERSION} \
--icon src/main/resources/icon/gitember.icns \
--type app-image \
--jlink-options "--strip-debug --no-man-pages --no-header-files" \
--java-options "${JAVA_OPTS} -XX:AOTCache=\$APPDIR/${AOT_CACHE}"

RUNTIME_HOME="Gitember.app/Contents/runtime/Contents/Home"
[ -d "$RUNTIME_HOME" ] || fail "jpackage did not produce Gitember.app/Contents/runtime"

# Only bin/java is needed, for the training run.
find "$RUNTIME_HOME/bin" -type f ! -name java -delete

HOST_DEPS="$(collect_host_package_manager_deps "$RUNTIME_HOME")"
if [ -n "$HOST_DEPS" ]; then
    echo "$HOST_DEPS"
    fail "Bundled runtime still references Homebrew/MacPorts libraries. Recipients without those packages cannot launch Gitember. Use Oracle JDK or Eclipse Temurin to package."
fi
ok "Bundled runtime has no Homebrew or MacPorts dylib dependencies"

# ─────────────────────────────────────────────
# Step 5 — AOT training run
# ─────────────────────────────────────────────
#
# Starts Gitember from the app image with -XX:AOTCacheOutput: the app opens this
# repository, closes itself after a few seconds (App --aot-train) and the JVM writes the
# cache. Same command line shape as the launcher (-jar <app>/<jar>), same options.
# A throw-away user.home keeps the packaging user's settings and recent list untouched.

step "Training the AOT cache"

APP_DIR="$(pwd)/Gitember.app/Contents/app"
BUNDLED_JAVA="$(pwd)/${RUNTIME_HOME}/bin/java"
TRAIN_HOME=$(mktemp -d)

run "$BUNDLED_JAVA" ${JAVA_OPTS} \
    -Duser.home="${TRAIN_HOME}" \
    -XX:AOTCacheOutput="${APP_DIR}/${AOT_CACHE}" \
    -jar "${APP_DIR}/${APP_JAR}" --aot-train "$(pwd)"

rm -rf "${TRAIN_HOME}"

# -XX:AOTMode=on makes the JVM exit with an error instead of silently ignoring the cache.
verify_aot_cache() {
    "$BUNDLED_JAVA" ${JAVA_OPTS} \
        -XX:AOTCache="${APP_DIR}/${AOT_CACHE}" -XX:AOTMode=on \
        -cp "${APP_DIR}/${APP_JAR}" -version >/dev/null 2>&1 \
        || fail "AOT cache ${AOT_CACHE} is not usable ($1)"
}

verify_aot_cache "after training"
ok "AOT cache created: $(du -h "${APP_DIR}/${AOT_CACHE}" | cut -f1)"

# ─────────────────────────────────────────────
# Step 6 — Sign app image in place, wrap DMG
# ─────────────────────────────────────────────

step "Signing app image and building DMG"

run "$JPACKAGE" \
--type app-image \
--app-image Gitember.app \
--mac-sign \
--mac-package-signing-prefix "com.az.gitember." \
--mac-signing-key-user-name "$CERT"

run codesign --verify --deep --strict Gitember.app
verify_aot_cache "after signing"
ok "App image signed, AOT cache still valid"

DMG_ROOT=$(mktemp -d)
run ditto Gitember.app "${DMG_ROOT}/Gitember.app"
ln -s /Applications "${DMG_ROOT}/Applications"

run hdiutil create \
-volname Gitember \
-srcfolder "${DMG_ROOT}" \
-ov \
-format UDZO \
"${DMG_NAME}"

rm -rf "${DMG_ROOT}"

ok "DMG created: ${DMG_NAME}"


# ─────────────────────────────────────────────
# Step 7 — Notarize & staple
# ─────────────────────────────────────────────

step "Signing, notarizing and stapling"

run codesign --timestamp --sign "$CERT" "${DMG_NAME}"

run xcrun notarytool submit "${DMG_NAME}" \
--wait \
--keychain-profile "Igor Azarny"

run xcrun stapler staple "${DMG_NAME}"

run spctl -a -t open --context context:primary-signature -vv "${DMG_NAME}"

ok "Gatekeeper validation passed"

echo ""
echo -e "${GREEN}${BOLD}══════════════════════════════════════════════${RESET}"
echo -e "${GREEN}${BOLD}   ${DMG_NAME} is ready for distribution${RESET}"
echo -e "${GREEN}${BOLD}══════════════════════════════════════════════${RESET}"
echo ""