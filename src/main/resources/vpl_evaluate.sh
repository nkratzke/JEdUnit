cat > vpl_execution <<EEOOFF
#! /bin/bash

rm -f *.class
rm -f *.jar
wget --quiet https://github.com/nkratzke/VPL-java-template/raw/working/build/libs/JEdUnit.jar -O JEdUnit.jar
wget --quiet https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.14/checkstyle-8.14-all.jar -O checkstyle.jar

java  -jar checkstyle.jar -c style_checks.xml *.java > checkstyle.log
javac -cp ".:*" -Xlint:none Checks.java
java  -cp ".:*" Checks
EEOOFF

chmod +x vpl_execution
