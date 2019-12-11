/*
 * Copyright 2018-2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name := "spark-cobol-app"

version := "0.0.1"

organization := "com.example"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.4"
val sparkCobolVersion = "2.0.0"
val scalatestVersion = "3.0.1"

libraryDependencies ++= Seq(
  "za.co.absa.cobrix" %% "spark-cobol"     % sparkCobolVersion,
  "org.scalatest"     %% "scalatest"       % scalatestVersion   % Test,
  "org.apache.spark"  %% "spark-core"      % sparkVersion       % Provided,
  "org.apache.spark"  %% "spark-sql"       % sparkVersion       % Provided,
  "org.apache.spark"  %% "spark-streaming" % sparkVersion       % Provided,
  "org.scala-lang"    %  "scala-library"   % scalaVersion.value % Provided
)

parallelExecution in Test := false

