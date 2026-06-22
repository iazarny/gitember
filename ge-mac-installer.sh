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

TOTAL_STEPS=6
CURRENT_STEP=0

APP_VERSION="3.3"
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

rm -rf app2 app3
mkdir app2 app3

run cp app/${BOOT_JAR} app2/

ok "Directories ready"

# ─────────────────────────────────────────────
# Step 2 — Unpack boot jar
# ─────────────────────────────────────────────

step "Unpacking Spring Boot jar"

cd app2
run jar xf ${BOOT_JAR}

ok "Boot jar unpacked"

# ─────────────────────────────────────────────
# Step 3 — Sign all native libraries in dependency JARs
# ─────────────────────────────────────────────

step "Signing native libraries in dependency JARs"

sign_natives_in_jar() {
    local jar_file="$1"
    local abs_jar jar_name tmp_dir
    abs_jar="$(pwd)/$jar_file"
    jar_name=$(basename "$jar_file")

    # Skip jars that contain no macOS native binaries
    if ! jar tf "$jar_file" 2>/dev/null | grep -qE '\.(dylib|jnilib|so)$'; then
        return 0
    fi

    echo "  Signing natives in $jar_name"
    tmp_dir=$(mktemp -d)

    (cd "$tmp_dir" && jar xf "$abs_jar")

    while IFS= read -r -d '' native; do
        echo "    codesign $native"
        codesign \
            --force \
            --options runtime \
            --timestamp \
            --sign "$CERT" \
            "$native" || fail "codesign failed: $native"
    done < <(find "$tmp_dir" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) -print0)

    (cd "$tmp_dir" && jar cf "$abs_jar" .)
    rm -rf "$tmp_dir"
}

for jar in BOOT-INF/lib/*.jar; do
    sign_natives_in_jar "$jar"
done

ok "All native libraries signed"

# ─────────────────────────────────────────────
# Step 4 — Rebuild Spring Boot jar
# ─────────────────────────────────────────────

step "Rebuilding Spring Boot jar"

rm ${BOOT_JAR}

#run jar cfm ${BOOT_JAR} META-INF/MANIFEST.MF .
run jar -cf0m ${BOOT_JAR} META-INF/MANIFEST.MF .

cd ..

run cp app2/${BOOT_JAR} app3/

ok "Boot jar rebuilt with preserved manifest"


# ─────────────────────────────────────────────
# Step 5 — Build DMG
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
--mac-package-signing-prefix "com.az.gitember" \
--mac-signing-key-user-name "$CERT"

ok "DMG created: ${DMG_NAME}"

# ─────────────────────────────────────────────
# Step 6 — Notarize & staple
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