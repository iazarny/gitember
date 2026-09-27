#!/bin/bash

# ─────────────────────────────────────────────────────────────
# Gitember — macOS DMG packager, signer & notarizer
# Fixes FlatLaf dylib notarization issue
# ─────────────────────────────────────────────────────────────

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
RESET='\033[0m'

TOTAL_STEPS=4
CURRENT_STEP=0

APP_VERSION="3.5"
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

print_banner

# ─────────────────────────────────────────────
# Step 1 — Prepare directories
# ─────────────────────────────────────────────

step "Preparing build directories"

rm -rf app3
mkdir app3

run cp app/${BOOT_JAR} app3/

BOOT_JAR_PATH="$(pwd)/app3/${BOOT_JAR}"

ok "Directories ready"

# ─────────────────────────────────────────────
# Step 2 — Sign the native libraries inside the dependency JARs
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
# Step 3 — Build DMG
# ─────────────────────────────────────────────

step "Building DMG with jpackage"

run jpackage \
--input app3/ \
--name Gitember \
--vendor "Igor Azarny" \
--main-jar ${BOOT_JAR} \
--app-version ${APP_VERSION} \
--icon src/main/resources/icon/gitember.icns \
--type dmg \
--mac-sign \
--mac-package-signing-prefix "com.az.gitember." \
--mac-signing-key-user-name "$CERT" \
--java-options "-XX:+UseSerialGC   -Xms16m  -Xmx512m   -XX:MinHeapFreeRatio=10   -XX:MaxHeapFreeRatio=20  -XX:TieredStopAtLevel=1 -Xss256k   -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=64m "

ok "DMG created: ${DMG_NAME}"

exit

# ─────────────────────────────────────────────
# Step 4 — Notarize & staple
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