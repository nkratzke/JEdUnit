#! /bin/bash
wget --quiet https://github.com/nkratzke/VPL-java-template/raw/working/build/libs/JEdUnit.jar -O jedunit.jar
wget --quiet https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.14/checkstyle-8.14-all.jar -O checkstyle.jar
base64 -i jedunit.jar -o jedunit.jar.b64
base64 -i checkstyle.jar -o checkstyle.jar.b64
java  -jar JEdUnit.jar
