#!/bin/bash
# Sourced by launch.sh from the folder the app runs from, where updates install (Application Support on macOS):
# MM_HOME names it here, also on installations whose own launch.sh set it to the folder they were extracted to.
MM_HOME="$PWD"
export MM_HOME

# check if there is an update update4j
echo "Checking for $MM_HOME/bootstrap/update4j.jar.new"
if [ -f "$MM_HOME/bootstrap/update4j.jar.new" ]; then
    echo "Upgrading Update4J jar"
    rm -f "$MM_HOME/bootstrap/update4j.jar.old"
    mv "$MM_HOME/bootstrap/update4j.jar" "$MM_HOME/bootstrap/update4j.jar.old"
    # copy as update will anyway download it again on next start (preserve permissions with -p)
    cp -p "$MM_HOME/bootstrap/update4j.jar.new" "$MM_HOME/bootstrap/update4j.jar"
fi

# remove previously failed update
if [ -f "$MM_HOME/update.zip" ]; then
    rm -f "$MM_HOME/update.zip"
fi

# remove previously used javafx files that had the os name in it, mainly a mac issue
# delete files not matching javafx-[a-z]*-[0-9]*.jar
# Set the directory where the files are located
#directory="$MM_HOME/bootstrap"
#for file in "$directory"/javafx-*.jar; do
#    # Check if the file exists and doesn't match the pattern we want to keep
#    if [[ -f "$file" && ! "$file" =~ javafx-[a-z]*-[0-9]*\.jar$ ]]; then
#        echo "Deleting javafx file: - $file"
#        rm -f "$file"
#    fi
#done

# Message Manager's own Java runtime: the app downloads it in the background and names it in runtime/current once it
# has passed its checks - one line, a path under runtime/ relative to this folder. It is used when its java starts,
# and the start is marked as a trial (runtime/trying) that the app clears once it is up; while a trial is unfinished,
# and in every other case, the Java chosen before this script stays. setenv.sh runs after this script: a
# LAUNCHER_JAVA set there keeps the last word.
if [ -f "$MM_HOME/runtime/trying" ]; then
    echo "Java runtime: a start on it has not brought the app up (yet) - using the Java chosen before"
elif [ -f "$MM_HOME/runtime/current" ]; then
    MM_RUNTIME="$(head -n 1 "$MM_HOME/runtime/current" | tr -d '\r')"
    case "$MM_RUNTIME" in
        *..* | *//* | */.* | runtime/*/*/* | *[!A-Za-z0-9._/-]*) MM_RUNTIME="" ;;
        runtime/?*/?*) ;;
        *) MM_RUNTIME="" ;;
    esac
    if [ -n "$MM_RUNTIME" ] && "$MM_HOME/$MM_RUNTIME/bin/java" -version >/dev/null 2>&1; then
        echo "Java runtime: $MM_RUNTIME starts"
        printf '%s\n' "$MM_RUNTIME" > "$MM_HOME/runtime/trying"
        LAUNCHER_JAVA="./$MM_RUNTIME/bin/java"
    else
        echo "Java runtime: runtime/current names none that starts"
    fi
fi
