#!/bin/bash
# set via setenv if needed
export UPDATE_URL="https://files.conapi.at/mm/stable/setup.xml"
# this script is in the bin folder , set MM_HOME to the parent folder of bin
MM_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
export DMG_JAVA="$MM_HOME/../PlugIns/amazon-corretto-17.jdk/Contents/Home"
export MM_HOME

echo MM - $MM_HOME
echo DMG - $DMG_JAVA

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

# assuming this is run from bin
cd ..

# cleanup and upgrade
# allow setting custom jvm options
if [ -f "./bin/cleanup.sh" ]
then
     echo "Found cleanup script"
      . ./bin/cleanup.sh
fi

# allow setting custom jvm options
if [ -f "./bin/setenv.sh" ]
then
      . ./bin/setenv.sh
fi

echo Launching: $LAUNCHER_JAVA

if [ "$1" = "debug" ]; then
    "$LAUNCHER_JAVA" $JVM_OPTS -Ddeveloper=true -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
elif [ "$1" = "rdebug" ]; then
    "$LAUNCHER_JAVA" $JVM_OPTS -Ddeveloper=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
else
    "$LAUNCHER_JAVA" $JVM_OPTS -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst
fi

