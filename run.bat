@echo off
setlocal
echo =================================================================
echo  SmartCity360 - Design Patterns Web Platform & H2 Database
echo =================================================================
cd /d "%~dp0"

set MVN_BIN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd

if exist "%MVN_BIN%" (
    echo Launching via Maven exec:java...
    call "%MVN_BIN%" compile exec:java
) else (
    echo Launching via Java directly...
    java -cp "target/classes;%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar" com.smartcity.parta.SmartCityWebApp
)

pause
