#!/bin/bash

# check if there is an update update4j
echo "Checking for $MM_HOME/bootstrap/update4j.jar.new"
if [ -f "$MM_HOME/bootstrap/update4j.jar.new" ]; then
    echo "Upgrading Update4J jar"
    rm -f "$MM_HOME/bootstrap/update4j.jar.old"
    mv "$MM_HOME/bootstrap/update4j.jar" "$MM_HOME/bootstrap/update4j.jar.old"
    # copy as update will anyway download it again on next start
    cp "$MM_HOME/bootstrap/update4j.jar.new" "$MM_HOME/bootstrap/update4j.jar"
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