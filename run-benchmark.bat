@echo off
echo Building Main Project...
call compile.bat
call mvn clean package -DskipTests -q

echo Building Benchmark Uber-JAR...
cd examples\Benchmark
call mvn clean package -DskipTests -q

echo Running JMH Benchmarks...
java -cp "target\benchmarks.jar;..\..\target\FastSTT-0.1.2.jar;%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.0\FastCore-0.1.0.jar;%USERPROFILE%\.m2\repository\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar" org.openjdk.jmh.Main -f 1 -i 2 -wi 1 -w 1s -r 1s

cd ..\..
pause
