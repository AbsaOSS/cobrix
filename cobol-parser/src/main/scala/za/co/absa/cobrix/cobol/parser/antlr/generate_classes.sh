#! /usr/bin/bash

# generate java files
antlr4 *g4 -no-listener -visitor

LICENSE="$(cat license.txt)"

for f in *java
do
    DATA="$(cat ${f})"
    # add the license
    echo "${LICENSE}" > "$f"
    # add the package info
    printf "\n\npackage za.co.absa.cobrix.cobol.parser.antlr;\n\n" >> "$f"
    echo "${DATA}" >> "$f"
done
