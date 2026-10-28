#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
modules=(MongoDB-01-VersionedStore MongoDB-02-LegacyDataCleanup MongoDB-03-OplogReplay MongoDB-04-DocumentMatcher MongoDB-05-FleetErrorReport)
index=1
for module in "${modules[@]}"; do
    build_dir="$repo_dir/target/mongodb/q$index"
    mkdir -p "$build_dir"
    sources=()
    while IFS= read -r -d '' source; do
        sources+=("$source")
    done < <(find "$repo_dir/$module/src" -name '*.java' -print0)
    javac --release 17 -Xlint:all -d "$build_dir" "${sources[@]}"
    java -cp "$build_dir" "mongodb.q$index.SolutionTest"
    index=$((index + 1))
done
