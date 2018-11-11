cat > vpl_execution <<EEOOFF
#! /bin/bash 
rm -f *.class
javac -cp ".:*" Main.java
java -cp ".:*"  Main
EEOOFF

chmod +x vpl_execution
