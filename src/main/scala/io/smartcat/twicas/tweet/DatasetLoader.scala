package io.smartcat.twicas.tweet

import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.io.Source

class DatasetLoader {

  def load(filename: String, spark: SparkSession): DataFrame = {
    import spark.sqlContext.implicits._

    val resource = this.getClass.getResourceAsStream(filename)
    val lines = Source.fromInputStream(resource).mkString
    val rddJson = spark.sparkContext.parallelize(Seq(lines))
    val dfJson = spark.sqlContext.read.json(rddJson)
    val rdd = dfJson.rdd.map(Tweet.makeJsonRow)

    rdd.toDF
  }

}