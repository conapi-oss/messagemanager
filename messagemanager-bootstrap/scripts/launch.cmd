@echo off

rem set via setenv if needed
set UPDATE_URL="https://product.conapi.at/messagemanager/setup.xml"

set JVM_OPTS=-Djavafx.embed.singleThread=true

if "%JAVA_HOME%"=="" (
   set LAUNCHER_JAVA=java
) else (
   set LAUNCHER_JAVA=%JAVA_HOME%\bin\java.exe
)


rem  assuming this is run from bin
cd ..

rem allow user to set additional JVM options i.e. truststore
if exist ".\bin\setenv.cmd" call ".\bin\setenv.cmd"

if "%~1"=="debug" (
    call %LAUNCHER_JAVA% %JVM_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local bootstrap.xml --launchFirst
) else (
    start %LAUNCHER_JAVA% %JVM_OPTS% -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local bootstrap.xml --launchFirst
)
