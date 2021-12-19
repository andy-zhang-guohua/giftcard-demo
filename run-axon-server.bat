@title Axon Server
@echo off
:: set JAVA_HOME=D:\programs\jdk-17_windows-x64_bin\jdk-17.0.1
set java="%JAVA_HOME%\bin\java"

%java% -version

%java% -jar D:\idea_wks\giftcard-demo\AxonServer\axonserver-4.5.9.jar
cmd /k