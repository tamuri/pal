@rem
@rem make in order (contributed by Thomas Keane)
mkdir ..\classes
cd ..\classes
deltree pal
deltree org

cd ..\src\org\w3c\dom
javac -classpath ../../../../classes -d ../../../../classes *.java
cd ..\..\..\pal\io
javac -classpath ../../../classes -d ../../../classes *.java
cd ..
javac -classpath ../../classes -d ../../classes math/*.java util/*.java misc/*.java
cd datatype
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\statistics
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\alignment
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\
javac -classpath ../../classes -d ../../classes tree/*.java distance/*.java mep/*.java substmodel/*.java
cd coalescent
javac -deprecation -classpath ../../../classes -d ../../../classes *.java
cd ..\popgen
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\eval
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\gui
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\xml
javac -classpath ../../../classes;../../../classes/xml.jar -d ../../../classes *.java
cd ..\algorithmics
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\treesearch
javac -classpath ../../../classes -d ../../../classes *.java
cd ..\supgma
javac -classpath ../../../classes -d ../../../classes *.java
pause