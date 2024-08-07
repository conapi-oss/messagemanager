#!/bin/bash
# set via setenv if needed
export UPDATE_URL="https://files.conapi.at/mm/stable/setup.xml"
# this script is in the bin folder , set MM_HOME to the parent folder of bin
export MM_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

if [[ -z "${JAVA_HOME}" ]]; then
  # no JAVA_HOME set
  export LAUNCHER_JAVA=java
else
  export LAUNCHER_JAVA=$JAVA_HOME/bin/java
fi

export JVM_OPTS="-Djavafx.embed.singleThread=true -DSolace_JMS_Browser_Timeout_In_MS=1000 -Djava.net.useSystemProxies=true"

# assuming this is run from bin
cd ..
# allow setting custom jvm options
if [ -f "./bin/setenv.sh" ]
then
      . ./bin/setenv.sh
fi

$LAUNCHER_JAVA $JVM_OPTS -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local setup.xml --launchFirst