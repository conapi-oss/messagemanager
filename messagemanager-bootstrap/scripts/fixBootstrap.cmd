@echo off
REM
REM fixBootstrap.cmd — re-download messagemanager-bootstrap.jar from files.conapi.at,
REM bypassing Update4J entirely.
REM
REM Use this when an in-place self-update of the bootstrap is blocked — typically
REM because Windows file locking prevents Update4J from overwriting the running
REM bootstrap jar, or because the running bootstrap has an upstream bug
REM (e.g. a too-short HTTP read timeout) that aborts its own download attempts.
REM
REM Run with Message Manager NOT RUNNING.

setlocal EnableDelayedExpansion

REM This script lives in <MM_HOME>\bin\; derive MM_HOME from its own location.
set "MM_HOME=%~dp0.."
for %%i in ("%MM_HOME%") do set "MM_HOME=%%~fi"

REM Load setenv.cmd if it sets UPDATE_URL (matches launch.cmd)
if exist "%MM_HOME%\bin\setenv.cmd" call "%MM_HOME%\bin\setenv.cmd"

if "%UPDATE_URL%"=="" set "UPDATE_URL=https://files.conapi.at/mm/stable/setup.xml"

REM Strip trailing "setup.xml" to get the base URL
set "BASE_URL=%UPDATE_URL:setup.xml=%"
set "BOOTSTRAP_URL=%BASE_URL%bootstrap/messagemanager-bootstrap.jar"
set "TARGET=%MM_HOME%\bootstrap\messagemanager-bootstrap.jar"

echo MM_HOME: %MM_HOME%
echo Source:  %BOOTSTRAP_URL%
echo Target:  %TARGET%
echo.

REM Refuse to run if MM looks like it's still running.
tasklist /FI "IMAGENAME eq java.exe" /FO LIST 2>nul | findstr /I "messagemanager" >nul
if not errorlevel 1 (
    echo ERROR: Message Manager appears to be running. Close it and try again.
    exit /b 1
)

REM Sanity check: target directory exists
if not exist "%MM_HOME%\bootstrap" (
    echo ERROR: Bootstrap directory not found: %MM_HOME%\bootstrap
    echo Are you running this from inside a Message Manager install?
    exit /b 1
)

REM Download to .new then atomic rename. PowerShell honors long timeouts.
echo Downloading...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-WebRequest -UseBasicParsing -Uri '%BOOTSTRAP_URL%' -OutFile '%TARGET%.new' } catch { Write-Error $_; exit 1 }"
if errorlevel 1 (
    echo ERROR: Download failed.
    if exist "%TARGET%.new" del /Q "%TARGET%.new"
    exit /b 1
)

REM Verify non-empty download
for %%I in ("%TARGET%.new") do if %%~zI LSS 1024 (
    echo ERROR: Downloaded file is suspiciously small ^(%%~zI bytes^).
    del /Q "%TARGET%.new"
    exit /b 1
)

move /Y "%TARGET%.new" "%TARGET%" >nul
echo.
echo OK. Bootstrap replaced. Launch Message Manager again.
endlocal
