@rem make in order
cd ..\classes
deltree pal
cd ..\src\pal
cd io
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\math
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\util
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\misc
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\mep
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\datatype
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\statistics
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\substmodel
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\alignment
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\distance
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\tree
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\coalescent
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\eval
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
cd ..\gui
jikes -classpath .;C:\jdk1.1.8\lib\classes.zip;../../../classes -d ../../../classes *.java
