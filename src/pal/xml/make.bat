@rem make just this directory

set CLASSPATH=
javac -deprecation -classpath ../../../classes;../../../classes/xml.jar -d ../../../classes *.java
pause
