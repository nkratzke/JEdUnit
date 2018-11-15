#! /bin/bash
cat > vpl_execution <<EEOOFF
#! /bin/bash
. common_script.sh

rm -f *.class

java  -jar checkstyle.jar -c style_checks.xml *.java > checkstyle.log
javac -cp ".:*" -Xlint:none Checks.java
java  -cp ".:*" Checks
EEOOFF

chmod +x vpl_execution
