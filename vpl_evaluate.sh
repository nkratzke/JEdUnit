cat > vpl_execution <<EEOOFF
#! /bin/bash

rm *.jar
rm *_checks.xml*
wget --quiet http://central.maven.org/maven2/com/github/javaparser/javaparser-core/3.6.27/javaparser-core-3.6.27.jar
wget --quiet https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.14/checkstyle-8.14-all.jar
wget --quiet https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/main/resources/sun_checks.xml

java  -jar checkstyle-8.14-all.jar -c sun_checks.xml *.java > checkstyle.log
javac -cp ".:*" *.java
java  -cp ".:*" Checks
EEOOFF

chmod +x vpl_execution
