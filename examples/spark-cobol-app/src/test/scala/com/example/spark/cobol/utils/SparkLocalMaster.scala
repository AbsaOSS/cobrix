package com.example.spark.cobol.utils

trait SparkLocalMaster {
  System.getProperties.setProperty("spark.master", "local[*]")
}
