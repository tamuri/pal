@echo off
rem ************************************************************
rem This batch file makes and signs the jar file
rem ************************************************************
@echo on 

cd ..\classes

jar cvf ..\pal.jar pal org *.class
jarsigner ..\pal.jar alexei
