@title GiftCard Demo
@echo off
:: set JAVA_HOME=D:\programs\jdk-17_windows-x64_bin\jdk-17.0.1
set java="%JAVA_HOME%\bin\java"

%java% -version

%java% -jar target\giftcard-demo-4.5.jar

cmd /k