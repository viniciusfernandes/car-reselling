#!/usr/bin/env bash
# =============================================================================
# build-api.sh – build the car-reselling-api Spring Boot JAR
#
# Usage:
#   ./build-api.sh [OPTIONS] [VERSION]
#
# Arguments:
#   VERSION        Semantic version string (e.g. 1.7 or 2.0.1).
#                  Defaults to the version declared in backend/build.gradle.
#
# Options:
#   -s, --skip-tests   Skip the test phase (./gradlew clean build -x test)
#   -h, --help         Show this help message and exit
#
# Examples:
#   ./build-api.sh                  # build with the default version
#   ./build-api.sh 1.8              # build with version 1.8
#   ./build-api.sh --skip-tests 1.8 # build 1.8 without running tests
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"

# --------------------------------------------------------------------------
# Defaults
# --------------------------------------------------------------------------
VERSION=""
SKIP_TESTS=false

# --------------------------------------------------------------------------
# Parse arguments
# --------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      sed -n '/^# ====/,/^# ====/p' "$0" | sed 's/^# \{0,2\}//'
      exit 0
      ;;
    -s|--skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    -*)
      echo "ERROR: Unknown option '$1'. Run ./build-api.sh --help for usage." >&2
      exit 1
      ;;
    *)
      if [[ -z "$VERSION" ]]; then
        VERSION="$1"
      else
        echo "ERROR: Unexpected argument '$1'. VERSION was already set to '$VERSION'." >&2
        exit 1
      fi
      shift
      ;;
  esac
done

# --------------------------------------------------------------------------
# Validate version format if provided (digits and dots only, e.g. 1.8 / 2.0.1)
# --------------------------------------------------------------------------
if [[ -n "$VERSION" ]] && ! [[ "$VERSION" =~ ^[0-9]+(\.[0-9]+)*$ ]]; then
  echo "ERROR: Invalid version format '$VERSION'. Expected digits and dots (e.g. 1.8 or 2.0.1)." >&2
  exit 1
fi

# --------------------------------------------------------------------------
# Resolve default version from build.gradle when none was supplied
# --------------------------------------------------------------------------
if [[ -z "$VERSION" ]]; then
  DEFAULT_VERSION=$(grep -E "^\s*version\s*=" "$BACKEND_DIR/build.gradle" \
    | grep -oP "findProperty\('projectVersion'\) \?:\s*'\K[^']+" || true)
  if [[ -z "$DEFAULT_VERSION" ]]; then
    DEFAULT_VERSION="(build.gradle default)"
  fi
fi

# --------------------------------------------------------------------------
# Summary
# --------------------------------------------------------------------------
echo "=============================================="
echo "  car-reselling-api  –  build"
echo "=============================================="
if [[ -n "$VERSION" ]]; then
  echo "  Version      : $VERSION"
  echo "  Output JAR   : backend/build/libs/car-reselling-api-${VERSION}.jar"
else
  echo "  Version      : $DEFAULT_VERSION"
fi
echo "  Skip tests   : $SKIP_TESTS"
echo "  Backend dir  : $BACKEND_DIR"
echo "=============================================="
echo ""

# --------------------------------------------------------------------------
# Build
# --------------------------------------------------------------------------
cd "$BACKEND_DIR"

GRADLE_ARGS=("clean" "build")

if [[ "$SKIP_TESTS" == true ]]; then
  GRADLE_ARGS+=("-x" "test")
fi

if [[ -n "$VERSION" ]]; then
  GRADLE_ARGS+=("-PprojectVersion=$VERSION")
fi

echo "Running: ./gradlew ${GRADLE_ARGS[*]}"
echo ""

./gradlew "${GRADLE_ARGS[@]}"

# --------------------------------------------------------------------------
# Report output
# --------------------------------------------------------------------------
echo ""
echo "=============================================="
echo "  Build successful!"
if [[ -n "$VERSION" ]]; then
  JAR="$BACKEND_DIR/build/libs/car-reselling-api-${VERSION}.jar"
else
  JAR=$(ls "$BACKEND_DIR"/build/libs/car-reselling-api-*.jar 2>/dev/null | head -1 || true)
fi

if [[ -n "$JAR" && -f "$JAR" ]]; then
  SIZE=$(du -sh "$JAR" | cut -f1)
  echo "  Artifact : $JAR"
  echo "  Size     : $SIZE"
fi
echo "=============================================="
