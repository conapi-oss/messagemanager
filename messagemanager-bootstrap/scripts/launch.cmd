@echo off

if [%1]==[debug] goto debug
goto run

:debug
set JVM_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005

:run
rem  assuming this is run from bin
cd ..
java %JVM_DEBUG% -p bootstrap\update4j.jar --add-modules jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote http://localhost/messagemanager/setup.xml