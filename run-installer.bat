@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo [FastSTT] Building core library...
call mvn install -DskipTests -q
echo [FastSTT] Starting Installer...
cd examples\Installer
mvn compile exec:java -Dexec.mainClass="faststt.manager.FastSTTInstaller" -q
cd ..\..

