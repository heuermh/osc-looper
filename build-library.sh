#!/bin/bash

mkdir osclooper
mkdir osclooper/library
cp COPYING osclooper
cp README osclooper
cp -R src osclooper
cp -R examples osclooper
cd src
javac -classpath "../lib/oscP5.jar" osclooper/*.java
jar cvf ../osclooper/library/osclooper.jar osclooper/*.class
cd ..