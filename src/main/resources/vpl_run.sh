#! /bin/bash
rm -f *.class
javac -encoding UTF-8 -cp ".:*" Main.java

cat > vpl_execution <<EEOOFF
#! /bin/bash
java -cp ".:*"  Main
EEOOFF

chmod +x vpl_execution
