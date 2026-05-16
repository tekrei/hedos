#!/bin/bash

# Exit on error
set -e

if [ ! -d "repo" ]; then
    echo "Error: Local repository 'repo' not found."
    echo "Please run: chmod +x install_deps.sh && ./install_deps.sh"
    exit 1
fi

echo "Building project with Maven..."
mvn clean compile -U

# Copy native libraries from lib/ to target/natives so the JVM can find them
mkdir -p target/natives
cp lib/*.so target/natives/ 2>/dev/null || cp lib/*.dll target/natives/ 2>/dev/null || true

echo "Starting HeDoS..."
# If you get UnsatisfiedLinkError for odejava, you might need to point to Xj3D installation natives:
# mvn exec:exec -Dxj3d.natives=/path/to/xj3d/installation/natives/Linux/x86_64

# Default to unpacked natives if not specified via environment variable
if [ -z "$XJ3D_NATIVE_PATH" ]; then
    mvn exec:exec
else
    mvn exec:exec -Dxj3d.natives="$XJ3D_NATIVE_PATH"
fi
