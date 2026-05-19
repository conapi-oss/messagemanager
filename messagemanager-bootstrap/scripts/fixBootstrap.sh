#!/bin/bash
#
# fixBootstrap.sh — re-download messagemanager-bootstrap.jar from files.conapi.at,
# bypassing Update4J entirely.
#
# Use this when an in-place self-update of the bootstrap is blocked — typically
# because Windows file locking prevents Update4J from overwriting the running
# bootstrap jar, or because the running bootstrap has an upstream bug
# (e.g. a too-short HTTP read timeout) that aborts its own download attempts.
#
# Run with Message Manager NOT RUNNING.

set -e

# This script lives in <MM_HOME>/bin/; derive MM_HOME from its own location.
MM_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# Source setenv.sh if it sets UPDATE_URL (matches launch.sh)
if [ -f "$MM_HOME/bin/setenv.sh" ]; then
    . "$MM_HOME/bin/setenv.sh"
fi

UPDATE_URL="${UPDATE_URL:-https://files.conapi.at/mm/stable/setup.xml}"
BASE_URL="${UPDATE_URL%setup.xml}"
BOOTSTRAP_URL="${BASE_URL}bootstrap/messagemanager-bootstrap.jar"
TARGET="$MM_HOME/bootstrap/messagemanager-bootstrap.jar"

echo "MM_HOME: $MM_HOME"
echo "Source:  $BOOTSTRAP_URL"
echo "Target:  $TARGET"
echo ""

# Refuse to run if MM looks like it's still running — overwriting a locked
# jar can leave the install in a half-broken state.
if pgrep -f "messagemanager\.bootstrap" >/dev/null 2>&1; then
    echo "ERROR: Message Manager appears to be running. Close it and try again." >&2
    exit 1
fi

# Sanity check: target directory exists
if [ ! -d "$(dirname "$TARGET")" ]; then
    echo "ERROR: Bootstrap directory not found: $(dirname "$TARGET")" >&2
    echo "Are you running this from inside a Message Manager install?" >&2
    exit 1
fi

# Download to .new then atomic rename. -f to fail on HTTP errors, -L to follow redirects.
TMP="$TARGET.new"
echo "Downloading…"
curl -fSL --output "$TMP" "$BOOTSTRAP_URL"

# Verify we got a non-empty jar
if [ ! -s "$TMP" ]; then
    echo "ERROR: Downloaded file is empty." >&2
    rm -f "$TMP"
    exit 1
fi

mv -f "$TMP" "$TARGET"

echo ""
echo "✓ Bootstrap replaced. Launch Message Manager again."
