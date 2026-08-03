#!/usr/bin/env bash

# Load .env file if it exists
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Ensure Java is in path (from SDKMAN)
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

echo "Starting DevHub Spring Boot Backend..."
mvn spring-boot:run
