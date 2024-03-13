#!/bin/bash


# set via setenv if needed
export UPDATE_URL="https://product.conapi.at/messagemanager/setup.xml"
export LAUNCHER_JAVA_HOME=$JAVA_HOME/bin

# assuming this is run from bin
cd ..

# allow setting custom jvm options
if [ -f "./bin/setenv.sh" ]
then
      . "/bin/setenv.sh"
fi

$LAUNCHER_JAVA_HOME/java $JVM_OPTS -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote $UPDATE_URL --syncLocal --local bootstrap.xml --launchFirst