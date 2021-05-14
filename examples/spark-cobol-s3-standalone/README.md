# Spark Job Application that uses S3 as a storage example

This is an example Apache Spark Application that can access S3 storage.

It uses Hadoop 3.2.0 to enable role-based authentication with Spark 2.4.7.
It should work with different Spark/Hadoop combinations as well.

This example is designed to run in local mode. You don't need a Spark cluster or AWS EMR to run this example.

## Usage

The example application is in `com.example.spark.cobol.app` package. 
The project contains build files for `Maven`.

## Maven

**To build the uber jar to run on any local computer**
```
mvn package
```

## Running

After the project is packaged you can copy `target/spark-cobol-s3-sa-0.0.1-SNAPSHOT.jar`.

The uber jar contains all dependencies, including Hadoop, AWS SDK and Spark libraries needed to run the application.

Use the following command to run it:

```sh
java -cp java -cp spark-cobol-s3-sa-0.0.1-SNAPSHOT.jar com.example.spark.cobol.app.SparkCobolS3App
```

or, since the main class is registered in the manifest, you can run the job even simpler:
```sh
java -jar spark-cobol-s3-sa-0.0.1-SNAPSHOT.jar
```

Alternatively, you can run the application from an IDE.
