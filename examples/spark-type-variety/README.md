# Spark Job Type Variety Example

This is an example Apache Spark Job that can be used for creating other Spark projects. It includes all dependencies to run the job locally and on cluster.
The copybook for this example conststs of varieties of data types supported by Cobrix.

The project generates source and javadoc jars so it can be used for creating Spark library projects as well.

## Usage 

The example application is in `com.example.spark.types.app.SparkTypesApp` object.

**To run this locally use**
```sh
mvn test
```
or change Scala and Spark dependencies from `provided` to `compile`.

**To run this on cluster generate the uber jar by running**
```
mvn package -DskipTests
```
After the project is packaged you can copy 'target/spark-types-app-0.0.1-SNAPSHOT.jar'
to an edge node of a cluster and use `spark-submit` to run the job. Here us an example when running on Yarn:

```sh
spark-submit --num-executors 20 --executor-memory 4g --executor-cores 2 --master yarn --deploy-mode client --driver-cores 4 --driver-memory 4G --conf 'spark.yarn.executor.memoryOverhead=512' --class com.example.spark.types.app.SparkTypesApp spark-types-app-0.0.1-SNAPSHOT.jar
```

**To run a spark shell with cobol files support on cluster**

```
mvn package -DskipTests
```

After the project is packaged you can copy 'target/spark-types-app-0.0.1-SNAPSHOT.jar'
to an edge node of a cluster

```sh
spark-shell --jars spark-types-app-0.0.1-SNAPSHOT.jar
```


### Troubleshooting
If you try to run the example from an IDE you'll likely get the following exception: 

```Exception in thread "main" java.lang.NoClassDefFoundError: scala/collection/Seq```

This is because the jar is created with all Scala and Spark dependencies removed (using shade plugin). This is done so that the uber jar for `spark-submit` is not too big.

To run the job from an IDE use `SparkTypesAppRunner` test. When running tests all provided dependencies will be loaded.
