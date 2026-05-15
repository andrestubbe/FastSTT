@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   FastSTT — Native DLL Builder
echo ========================================

:: 1. Detect JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo [INFO] JAVA_HOME not set, searching in C:\Program Files\Java...
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\include\jni.h" (
            set "JAVA_HOME=%%d"
            goto :found_java
        )
    )
    echo [ERROR] could not find a valid JDK in C:\Program Files\Java. Please set JAVA_HOME.
    exit /b 1
)
:found_java
echo [INFO] Using JAVA_HOME: %JAVA_HOME%

:: 2. Detect Visual Studio
set "VS_PATH="
for %%v in (2022, 2019) do (
    for %%e in (Community, BuildTools, Professional, Enterprise) do (
        if exist "C:\Program Files\Microsoft Visual Studio\%%v\%%e\VC\Auxiliary\Build\vcvars64.bat" (
            set "VS_PATH=C:\Program Files\Microsoft Visual Studio\%%v\%%e\VC\Auxiliary\Build\vcvars64.bat"
            goto :found_vs
        )
        if exist "C:\Program Files (x86)\Microsoft Visual Studio\%%v\%%e\VC\Auxiliary\Build\vcvars64.bat" (
            set "VS_PATH=C:\Program Files (x86)\Microsoft Visual Studio\%%v\%%e\VC\Auxiliary\Build\vcvars64.bat"
            goto :found_vs
        )
    )
)

if "%VS_PATH%"=="" (
    echo [ERROR] Visual Studio vcvars64.bat not found. 
    echo Please install Visual Studio 2019/2022 with "Desktop development with C++".
    exit /b 1
)
:found_vs
echo [INFO] Using Visual Studio: %VS_PATH%

:: 3. Setup Build Environment
call "%VS_PATH%"

:: 4. Create Build Directory
if not exist "build" mkdir build

:: 5. Compile C++ JNI
echo [INFO] Compiling native\src\faststt.cpp...
cl /LD /EHsc /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" /I"native\include" ^
   native\src\faststt.cpp ^
   /Fe:build\faststt.dll ^
   /link /DEF:native\faststt.def user32.lib

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Native DLL built: build\faststt.dll
echo ========================================
