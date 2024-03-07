@echo off

set UPDATE_URL="http://localhost/messagemanager/setup.xml"

rem  assuming this is run from bin
cd ..

if "%~1"=="debug" (
    call java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL%
) else (
    start javaw -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL%
)
