@echo off&setlocal

rem get the parent folder
for %%i in ("%~dp0..") do set "INSTALL_DIR=%%~fi"

rem set via setenv if needed
set UPDATE_URL="https://files.conapi.at/mm/stable/setup.xml"

set JVM_OPTS=-Djavax.net.ssl.trustStore=NUL -Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavafx.embed.singleThread=true -Dswing.systemlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel -Djava.net.useSystemProxies=true

rem use shipped JRE if available, otherwise fallback to JAVA_HOME and PATH
if exist "%INSTALL_DIR%\jre\" (
    set LAUNCHER_JAVA=%INSTALL_DIR%\jre\bin\javaw.exe
    set LAUNCHER_JAVA_DEBUG=%INSTALL_DIR%\jre\bin\java.exe
) else (
    if "%JAVA_HOME%"=="" (
       set LAUNCHER_JAVA=javaw
       set LAUNCHER_JAVA_DEBUG=java
    ) else (
       set LAUNCHER_JAVA=%JAVA_HOME%\bin\java.exe
       set LAUNCHER_JAVA_DEBUG=%JAVA_HOME%\bin\java.exe
    )
)

rem  assuming this is run from bin
cd ..

rem allow user to set additional JVM options i.e. truststore
if exist ".\bin\setenv.cmd" call ".\bin\setenv.cmd"

if "%~1"=="debug" (
    call %LAUNCHER_JAVA_DEBUG% %JVM_OPTS% -Ddeveloper=true -p bootstrap\update4j.jar --add-modules jdk.attach,jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local setup.xml --launchFirst
) else if "%~1"=="rdebug" (
    call %LAUNCHER_JAVA_DEBUG% %JVM_OPTS% -Ddeveloper=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -p bootstrap\update4j.jar --add-modules jdk.attach,jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local setup.xml --launchFirst
) else (
    start %LAUNCHER_JAVA% %JVM_OPTS% -p bootstrap\update4j.jar --add-modules jdk.attach,jdk.unsupported,java.scripting -m org.update4j/org.update4j.Bootstrap --remote %UPDATE_URL% --syncLocal --local setup.xml --launchFirst
)
