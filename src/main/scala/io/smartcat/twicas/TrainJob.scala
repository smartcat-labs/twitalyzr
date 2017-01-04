package io.smartcat.twicas

import io.smartcat.twicas.preprocessing.models._
import io.smartcat.twicas.preprocessing.pipeline.PipelineProcessor
import io.smartcat.twicas.tweet.Tweet
import org.apache.spark.sql.SparkSession

object TrainJob extends App {
  val spark = SparkSession.builder()
    .appName("twitter_trainer")
    .getOrCreate()

  //This is just for testing, whole dataset will be added later in project
  val filename = "/home/stanko/Documents/dataset_parts/02_11_2016/raw_labeled.json"

  val jsonDF = spark.read.json(filename)

  val rdd = jsonDF.rdd.map(Tweet.makeJsonRow)

  import spark.sqlContext.implicits._

  val df = rdd.toDF

  val pipelineProcesor = new PipelineProcessor()
  val textCleaner = new TextCleaner(List("text", "userDescription"))
  val tokenizer = FeatureTokenizer.make(List("text", "userDescription"))
  val hashingTF = FeatureHashTF.make(Map("text_t" -> 100, "userDescription_t" -> 100))
  val result = pipelineProcesor.processAll(df, List(textCleaner, tokenizer, hashingTF))
  result.show

  val idf = FeatureIDF.make(result, List("text_t_tf", "userDescription_t_tf"))

  val preprocessPipeline = pipelineProcesor.processAll(df, List(textCleaner, tokenizer, hashingTF, idf))
  preprocessPipeline.show

}
