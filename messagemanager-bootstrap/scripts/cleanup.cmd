
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