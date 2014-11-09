#!/bin/sh

# Browse to the android library project and build
cd android-uribeacon
./gradlew :uribeacon-library:copyReleaseLint
# ./gradlew assembleRelease
