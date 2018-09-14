cat > vpl_execution <<EEOOFF
#! /bin/bash
javac *.java
java Checks
EEOOFF

chmod +x vpl_execution
