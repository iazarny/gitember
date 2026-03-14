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

APP_VERSION="3.0"
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
# Step 3 — Patch and sign FlatLaf
# ─────────────────────────────────────────────

step "Signing FlatLaf native libraries"

FLATLAF_JAR=$(find BOOT-INF/lib -name "flatlaf*.jar")

[ -f "$FLATLAF_JAR" ] || fail "FlatLaf jar not found"

mkdir flatlaf_tmp
cd flatlaf_tmp

run jar xf ../${FLATLAF_JAR}

for dylib in $(find . -name "*.dylib"); do
echo "Signing $dylib"
run codesign \
--force \
--options runtime \
--timestamp \
--sign "$CERT" \
"$dylib"
done

run jar cf ../flatlaf-signed.jar .

cd ..

rm "$FLATLAF_JAR"
mv flatlaf-signed.jar "$FLATLAF_JAR"

rm -rf flatlaf_tmp

ok "FlatLaf dylibs signed"

# ─────────────────────────────────────────────
# Step 4 — Rebuild Spring Boot jar
# ─────────────────────────────────────────────

step "Rebuilding Spring Boot jar"

rm ${BOOT_JAR}

run jar cfm ${BOOT_JAR} META-INF/MANIFEST.MF .

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