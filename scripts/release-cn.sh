#!/bin/sh
#
# Releases the Chinese fork: takes the upstream version and appends the fork
# suffix, so a release of this fork is never mistaken for an upstream one.
#
#   upstream 3.0.0-RC10 -> 3.0.0-RC10-cn.1
#   3.0.0-RC10-cn.1     -> 3.0.0-RC10-cn.2   (same upstream version)
#   upstream 3.0.0-RC11 -> 3.0.0-RC11-cn.1   (counter starts over)
#
# Usage:
#   ./scripts/release-cn.sh              # next counter for the current version
#   ./scripts/release-cn.sh 3.0.0-RC11   # start from this upstream version
#   ./scripts/release-cn.sh --dry-run    # only print what would happen
#
set -eu

SUFFIX='-cn.'
DRY_RUN=false

for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=true ;;
        -h|--help) sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        -*) echo "Unknown option: $arg" >&2; exit 2 ;;
        *) UPSTREAM="$arg" ;;
    esac
done

if [ -n "$(git status --porcelain)" ]; then
    echo "Working tree is not clean, commit or stash first." >&2
    exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$BRANCH" != 'main' ]; then
    echo "Not on main (on '$BRANCH'), releases are cut from main." >&2
    exit 1
fi

# Without an explicit version, keep the upstream part of the current one:
# 3.0.0-RC10-cn.2 -> 3.0.0-RC10
if [ -z "${UPSTREAM:-}" ]; then
    CURRENT=$(sed -n 's/^version=//p' gradle.properties)
    UPSTREAM=${CURRENT%%"$SUFFIX"*}
    echo "Upstream version: $UPSTREAM (from $CURRENT)"
fi

case "$UPSTREAM" in
    *"$SUFFIX"*)
        echo "'$UPSTREAM' already carries the fork suffix, pass the upstream version without it." >&2
        exit 1
        ;;
esac

# Find the highest counter already used for this upstream version.
GREP_ESCAPED=$(printf '%s' "$UPSTREAM$SUFFIX" | sed 's/[.[\*^$/]/\\&/g')
COUNTER=0
for tag in $(git tag -l "$UPSTREAM$SUFFIX*"); do
    n=${tag#"$UPSTREAM$SUFFIX"}
    case "$n" in
        ''|*[!0-9]*) continue ;;
    esac
    if [ "$n" -gt "$COUNTER" ]; then
        COUNTER=$n
    fi
done
COUNTER=$((COUNTER + 1))

VERSION="$UPSTREAM$SUFFIX$COUNTER"

echo
echo "Upstream: $UPSTREAM"
echo "Version:  $VERSION"
echo "Tag:      $VERSION"
echo

if $DRY_RUN; then
    echo "Dry run, nothing was changed."
    exit 0
fi

# gradle.properties holds the version between x-release-please markers. Keep the
# markers so the file still parses if upstream tooling reads it.
python3 - "$VERSION" <<'EOF'
import re
import sys

version = sys.argv[1]
path = 'gradle.properties'
with open(path, encoding='utf-8') as f:
    text = f.read()

new, n = re.subn(
    r'(# x-release-please-start-version\n)version=[^\n]*(\n# x-release-please-end)',
    r'\g<1>version=' + version + r'\g<2>',
    text,
)
if n != 1:
    sys.exit(f'Could not find the version block in {path}')

with open(path, 'w', encoding='utf-8') as f:
    f.write(new)
print(f'{path}: version={version}')
EOF

git add gradle.properties
git commit -q -m "chore(release): $VERSION"
git tag -a "$VERSION" -m "$VERSION"

echo
echo "Created commit and tag. To publish:"
echo
echo "    git push origin main && git push origin $VERSION"
echo
echo "The Lifecycle workflow builds the jars and native images, then uploads"
echo "them to the GitHub release for the tag."
