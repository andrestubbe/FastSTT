@echo off
setlocal enabledelayedexpansion

echo ========================================
echo FastSTT Native Library Builder (AVX2)
echo ========================================

set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installerswhere.exe"
if not exist "%VSWHERE%" set "VSWHERE=%ProgramFiles%\Microsoft Visual Studio\Installerswhere.exe"

if exist "%VSWHERE%" (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VS_PATH=%%i"
    )
)

if not defined VS_PATH (
    if exist "C:\Program Files\Microsoft Visual Studio\18\Community" set "VS_PATH=C:\Program Files\Microsoft Visual Studio\18\Community"
)

if not defined VS_PATH (
    echo [ERROR] Visual Studio with C++ tools not found!
    exit /b 1
)

echo Found Visual Studio at: %VS_PATH%

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-25.0.3" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.3"
    ) else if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

echo Using JAVA_HOME: %JAVA_HOME%

@call "%VS_PATH%\VC\Auxiliary\Build\vcvarsall.bat" x64 > nul

if not exist "build" mkdir build > nul 2>&1
if not exist "src\main\resources\native" mkdir "src\main\resources\native" > nul 2>&1
if not exist "src\main\resources\win32-x86-64" mkdir "src\main\resources\win32-x86-64" > nul 2>&1
if not exist "target\classes\native" mkdir "target\classes\native" > nul 2>&1

cl.exe /nologo /O2 /arch:AVX2 /EHsc /std:c++17 /MD /LD /D_CRT_SECURE_NO_WARNINGS ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    native\src\faststt.cpp ^
    /Fo:build\faststt.obj ^
    /link /DLL /OUT:build\faststt.dll user32.lib gdi32.lib shcore.lib advapi32.lib dwmapi.lib > nul

if errorlevel 1 (
    echo [ERROR] Compilation failed!
    exit /b 1
)

copy /Y build\faststt.dll src\main\resources\native\faststt.dll > nul
copy /Y build\faststt.dll src\main\resources\win32-x86-64\faststt.dll > nul
copy /Y build\faststt.dll target\classes\native\faststt.dll > nul

echo [SUCCESS] FastSTT AVX2 C++ Native Library built.
