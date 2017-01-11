package io.smartcat.twicas

import io.smartcat.twicas.preprocessing.models._
import io.smartcat.twicas.preprocessing.pipeline.PipelineProcessor
import io.smartcat.twicas.tweet.Tweet
import org.apache.spark.sql.SparkSession

import scala.io.Source

object TrainJob extends App {
  val spark = SparkSession.builder()
    .appName("twitter_trainer")
    .getOrCreate()

  val filename = "/raw_labeled.json"

  val resource = TrainJob.getClass.getResourceAsStream(filename)
  val lines = Source.fromInputStream(resource).mkString

  val rddJson = spark.sparkContext.parallelize(Seq(lines))
  val dfJson = spark.sqlContext.read.json(rddJson)

  val rdd = dfJson.rdd.map(Tweet.makeJsonRow)

  import spark.sqlContext.implicits._

  val df = rdd.toDF

  val pipelineProcesor = new PipelineProcessor()
  val textCleaner = new TextCleaner(List("text", "userDescription"))
  val tokenizer = FeatureTokenizer.make(List("text", "userDescription"))
  val hashingTF = FeatureHashTF.make(Map("text_t" -> 100, "userDescription_t" -> 100))
  val result = pipelineProcesor.processAll(df, List(textCleaner, tokenizer, hashingTF))

  val idf = FeatureIDF.make(result, List("text_t_tf", "userDescription_t_tf"))

  val preprocessPipeline = pipelineProcesor.processAll(df, List(textCleaner, tokenizer, hashingTF, idf))

}
