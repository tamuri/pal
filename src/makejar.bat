@echo off
rem ************************************************************
rem This batch file makes an unsigned jar file
rem ************************************************************
@echo on 

cd ..\classes

jar cvf ..\pal.jar pal org *.class
