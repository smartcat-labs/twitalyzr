package io.smartcat.twicas

import io.smartcat.twicas.tweet.Tweet
import org.apache.spark.sql.SparkSession

/**
  * Created by stanko on 26.12.16..
  */
object TrainJob extends App{
  val spark = SparkSession.builder()
    .appName("twitter_trainer")
    .getOrCreate()

  val filename = ""

  val jsonDF = spark.read.json(filename)

  val rdd = jsonDF.rdd.map(Tweet.makeJsonRow)

  import spark.sqlContext.implicits._
  val df = rdd.toDF

  df.show

}
