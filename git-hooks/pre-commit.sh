#!/bin/sh
#
# Git hook to perform relevant code checks before a commit.
#
# Based on:
# - https://proandroiddev.com/ooga-chaka-git-hooks-to-enforce-code-quality-11ce8d0d23cb
# - https://medium.com/@hamen/running-android-unit-tests-before-git-push-e9e7ec78e6d1
#

CMD="./gradlew clean check"

$CMD
RESULT=$?

if [ $RESULT -ne 0 ] ; then
 echo "Issues detected for: $CMD"
 exit 1
else
 echo "No issues detected for: $CMD"
 exit 0
fi