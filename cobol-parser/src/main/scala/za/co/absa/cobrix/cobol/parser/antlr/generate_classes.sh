#! /usr/bin/bash

# generate java files
antlr4 *g4 -no-listener -visitor

# add the package info
sed -i '1 s/^/package za.co.absa.cobrix.cobol.parser.antlr;\n\n/g' *.java
