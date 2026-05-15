@echo off
echo [FastSTT] Starting Demo...
cd examples\Demo
mvn compile exec:java -Dexec.mainClass="faststt.Demo"
cd ..\..
