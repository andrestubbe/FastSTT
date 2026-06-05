@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo [FastSTT] Starting Demo...
cd examples\Demo
mvn compile exec:java -Dexec.mainClass="faststt.Demo"
cd ..\..
