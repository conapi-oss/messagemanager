@echo off

jpackage --type exe ^
    --input messagemanager-app\build\install\messagemanager-app\lib ^
    --main-jar messagemanager-app-4.0-SNAPSHOT.jar ^
    --main-class nl.queuemanager.app.Main ^
    --java-options -Xmx512m ^
    --java-options -DSolace_JMS_Browser_Timeout_In_MS=1000 ^
    --name "Message Manager" ^
    --app-version 4.0.0 ^
    --vendor "Gerco Dries" ^
    --copyright "(c) 2008-2022 Gerco Dries" ^
    --description "A Compelling Replacement for the JMS Test Client" ^
    --dest build\install ^
    --about-url "https://queuemanager.nl" ^
    --win-menu-group "Gerco Dries" ^
    --win-menu ^
    --verbose
