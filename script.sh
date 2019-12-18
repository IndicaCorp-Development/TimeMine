#!/bin/bash

echo ">>> Populating release data"
PLUGIN_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)
PLUGIN_NAME=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.artifactId}' --non-recursive exec:exec)
RELEASE_DESCRIPTION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.releaseDescription}' --non-recursive exec:exec)
PRERELEASE_STR=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.prerelease}' --non-recursive exec:exec)
DRAFT_STR=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.draft}' --non-recursive exec:exec)
PLUGIN_JAR_PATH="./target/$PLUGIN_NAME-$PLUGIN_VERSION"
if [ "$PRERELEASE_STR" == "yes" ]
then
  PRERELEASE=true
else
  PRERELEASE=false
fi

if [ "$DRAFT_STR" == "yes" ]
then
  DRAFT=true
else
  DRAFT=false
fi
export PRERELEASE
export DRAFT
export PLUGIN_JAR_PATH
export TRAVIS_TAG=$PLUGIN_VERSION


echo ">>> Setting GitHub configurations"
git config --local user.name "$GITHUB_USERNAME"
git config --local user.email "$GITHUB_EMAIL"


PREVIOUS_VERSION="$(git tag -l "$PLUGIN_VERSION")"

echo ">>> Variable tests"
echo "Previous Version: $PREVIOUS_VERSION"
echo "Plugin Version: $PLUGIN_VERSION"
echo "Release Description: $RELEASE_DESCRIPTION"
echo "Path: $PLUGIN_JAR_PATH"
echo $PRERELEASE
echo $DRAFT

if [ "$PREVIOUS_VERSION" != "$PLUGIN_VERSION" ]
then
  echo ">>> New version detected. Tagging for GitHub release..."
  git tag -a "'$(echo $PLUGIN_VERSION)'" -m "'$(echo $RELEASE_DESCRIPTION)'"
else
  echo ">>> Existing release found. Not tagging..."
fi