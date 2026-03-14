#!/bin/bash

# ─────────────────────────────────────────────────────────────
#  Gitember — macOS DMG packager, signer & notarizer
# ─────────────────────────────────────────────────────────────

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
RESET='\033[0m'

TOTAL_STEPS=4
CURRENT_STEP=0

APP_VERSION="3.0"
DMG_NAME="Gitember-${APP_VERSION}.dmg"

print_banner() {
    echo ""
    echo -e "${CYAN}${BOLD}  ╔══════════════════════════════════════════╗${RESET}"
    echo -e "${CYAN}${BOLD}  ║        Gitember macOS Packager           ║${RESET}"
    echo -e "${CYAN}${BOLD}  ║        v${APP_VERSION}  •  DMG + Notarize          ║${RESET}"
    echo -e "${CYAN}${BOLD}  ╚══════════════════════════════════════════╝${RESET}"
    echo ""
}

step() {
    CURRENT_STEP=$((CURRENT_STEP + 1))
    echo ""
    echo -e "${CYAN}${BOLD}  ┌─ Step ${CURRENT_STEP}/${TOTAL_STEPS} ─────────────────────────────────${RESET}"
    echo -e "${CYAN}${BOLD}  │  $1${RESET}"
    echo -e "${CYAN}${BOLD}  └────────────────────────────────────────────${RESET}"
    echo ""
}

ok() {
    echo -e "  ${GREEN}✔${RESET}  $1"
}

fail() {
    echo ""
    echo -e "  ${RED}✘  ERROR: $1${RESET}"
    echo ""
    exit 1
}

run() {
    echo -e "  ${DIM}» $*${RESET}"
    "$@" || fail "Command failed: $*"
}

print_banner

# ── Step 1: jpackage ─────────────────────────────────────────
step "Building DMG with jpackage"

run jpackage \
    --input app/ \
    --name Gitember \
    --vendor "Igor Azarny" \
    --main-jar gitember-${APP_VERSION}-SNAPSHOT-boot.jar \
    --app-version ${APP_VERSION} \
    --icon src/main/resources/icon/gitember.icns \
    --type dmg \
    --mac-sign \
    --mac-package-signing-prefix "com.az.gitember" \
    --mac-signing-key-user-name "Igor Azarny (3H6449CVS8)"

ok "DMG created: ${DMG_NAME}"

# ── Step 2: codesign ─────────────────────────────────────────
step "Code-signing the DMG"

#fnsa-vmtk-sddd-poul
run codesign --timestamp \
    --sign D5D8A01CF3AA50AB7099527B727E97918FBADB6D \
    "${DMG_NAME}"

ok "Signed: ${DMG_NAME}"

# ── Step 3: notarize ─────────────────────────────────────────
step "Submitting to Apple Notary Service"

run xcrun notarytool submit "${DMG_NAME}" \
    --wait \
    --keychain-profile "Igor Azarny"

xcrun notarytool log f49e81b2-a40b-447d-8421-19cc8bcf8417 --keychain-profile "Igor Azarny"

ok "Notarization complete"

# ── Step 4: staple & verify ───────────────────────────────────
step "Stapling ticket & verifying Gatekeeper"

run xcrun stapler staple "${DMG_NAME}"
ok "Ticket stapled"

run spctl -a -t open --context context:primary-signature -vv "${DMG_NAME}"
ok "Gatekeeper check passed"

# ── Done ──────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}${BOLD}  ══════════════════════════════════════════════${RESET}"
echo -e "${GREEN}${BOLD}    All ${TOTAL_STEPS} steps completed successfully!${RESET}"
echo -e "${GREEN}${BOLD}    ${DMG_NAME} is ready to distribute.${RESET}"
echo -e "${GREEN}${BOLD}  ══════════════════════════════════════════════${RESET}"
echo ""
