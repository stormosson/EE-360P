#!/usr/bin/env bash
# Written by Eric Crosson
# 2016-01-28

# Exit under these circumstances -- when we don't care about publishing javadoc
if [[ "$TRAVIS_REPO_SLUG" != "stormosson/EE-360P" || \
      "$TRAVIS_JDK_VERSION" != "oraclejdk8" || \
      "$TRAVIS_PULL_REQUEST" != "false" || \
      "$TRAVIS_BRANCH" != "master" ]]; then
    exit
fi

# Get to the Travis build dir, configure git and clone the repo
cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/stormosson/stormosson.github.io $HOME/gh-pages

# Commit and push changes
cd $HOME/gh-pages
git rm -rf *
cp -Rf $HOME/javadoc-latest/* ./
git add -f .
git commit -m "Latest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
git push -fq origin gh-pages > /dev/null
