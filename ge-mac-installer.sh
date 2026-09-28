#!/bin/bash

# ─────────────────────────────────────────────────────────────
# Gitember — macOS DMG packager, signer & notarizer
# Fixes FlatLaf dylib notarization issue
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

TOTAL_STEPS=5
CURRENT_STEP=0

APP_VERSION="3.5.2"
BOOT_JAR="gitember-${APP_VERSION}-SNAPSHOT-boot.jar"
DMG_NAME="Gitember-${APP_VERSION}.dmg"

CERT="Developer ID Application: Igor Azarny (3H6449CVS8)"

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

step "Preparing build directories"

rm -rf app3 Gitember.app
mkdir app3

run cp app/${BOOT_JAR} app3/

BOOT_JAR_PATH="$(pwd)/app3/${BOOT_JAR}"

ok "Directories ready"

# ─────────────────────────────────────────────
# Step 3 — Sign the native libraries inside the dependency JARs
# ─────────────────────────────────────────────
#
# Only the entries that actually change are replaced, with `zip`, first inside the
# dependency jar and then inside the fat jar. Exploding the fat jar and rebuilding it with
# `jar cf` cannot be used here: `jar` drops per-entry comments, and Spring Boot keeps its
# "unpack this nested jar at runtime" marker (UNPACK:<sha1>, set by requiresUnpack in
# pom.xml) in exactly such a comment. Lose it and BouncyCastle stays nested in the fat jar,
# where the JVM cannot verify its signature, so every SSH push/pull dies with
# "JCE cannot authenticate the provider BC".

step "Signing native libraries in dependency JARs"

WORK_DIR=$(mktemp -d)

run unzip -q -o "${BOOT_JAR_PATH}" 'BOOT-INF/lib/*.jar' -d "${WORK_DIR}"

sign_natives_in_jar() {
    local jar_path="$1"
    local entry natives_dir native_count

    entry="BOOT-INF/lib/$(basename "$jar_path")"

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
    [ "$native_count" -gt 0 ] || fail "no native binaries extracted from $entry"

    while IFS= read -r -d '' native; do
        echo "    codesign ${native#"$natives_dir"/}"
        codesign \
            --force \
            --options runtime \
            --timestamp \
            --sign "$CERT" \
            "$native" || fail "codesign failed: $native"
    done < <(find "$natives_dir" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) -print0)

    # Put the signed binaries back, replacing only those entries: the rest of the
    # dependency jar (manifest, service files, everything else) stays as shipped.
    (cd "$natives_dir" && find . -type f -print0 | xargs -0 zip -q "$jar_path") \
        || fail "cannot update natives inside $entry"

    # Nested jars must stay uncompressed for the Boot loader, hence -0.
    (cd "$WORK_DIR" && zip -0 -q "${BOOT_JAR_PATH}" "$entry") \
        || fail "cannot update $entry inside ${BOOT_JAR}"

    rm -rf "$natives_dir"
}

for jar in "${WORK_DIR}"/BOOT-INF/lib/*.jar; do
    sign_natives_in_jar "$jar"
done

rm -rf "${WORK_DIR}"

ok "All native libraries signed, fat jar metadata preserved"


# ─────────────────────────────────────────────
# Step 4 — Build app image, verify runtime, wrap DMG
# ─────────────────────────────────────────────

step "Building DMG with jpackage"

JPACKAGE="$JAVA_HOME/bin/jpackage"

run "$JPACKAGE" \
--input app3/ \
--name Gitember \
--vendor "Igor Azarny" \
--main-jar ${BOOT_JAR} \
--app-version ${APP_VERSION} \
--icon src/main/resources/icon/gitember.icns \
--type app-image \
--java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "

RUNTIME_HOME="Gitember.app/Contents/runtime/Contents/Home"
[ -d "$RUNTIME_HOME" ] || fail "jpackage did not produce Gitember.app/Contents/runtime"

HOST_DEPS="$(collect_host_package_manager_deps "$RUNTIME_HOME")"
if [ -n "$HOST_DEPS" ]; then
    echo "$HOST_DEPS"
    fail "Bundled runtime still references Homebrew/MacPorts libraries. Recipients without those packages cannot launch Gitember. Use Oracle JDK or Eclipse Temurin to package."
fi
ok "Bundled runtime has no Homebrew or MacPorts dylib dependencies"

run "$JPACKAGE" \
--type dmg \
--app-image Gitember.app \
--name Gitember \
--app-version ${APP_VERSION} \
--vendor "Igor Azarny" \
--mac-sign \
--mac-package-signing-prefix "com.az.gitember." \
--mac-signing-key-user-name "$CERT"

ok "DMG created: ${DMG_NAME}"


# ─────────────────────────────────────────────
# Step 5 — Notarize & staple
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