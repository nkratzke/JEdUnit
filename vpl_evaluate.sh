cat > vpl_execution <<EEOOFF
#! /bin/bash

wget --quiet http://central.maven.org/maven2/com/github/javaparser/javaparser-core/3.6.27/javaparser-core-3.6.27.jar

javac -cp ".:*" Checks
java  -cp ".:*" Checks
EEOOFF

chmod +x vpl_execution
