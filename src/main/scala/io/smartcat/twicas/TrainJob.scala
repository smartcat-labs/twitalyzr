package io.smartcat.twicas

import org.apache.spark.sql.SparkSession

/**
  * Created by stanko on 26.12.16..
  */
object TrainJob extends App{
  val spark = SparkSession.builder()
    .appName("twitter_trainer")
    .getOrCreate()

  val ar = spark.sparkContext.parallelize(Array[Int](1, 2, 3, 4, 5, 6, 7))
  ar.collect().foreach(println)
}
