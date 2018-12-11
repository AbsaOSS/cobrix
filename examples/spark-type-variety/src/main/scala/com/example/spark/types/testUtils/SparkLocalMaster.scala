package com.example.spark.types.testUtils

trait SparkLocalMaster {
  System.getProperties.setProperty("spark.master", "local[*]")
}
