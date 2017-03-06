package io.smartcat.twitalyzr.util

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object Conf {
  lazy val textColumn: String = root.getString("text_column")
  lazy val userDescriptionColumn: String = root.getString("user_description_column")
  lazy val hashtagsColumn: String = root.getString("hashtags_column")
  lazy val idColumn: String = root.getString("id_column")
  lazy val trainJob: String = root.getString("train_job_name")
  lazy val classifyJob: String = root.getString("classify_job_name")

  private lazy val root = config.getConfig("twitalyzr")
  private val config = ConfigFactory.load()

  private def getIntList(conf: Config): List[Int] = {
    if (conf.hasPath("manual"))
      conf.getIntList("manual").asScala.map(_.toInt).toList
    else
      (conf.getInt("from") to conf.getInt("to") by conf.getInt("by")).toList
  }

  private def getDoubleList(conf: Config): List[Double] = {
    if (conf.hasPath("manual"))
      conf.getDoubleList("manual").asScala.map(_.toDouble).toList
    else
      (conf.getDouble("from") to conf.getDouble("to") by conf.getDouble("by")).toList
  }

  object SendResults {
    lazy val maxTweets: Int = send.getInt("max_tweets")
    lazy val host: String = send.getString("host")
    private lazy val send = root.getConfig("send_results")
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
    lazy val numFold: Int = train.getInt("num_fold")

    private lazy val train = root.getConfig("train")

    object LogReg {
      lazy val regParams: List[Double] = getDoubleList(logreg.getConfig("reg_param"))
      lazy val elasticNet: List[Double] = getDoubleList(logreg.getConfig("elastic_net"))
      private lazy val logreg = train.getConfig("log_reg")
    }

    object NaiveBayes {
      lazy val smoothings: List[Int] = getIntList(nb.getConfig("smoothing"))
      private lazy val nb = train.getConfig("naive_bayes")
    }

    object RandomForest {
      lazy val subsetStrategy: String = rf.getString("subset_strategy")
      lazy val impurity: String = rf.getString("impurity")
      lazy val numClasses: Int = rf.getInt("num_classes")
      lazy val seed: Int = rf.getInt("seed")
      lazy val subsamplingRate: Double = rf.getDouble("subsampling_rate")
      lazy val maxBins: List[Int] = getIntList(rf.getConfig("max_bins"))
      lazy val maxDepths: List[Int] = getIntList(rf.getConfig("max_depths"))
      lazy val numTrees: List[Int] = getIntList(rf.getConfig("num_trees"))
      private lazy val rf = train.getConfig("random_forest")
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
      lazy val afterCountVectorizer: String = sufixes.getString("after_count_vectorizer")
      lazy val afterClean: String = sufixes.getString("after_clean")

      private lazy val sufixes = preprocessing.getConfig("sufixes")

    }

    object TermFrequency {
      lazy val text: List[Int] = getIntList(tf.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(tf.getConfig("user_description"))
      lazy val hashtags: List[Int] = getIntList(tf.getConfig("hashtags"))
      private lazy val tf = preprocessing.getConfig("tf")

    }

    object Word2Vector {
      lazy val text: List[Int] = getIntList(w2v.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(w2v.getConfig("user_description"))
      private lazy val w2v = preprocessing.getConfig("w2v")
    }

    object NGram {
      lazy val text: List[Int] = getIntList(ngram.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(ngram.getConfig("user_description"))
      lazy val textVectorLength: List[Int] = getIntList(ngram.getConfig("text").getConfig("tf"))
      lazy val userDescriptionVectorLength: List[Int] = getIntList(ngram.getConfig("user_description").getConfig("tf"))
      private lazy val ngram = preprocessing.getConfig("ngram")
    }

    object CountVectorizer {
      lazy val minDocumentFrequency: Int = cv.getInt("min_doc_freq")
      lazy val minTermFrequency: Int = cv.getInt("min_term_freq")
      lazy val text: List[Int] = getIntList(cv.getConfig("text"))
      lazy val userDescription: List[Int] = getIntList(cv.getConfig("user_description"))
      lazy val hashtags: List[Int] = getIntList(cv.getConfig("hashtags"))
      private lazy val cv = preprocessing.getConfig("count_vectorizer")
    }

  }

  object Stream {

    lazy val token: String = stream.getString("token")
    lazy val tokenSecret: String = stream.getString("tokenSecret")
    lazy val consumerKey: String = stream.getString("consumerKey")
    lazy val consumerSecret: String = stream.getString("consumerSecret")
    lazy val searchFilter: String = stream.getString("search_filter")
    lazy val interval: Int = stream.getInt("interval")
    private lazy val stream = root.getConfig("twitter_stream")

  }

}
