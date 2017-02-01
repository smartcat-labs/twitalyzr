package io.smartcat.twicas.util

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object Conf {
  lazy val textColumn: String = root.getString("text_column")
  lazy val userDescriptionColumn: String = root.getString("user_description_column")
  lazy val hashtagsColumn: String = root.getString("hashtags_column")
  lazy val trainJob: String = root.getString("train_job_name")
  lazy val classifyJob: String = root.getString("classify_job_name")
  private lazy val root = config.getConfig("twicas")
  private val config = ConfigFactory.load()

  private def getIntList(config: Config): List[Int] = {
    val manual = config.getIntList("manual").asScala
    if (manual.isEmpty) {
      (config.getInt("from") to config.getInt("to") by config.getInt("by")).toList
    } else
      manual.map(x => x.toInt).toList
  }

  private def getDoubleList(conf: Config): List[Double] = {
    val manual = config.getDoubleList("manual").asScala
    if (manual.isEmpty) {
      (config.getDouble("from") to config.getDouble("to") by config.getDouble("by")).toList
    } else
      manual.map(x => x.toDouble).toList
  }

  object Train {
    lazy val dataset: String = train.getString("dataset_file")
    lazy val reportDir: String = train.getString("report_base_dir")
    lazy val trainSplit = Array(train.getDouble("train_split"), train.getDouble("validation_split"))
    lazy val splitSeed: Int = train.getInt("split_seed")
    lazy val thresholds: List[Double] = getDoubleList(train.getConfig("threshold"))
    lazy val featuresColumn: String = train.getString("features_column")
    lazy val labelColumn: String = train.getString("label_column")
    lazy val predictionColumn: String = train.getString("prediction_column")
    lazy val probabilityColumn: String = train.getString("probability_column")

    private lazy val train = root.getConfig("train")

    object LogReg {
      lazy val regParams: List[Double] = getDoubleList(logreg.getConfig("reg_param"))
      lazy val elasticNet: List[Double] = getDoubleList(logreg.getConfig("elastic_net"))
      private lazy val logreg = train.getConfig("log_reg")
    }

  }

  object Preprocessing {
    lazy val outputColumn: String = preprocessing.getString("output_col")
    private lazy val preprocessing = root.getConfig("preprocessing")

    object Sufix {
      lazy val afterStopWord: String = sufixes.getString("after_stop_word")
      lazy val afterTokenizer: String = sufixes.getString("after_tokenizer")
      lazy val afterTF: String = sufixes.getString("after_tf")
      lazy val afterIDF: String = sufixes.getString("after_idf")
      lazy val afterW2V: String = sufixes.getString("after_w2v")
      lazy val afterNGram: String = sufixes.getString("after_ngram")
      private lazy val sufixes = preprocessing.getConfig("sufixes")

    }

    object TF {
      lazy val text: List[Int] = getIntList(tf.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(tf.getConfig("user_description"))
      lazy val hashtags: List[Int] = getIntList(tf.getConfig("hashtags"))
      private lazy val tf = preprocessing.getConfig("tf")

    }

    object W2V {
      lazy val text: List[Int] = getIntList(w2v.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(w2v.getConfig("user_description"))
      private lazy val w2v = preprocessing.getConfig("w2v")
    }

    object NGram {
      lazy val text: List[Int] = getIntList(ngram.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(ngram.getConfig("user_description"))
      lazy val text_tf: List[Int] = getIntList(ngram.getConfig("text").getConfig("tf"))
      lazy val userDescription_tf: List[Int] = getIntList(ngram.getConfig("user_description").getConfig("tf"))
      private lazy val ngram = preprocessing.getConfig("ngram")
    }

  }

}
