
REM check if there is an update update4j
echo Checking for %MM_HOME%\bootstrap\update4j.jar.new
if exist "%MM_HOME%\bootstrap\update4j.jar.new" (
    echo Upgrading Update4J jar
    if exist "%MM_HOME%\bootstrap\update4j.jar.old" del /Q "%MM_HOME%\bootstrap\update4j.jar.old"
    move /Y "%MM_HOME%\bootstrap\update4j.jar" "%MM_HOME%\bootstrap\update4j.jar.old"
    REM copy as update will anyway download it again on next start
    copy /Y "%MM_HOME%\bootstrap\update4j.jar.new" "%MM_HOME%\bootstrap\update4j.jar"
)

REM remove previously failed update
if exist "%MM_HOME%\update.zip" del /Q "%MM_HOME%\update.zip"

REM Message Manager's own Java runtime: the app downloads it in the background and names it in runtime\current once
REM it has passed its checks - one line, a path under runtime/ relative to this folder. It is used when its java
REM starts; in every other case the Java chosen before this script stays. setenv.cmd runs after this script: a
REM LAUNCHER_JAVA set there keeps the last word.
set "MM_RUNTIME="
if exist "%MM_HOME%\runtime\current" (
    findstr /r /x /c:"runtime/[A-Za-z0-9._-]*/[A-Za-z0-9._-]*" "%MM_HOME%\runtime\current" >NUL 2>&1
    if not errorlevel 1 set /p MM_RUNTIME=<"%MM_HOME%\runtime\current"
)
if defined MM_RUNTIME (
    findstr /c:".." "%MM_HOME%\runtime\current" >NUL 2>&1
    if not errorlevel 1 set "MM_RUNTIME="
)
if defined MM_RUNTIME set "MM_RUNTIME=%MM_RUNTIME:/=\%"
if defined MM_RUNTIME (
    "%MM_HOME%\%MM_RUNTIME%\bin\java.exe" -version >NUL 2>&1
    if errorlevel 1 set "MM_RUNTIME="
)
if defined MM_RUNTIME (
    echo Java runtime: %MM_RUNTIME% starts
    set "LAUNCHER_JAVA=%MM_RUNTIME%\bin\javaw.exe"
    set "LAUNCHER_JAVA_DEBUG=%MM_RUNTIME%\bin\java.exe"
) else (
    if exist "%MM_HOME%\runtime\current" echo Java runtime: runtime\current names none that starts
)
