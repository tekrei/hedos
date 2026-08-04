#!/bin/bash

# Exit on error
set -e

REPO_PATH="repo"

# Arrays matching exact artifact definitions in pom.xml
GROUPS=("org.j3d" "org.xj3d" "org.xj3d" "org.xj3d" "org.xj3d" "org.xj3d" "org.xj3d")
ARTIFACTS=("aviatrix3d-all" "xj3d-core" "xj3d-browser" "xj3d-cadfilter" "xj3d-replica" "xj3d-3rdparty" "odejava")
FILES=("lib/aviatrix3d-all_3.1.1-nps.jar" "lib/xj3d-2.3-nps.jar" "lib/xj3d.browser_2.3.0-nps.jar" "lib/xj3d.cadfilter_2.3.0-nps.jar" "lib/xj3d.replica_2.3.0-nps.jar" "lib/xj3d-2.3-3rdparty-nps.jar" "lib/odejava-jni.jar")
VERSIONS=("3.1.1" "2.3.0" "2.3.0" "2.3.0" "2.3.0" "2.3.0" "2.3.0")

for i in "${!ARTIFACTS[@]}"; do
    GROUP="${GROUPS[$i]}"
    VERSION="${VERSIONS[$i]}"
    ARTIFACT="${ARTIFACTS[$i]}"
    FILE="${FILES[$i]}"

    if [ -f "$FILE" ]; then
        echo "Installing $ARTIFACT ($GROUP) from $FILE into local repository..."
        mvn install:install-file \
            -Dfile="$FILE" \
            -DgroupId="$GROUP" \
            -DartifactId="$ARTIFACT" \
            -Dversion="$VERSION" \
            -Dpackaging=jar \
            -DlocalRepositoryPath="$REPO_PATH"
    else
        echo "Warning: $FILE not found, skipping $ARTIFACT."
    fi
done

# Extract native shared libraries (.so, .dll, .dylib) from odejava-jni.jar into lib/
if [ -f "lib/odejava-jni.jar" ]; then
    echo "Extracting native binaries from lib/odejava-jni.jar into lib/..."
    unzip -o -q lib/odejava-jni.jar "*.so" "*.dll" "*.dylib" -d lib/ 2>/dev/null || true
fi

echo "Dependencies installed successfully."
