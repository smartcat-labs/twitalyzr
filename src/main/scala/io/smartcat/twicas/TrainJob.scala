package io.smartcat.twicas

import io.smartcat.twicas.models.LogisticRegressionTweet
import io.smartcat.twicas.pipeline.PipelineProcessor
import io.smartcat.twicas.preprocessing._
import io.smartcat.twicas.summary.ModelSummary
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


  val textCleaner = new TextCleaner(List("text", "userDescription"))
  val tokenizer = FeatureTokenizer.make(List("text", "userDescription"))
  val hashingTF = FeatureHashTF.make(Map("text_t" -> 100, "userDescription_t" -> 100))
  val pipelineProcesorToTF = new PipelineProcessor(List(textCleaner, tokenizer, hashingTF))
  val result = pipelineProcesorToTF.processAll(df)

  val idf = FeatureIDF.make(result, List("text_t_tf", "userDescription_t_tf"))

  val assembler = FeatureAssembler.make(List("text_t_tf_idf", "userDescription_t_tf_idf"))

  val preprocessPipelinePreprocess = new PipelineProcessor(List(textCleaner, tokenizer, hashingTF, idf, assembler))
  val res = preprocessPipelinePreprocess.processAll(df)

  res.select("features").show(false)

  val model = LogisticRegressionTweet.train(res)

  val summary = ModelSummary.crossValidation(res,model)

  println("\n")
  println("True Positive "+summary.tp)
  println("True Negative "+summary.tn)
  println("False positive "+summary.fp)
  println("False negative "+summary.fn)
  println("Precision "+summary.precision)
  println("Recall "+summary.recall)
  println("Accuracy "+summary.accuracy)
  println("\n")

  println(summary.report)

  //val predicted = model.classify(res)

  //predicted.select("prediction").show(false)

}
