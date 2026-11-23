
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

REM remove what an interrupted application update left behind: its archive and the temporary files the JDK's zip
REM file system writes beside it while it downloads (zipfstmp*.tmp). Only files from an earlier day (forfiles counts
REM whole days): a second start must not remove the download of a start that is still running.
%SystemRoot%\System32\forfiles.exe /P "%MM_HOME%" /M messagemanager-update.zip /D -1 /C "cmd /c del /Q @path" >NUL 2>&1
%SystemRoot%\System32\forfiles.exe /P "%MM_HOME%" /M zipfstmp*.tmp /D -1 /C "cmd /c del /Q @path" >NUL 2>&1

REM Message Manager's own Java runtime: the app downloads it in the background and names it in runtime\current once
REM it has passed its checks - one line, a path under runtime/ relative to this folder. It is used when its java
REM starts, and the start is marked as a trial (runtime\trying) that the app clears once it is up; while a trial is
REM unfinished, and in every other case, the Java chosen before this script stays. setenv.cmd runs after this script:
REM a LAUNCHER_JAVA set there keeps the last word. find and findstr are Windows' own, whatever the PATH holds.
set "MM_RUNTIME="
set "MM_RUNTIME_LINES="
set "MM_TRIAL="
if exist "%MM_HOME%\runtime\trying" set "MM_TRIAL=1"
if defined MM_TRIAL echo Java runtime: a start on it has not brought the app up (yet) - using the Java chosen before
if not defined MM_TRIAL if exist "%MM_HOME%\runtime\current" for /f %%n in ('%SystemRoot%\System32\find.exe /c /v "" ^< "%MM_HOME%\runtime\current"') do set "MM_RUNTIME_LINES=%%n"
REM one line, and that line a path under runtime/: findstr alone would pass a file whose other line matches
if "%MM_RUNTIME_LINES%"=="1" (
    %SystemRoot%\System32\findstr.exe /r /x /c:"runtime/[A-Za-z0-9_-][A-Za-z0-9._-]*/[A-Za-z0-9_-][A-Za-z0-9._-]*" "%MM_HOME%\runtime\current" >NUL 2>&1
    if not errorlevel 1 set /p MM_RUNTIME=<"%MM_HOME%\runtime\current"
)
if defined MM_RUNTIME (
    %SystemRoot%\System32\findstr.exe /c:".." "%MM_HOME%\runtime\current" >NUL 2>&1
    if not errorlevel 1 set "MM_RUNTIME="
)
if defined MM_RUNTIME set "MM_RUNTIME=%MM_RUNTIME:/=\%"
if defined MM_RUNTIME (
    "%MM_HOME%\%MM_RUNTIME%\bin\java.exe" -version >NUL 2>&1
    if errorlevel 1 set "MM_RUNTIME="
)
if defined MM_RUNTIME (
    echo Java runtime: %MM_RUNTIME% starts
    >"%MM_HOME%\runtime\trying" echo %MM_RUNTIME%
    set "LAUNCHER_JAVA=%MM_RUNTIME%\bin\javaw.exe"
    set "LAUNCHER_JAVA_DEBUG=%MM_RUNTIME%\bin\java.exe"
) else (
    if not defined MM_TRIAL if exist "%MM_HOME%\runtime\current" echo Java runtime: runtime\current names none that starts
)
