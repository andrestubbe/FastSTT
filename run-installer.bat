@echo off
echo [FastSTT] Starting Installer...
cd examples\Installer
mvn compile exec:java -Dexec.mainClass="faststt.manager.FastSTTInstaller"
cd ..\..
