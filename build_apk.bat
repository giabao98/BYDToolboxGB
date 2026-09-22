@echo off
set "JAVA_HOME=C:\My Data\jdk-17\jdk-17.0.12+7"
set "ANDROID_HOME=C:\My Data\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

call gradlew.bat assembleDebug
