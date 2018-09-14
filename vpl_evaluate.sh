cat > vpl_execution <<EEOOFF
#! /bin/bash
javac Checks.java
java Checks
EEOOFF

chmod +x vpl_execution
