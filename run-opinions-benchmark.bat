@echo off
chcp 65001 > nul
echo ==================================================
echo [FastSTT] opinions.mp3 Benchmark Demo
echo ==================================================
echo.
echo Launching Real-World Audio Benchmark for opinions.mp3...
echo.

java --enable-native-access=ALL-UNNAMED -cp "target/test-classes;target/classes;target/FastSTT-0.1.3.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastSharedMemory/0.1.2/FastSharedMemory-0.1.2.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastPointer/0.1.1/FastPointer-0.1.1.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastCore/0.1.0/FastCore-0.1.0.jar" faststt.benchmark.OpinionsMp3BenchmarkDemo

pause
