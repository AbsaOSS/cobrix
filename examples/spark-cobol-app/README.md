# Spark Job Application Example

This is an example Apache Spark Application that can be used for creating other Spark projects.
It includes all dependencies in a 'fat' jar to run the job locally and on cluster.

Here is the list of Spark Application examples:

- `SparkCobolApp` An example that demonstrates how a multisegment mainframe file can be read in Spark.
- `SparkTypesApp` An example fixed record length EBCDIC file reader Spark with variety of data types supported.
- `SparkCodecApp` An example usage of a custom record header parser. This application reads a variable
  record length file having non-standard RDW headers. In this example RDH header is 5 bytes instead of 4
- `SparkCobolHierarchical` An example processing of an EBCDIC multisegment file extracted from a hierarchical database.

The project generates source and javadoc jars so it can be used for creating Spark library projects as well.

## Usage

The example application is in `com.example.spark.cobol.app` package. The project contains build files for `sbt` and `Maven`.

## Sbt

**To run this locally use**
```sh
sbt test
```

**To build an uber jar to run on cluster**
```
sbt assembly
```

## Maven

**To run this locally use**
```sh
mvn test
```

**To build an uber jar to run on cluster**
```
mvn package -DskipTests=true
```

## Running via spark-submit

After the project is packaged you can copy `target/spark-cobol-app-0.0.1-SNAPSHOT.jar` (if built using `Maven`) or `target/scala-2.11/spark-cobol-app-assembly-0.1.0-SNAPSHOT.jar` (if built using `sbt`)
to an edge node of a cluster and use `spark-submit` to run the job. Here us an example when running on Yarn:

```sh
spark-submit --num-executors 20 --executor-memory 4g --executor-cores 2 --master yarn --deploy-mode client --driver-cores 4 --driver-memory 4G --conf 'spark.yarn.executor.memoryOverhead=512' --class com.example.spark.cobol.app.SparkCobolApp spark-cobol-app-0.0.1-SNAPSHOT.jar
```

## Running via spark-shell

To run a spark shell with cobol files support on cluster after the project is packaged you can copy 'target/spark-cobol-app-0.0.1-SNAPSHOT.jar'
to an edge node of a cluster

```sh
spark-shell --jars spark-cobol-app-0.0.1-SNAPSHOT.jar
```


### Running Spark Applications in local mode from an IDE
If you try to run the example from an IDE you'll likely get the following exception: 

```Exception in thread "main" java.lang.NoClassDefFoundError: scala/collection/Seq```

This is because the jar is created with all Scala and Spark dependencies removed (using shade plugin). This is done so that the uber jar for `spark-submit` is not too big.

To run the job from an IDE use `Spark{AppName}Runner` test. When running tests all provided dependencies will be loaded.
Right click on a runner and select `Run...`.

Alternatively, you can switch `provided` dependencies to `compile` in the POM file and run Saprk Applications as a normal JVM App.
