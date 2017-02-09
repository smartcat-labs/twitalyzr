package io.smartcat.twicas

import io.smartcat.twicas.models.ClassificationModel
import io.smartcat.twicas.pipeline.PipelineProcessor
import io.smartcat.twicas.summary.ModelSummary
import io.smartcat.twicas.training.LogRegNGramCV
import io.smartcat.twicas.tweet.DatasetLoader
import io.smartcat.twicas.util.Conf
import org.apache.spark.sql.SparkSession

object TrainJob extends App {
  val spark = SparkSession.builder()
    .appName("twitter_trainer")
    .getOrCreate()

  val df = DatasetLoader.load(Conf.Train.dataset, spark)

  val textNGram = Map(
    "ngram" -> Conf.Preprocessing.NGram.text,
    "size" -> Conf.Preprocessing.NGram.textVectorLength
  )

  val userNGram = Map(
    "ngram" -> Conf.Preprocessing.NGram.userDescription,
    "size" -> Conf.Preprocessing.NGram.userDescriptionVectorLength
  )

  val model = LogRegNGramCV.trainAndReport(df, Conf.Preprocessing.CountVectorizer.text,
    Conf.Preprocessing.CountVectorizer.userDescription, textNGram, userNGram, Conf.Preprocessing.CountVectorizer.hashtags,
    Conf.Train.thresholds, Conf.Train.LogReg.regParams, Conf.Train.LogReg.elasticNet)

}
