#!/bin/bash
set -e

# Test runner 

if [ "$1" == "-h" ] || [ "$1" == "--help" ]; then
    echo "Usage: ./scripts/run-tests.sh [test-class-name]"
    echo "Example: ./scripts/run-tests.sh SignupAndLoginTest"
    exit 0
fi

if [ -n "$1" ]; then
    echo "Running specific test: $1..."
    ./gradlew test --tests "*$1"
else
    echo "Running all tests..."
    ./gradlew test
fi

if [ $? -eq 0 ]; then
    echo "Tests passed. Report: build/reports/tests/test/index.html"
else
    echo "Tests failed."
    exit 1
fi
