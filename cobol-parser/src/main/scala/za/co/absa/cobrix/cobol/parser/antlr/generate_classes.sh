#! /usr/bin/bash

# Copyright 2018 ABSA Group Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
