@echo off
chcp 65001 > nul
echo ==================================================
echo [FastSTT] FastSharedMemory Zero-Copy Audio Demo
echo ==================================================
echo.
echo Launching Zero-Copy Shared Memory Audio Transcription...
echo.

java --enable-native-access=ALL-UNNAMED -cp "target/test-classes;target/classes;target/FastSTT-0.1.2.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastSharedMemory/0.1.2/FastSharedMemory-0.1.2.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastPointer/0.1.1/FastPointer-0.1.1.jar;%USERPROFILE%/.m2/repository/com/github/andrestubbe/FastCore/0.1.0/FastCore-0.1.0.jar" faststt.benchmark.ZeroCopySttDemo

pause
