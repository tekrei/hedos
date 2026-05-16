#!/bin/bash

# This script installs the Xj3D libraries from the lib/ folder into the project-local repo.
# Make sure you have the .jar files in the lib/ directory as described in the README.

REPO_PATH="repo"

# Use arrays for better compatibility across different bash versions
ARTIFACTS=("aviatrix3d-all" "xj3d-core" "xj3d-browser" "xj3d-cadfilter" "xj3d-replica" "xj3d-3rdparty" "odejava")
FILES=("lib/aviatrix3d-all_3.1.1-nps.jar" "lib/xj3d-2.3-nps.jar" "lib/xj3d.browser_2.3.0-nps.jar" "lib/xj3d.cadfilter_2.3.0-nps.jar" "lib/xj3d.replica_2.3.0-nps.jar" "lib/xj3d-2.3-3rdparty-nps.jar" "lib/odejava-jni.jar")
VERSIONS=("3.1.1" "2.3.0" "2.3.0" "2.3.0" "2.3.0" "2.3.0" "2.3.0")
GROUP="org.j3d"

for i in "${!ARTIFACTS[@]}"; do
    VERSION="${VERSIONS[$i]}"
    ARTIFACT="${ARTIFACTS[$i]}"
    FILE="${FILES[$i]}"

    if [ -f "$FILE" ]; then
        echo "Installing $ARTIFACT from $FILE..."
        mvn install:install-file -Dfile="$FILE" -DgroupId="$GROUP" -DartifactId="$ARTIFACT" -Dversion="$VERSION" -Dpackaging=jar -DlocalRepositoryPath="$REPO_PATH"
    else
        echo "Warning: $FILE not found, skipping $ARTIFACT."
    fi
done