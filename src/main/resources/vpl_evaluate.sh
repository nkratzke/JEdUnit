#! /bin/bash
. common_script.sh
rm -f *.class
java  -jar checkstyle.jar -c style_checks.xml *.java > checkstyle.log
javac -cp ".:*" Checks.java

cat > vpl_execution <<EEOOFF
#! /bin/bash
java  -cp ".:*" Checks
EEOOFF

chmod +x vpl_execution
