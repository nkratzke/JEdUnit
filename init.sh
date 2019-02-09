#! /bin/bash
echo "Downloading JEdUnit"
wget --no-cache --quiet https://github.com/nkratzke/JEdUnit/raw/working/build/libs/JEdUnit.jar -O jedunit.jar
echo "Downloading Checkstyle"
wget --no-cache --quiet https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.14/checkstyle-8.14-all.jar -O checkstyle.jar
echo "Initializing assignment"
java -jar jedunit.jar
