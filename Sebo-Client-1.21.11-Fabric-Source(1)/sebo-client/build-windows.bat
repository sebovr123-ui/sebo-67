@echo off
cd /d "%~dp0"
where java >nul 2>&1 || (echo Install Java 21 first. & pause & exit /b 1)
where gradle >nul 2>&1 || (echo Install Gradle 9.2.1 and add its bin directory to PATH. & pause & exit /b 1)
call gradle build --no-daemon
if errorlevel 1 (echo Build failed. Read the error above. & pause & exit /b 1)
echo Built JAR: build\libs\sebo-client-0.1.0.jar
pause
