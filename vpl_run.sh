cat > vpl_execution <<EEOOFF
#! /bin/bash 
javac  Main.java
java Main
EEOOFF

chmod +x vpl_execution