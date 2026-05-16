#!/bin/bash

# ==============================================================================
# Life OS — Android SDK Auto-config
# ==============================================================================
# Detects OS and writes the correct local.properties for the Android project.
# ==============================================================================

ANDROID_DIR="android"
PROPERTIES_FILE="$ANDROID_DIR/local.properties"

echo "Detecting OS..."

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Linux detected."
    # Standard Linux SDK path
    SDK_PATH="$HOME/Android/Sdk"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    echo "Windows (via bash) detected."
    # Standard Windows SDK path
    SDK_PATH="C:\\Users\\$USER\\AppData\\Local\\Android\\Sdk"
else
    echo "Unknown OS: $OSTYPE. Please set sdk.dir manually in $PROPERTIES_FILE"
    exit 1
fi

if [ -d "$SDK_PATH" ] || [[ "$OSTYPE" != "linux-gnu"* ]]; then
    echo "Writing sdk.dir to $PROPERTIES_FILE..."
    echo "sdk.dir=$SDK_PATH" > "$PROPERTIES_FILE"
    echo "Done."
else
    echo "Warning: SDK path $SDK_PATH not found. Please ensure Android SDK is installed."
fi
