#!/bin/bash

set -e

if [ ! -d "repo" ]; then
    echo "Error: Local repository 'repo' not found."
    echo "Please run: chmod +x install_deps.sh && ./install_deps.sh"
    exit 1
fi

# Ensure target/natives exists and copy native binaries
mkdir -p target/natives
cp lib/*.so* target/natives/ 2>/dev/null || cp lib/*.dll target/natives/ 2>/dev/null || true

echo "Building project with Maven..."
mvn clean compile -U

# Re-copy native binaries in case clean deleted target/
cp lib/*.so* target/natives/ 2>/dev/null || cp lib/*.dll target/natives/ 2>/dev/null || true

echo "Starting HeDoS..."

if [ -z "$XJ3D_NATIVE_PATH" ]; then
    mvn exec:exec
else
    mvn exec:exec -Dxj3d.natives="$XJ3D_NATIVE_PATH"
fi
