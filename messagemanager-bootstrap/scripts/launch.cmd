@echo off

rem set via setenv if needed
set UPDATE_URL="https://product.conapi.at/messagemanager/setup.xml"
set LAUNCHER_JAVA_HOME=%JAVA_HOME%\bin

rem  assuming this is run from bin
cd ..

rem allow user to set additional JVM options i.e. truststore
if exist ".\bin\setenv.cmd" call ".\bin\setenv.cmd"

if "%~1"=="debug" (
    call %LAUNCHER_JAVA_HOME%\java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local bootstrap.xml --launchFirst
) else (
    start %LAUNCHER_JAVA_HOME%\javaw -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local bootstrap.xml --launchFirst
)
