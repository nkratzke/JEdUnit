cat > vpl_execution <<EEOOFF
#! /bin/bash 
rm *.class
javac  Main.java
java Main
EEOOFF

chmod +x vpl_execution
