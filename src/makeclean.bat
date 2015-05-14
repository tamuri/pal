@rem
@rem make in order
cd ..\classes
deltree pal
deltree org

cd ..\src\org\w3c\dom
javac -source 1.3 -classpath ../../../../classes -d ../../../../classes *.java
cd ..\..\..\pal\io
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\math
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\util
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\misc
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\datatype
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java

cd ..\statistics
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\substmodel
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\mep
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\alignment
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\distance
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\tree
javac -source 1.3 -deprecation -classpath ../../../classes -d ../../../classes *.java
cd ..\coalescent
javac -source 1.3 -deprecation -classpath ../../../classes -d ../../../classes *.java
cd ..\popgen
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\eval
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\gui
javac -source 1.3 -classpath ../../../classes -d ../../../classes *.java
cd ..\xml
javac -source 1.3 -classpath ../../../classes;../../../classes/xml.jar -d ../../../classes *.java
