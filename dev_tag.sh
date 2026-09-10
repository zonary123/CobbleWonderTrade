#!/usr/bin/env bash
set -e

GRADLE_FILE="gradle.properties"
if [ ! -f "$GRADLE_FILE" ]; then
    echo "Error: No se encontró gradle.properties" >&2
    exit 1
fi

VERSION=$(grep -E '^(mod_)?version=' "$GRADLE_FILE" | head -n1 | cut -d'=' -f2 | tr -d '\r ' || true)
if [ -z "$VERSION" ]; then
    echo "Error: No se encontró version en gradle.properties" >&2
    exit 1
fi

GIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "dev")
SAFE_VERSION=$(echo "$VERSION" | tr -cd 'a-zA-Z0-9._-')
TAG="v${SAFE_VERSION}-dev-${GIT_HASH}"

if git tag -l "$TAG" | grep -q "$TAG"; then
    echo "Warning: El tag '$TAG' ya existe localmente."
else
    git tag -a "$TAG" -m "Dev build $TAG"
    echo "Tag local creado: $TAG"
fi

git push origin "$TAG"
echo "Tag subido a origin: $TAG"
