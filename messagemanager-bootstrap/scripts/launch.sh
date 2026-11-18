#!/bin/bash

# Detect macOS and DMG environment
IS_MACOS=false
if [[ "$OSTYPE" == "darwin"* ]]; then
    IS_MACOS=true
fi

# set via setenv if needed
export UPDATE_URL="https://files.conapi.at/mm/stable/setup.xml"
# this script is in the bin folder , set MM_HOME to the parent folder of bin
MM_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
# For macOS, use the bundled JRE
export DMG_JAVA="/Applications/Message Manager.app/Contents/PlugIns/jdk-17.0.13+11-jre/Contents/Home"
export MM_HOME

echo MM - $MM_HOME
echo DMG - $DMG_JAVA

# assuming this is run from bin
cd ..

# For macOS, handle first start and file copying
if [ "$IS_MACOS" = true ]; then
    APP_SUPPORT_DIR="$HOME/Library/Application Support/conapi/Message Manager"

    # Check if directory exists - if not, this is first start
    if [ ! -d "$APP_SUPPORT_DIR" ]; then
        echo "First start detected, creating application directory..."
        mkdir -p "$APP_SUPPORT_DIR"

        # Copy all files from MM_HOME to App Support
        echo "Copying application files from $MM_HOME to $APP_SUPPORT_DIR"
        cp -R "$MM_HOME/"* "$APP_SUPPORT_DIR/"
    fi
    # Change to the new directory for the rest of the execution
    cd "$APP_SUPPORT_DIR"
    # Updates install here: the app, cleanup.sh and @MM_HOME@ in the plugin profiles all use this folder
    MM_HOME="$PWD"
    export MM_HOME
fi

if [ -f "$DMG_JAVA/bin/java" ]; then
    LAUNCHER_JAVA="$DMG_JAVA/bin/java"
else
  if [[ -z "${JAVA_HOME}" ]]; then
    # no JAVA_HOME set
    export LAUNCHER_JAVA=java
  else
    export LAUNCHER_JAVA=$JAVA_HOME/bin/java
  fi
fi

export JVM_OPTS="-Djavafx.embed.singleThread=true -DSolace_JMS_Browser_Timeout_In_MS=1000 -Djava.net.useSystemProxies=true"

# Cleanup and upgrade script
if [ -f "./bin/cleanup.sh" ]
then
     echo "Found cleanup script"
      . ./bin/cleanup.sh
fi

# allow setting custom JVM options
if [ -f "./bin/setenv.sh" ]
then
      . ./bin/setenv.sh
fi

echo Launching: $LAUNCHER_JAVA

# NOTE: $JVM_OPTS must NOT be quoted here. Quoting it passes every option as a
# SINGLE argument, which the JVM reads as one -D whose value is the rest of the
# string - so all but the first option are silently ignored.
# -Dfile.encoding is set explicitly (and before $JVM_OPTS, so it can still be
# overridden there) because the bundled JRE is 17, where the default charset
# follows the OS locale. Without it, message content is written with whatever
# the platform charset happens to be and unrepresentable characters are lost.
if [ "$1" = "debug" ]; then
    "$LAUNCHER_JAVA" -Dfile.encoding=UTF-8 $JVM_OPTS -Ddeveloper=true -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting,java.net.http,java.sql,java.naming,java.compiler -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
elif [ "$1" = "rdebug" ]; then
    "$LAUNCHER_JAVA" -Dfile.encoding=UTF-8 $JVM_OPTS -Ddeveloper=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting,java.net.http,java.sql,java.naming,java.compiler -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
else
    "$LAUNCHER_JAVA" -Dfile.encoding=UTF-8 $JVM_OPTS -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting,java.net.http,java.sql,java.naming,java.compiler -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
fi