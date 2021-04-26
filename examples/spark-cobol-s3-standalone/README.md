# Spark Job Application that uses S3 as a storage example

This is an example Apache Spark Application that can access S3 storage even when running in local mode.

## Usage

The example application is in `com.example.spark.cobol.app` package. 
The project contains build files for `Maven`.

## Maven

**To run this locally use an IDE**

**To build an uber jar to run on cluster**
```
mvn package
```

## Running via spark-submit

After the project is packaged you can copy `target/spark-cobol-s3-0.0.1-SNAPSHOT.jar` 
to an edge node of a cluster and use `spark-submit` to run the job. Here us an example when running on Yarn:

```sh
spark-submit --num-executors 2 --executor-memory 4g --executor-cores 2 --master yarn --deploy-mode client --driver-cores 4 --driver-memory 4G --conf 'spark.yarn.executor.memoryOverhead=512' --class com.example.spark.cobol.app.SparkCobolS3App spark-cobol-s3-0.0.1-SNAPSHOT.jar
```

## Running via spark-shell

To run a spark shell with cobol files support on cluster after the project is packaged you can copy 'target/spark-cobol-s3-0.0.1-SNAPSHOT.jar'
to an edge node of a cluster

```sh
spark-shell --jars spark-cobol-s3-0.0.1-SNAPSHOT.jar
```


### Running Spark Applications in local mode from an IDE
If you try to run the example from an IDE you'll likely get the following exception: 

```Exception in thread "main" java.lang.NoClassDefFoundError: scala/collection/Seq```

This is because the generated jar does not contain dependencies that have 'Provided' scope. This is done so that the uber
jar for `spark-submit` is not too big and does not contain cluster-specific Hadoop and Spark libraries.

To run the job from an IDE use `include dependencies with "Provided" scope` flag when running the example.

Alternatively, you can switch `provided` dependencies to `compile` in the POM file and run Saprk Applications as a normal JVM App.
