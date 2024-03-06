#!/bin/bash

# assuming this is run from bin
cd ..
java -p bootstrap/update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote http://localhost/messagemanager/setup.xml