package io.smartcat.twicas.training

import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

import io.smartcat.twicas.models
import io.smartcat.twicas.models.LogisticRegressionTweet
import io.smartcat.twicas.pipeline.PipelineProcessor
import io.smartcat.twicas.summary.{ModelSummary, ParameterOptimization}
import io.smartcat.twicas.training.preprocess.NGrams
import io.smartcat.twicas.util.Conf
import org.apache.spark.sql.DataFrame

object LogRegNGramCV {
  val reportBase = Conf.Train.reportDir

  val headerShape = "LOGREG COUNT VECTORIZER:\nTEXT_SIZE: %s\nUSER_SIZE: %s\nHASHTAGS: %s\n" +
    "NGRAM_TEXT: %s\nNGRAM_TEXT_VEC: %s\n" +
    "NGRAM_USER: %s\nNGRAM_USER_VEC: %s\n"

  /**
    * Make all models and choose one with best results on validation set
    * @param df DataFrame train+validation set
    * @param text all possible vector size for tweet text
    * @param user all possible vector size for tweet user description
    * @param nGramText all possible ngrams for tweet text
    * @param nGramUser all possible ngrams for user description
    * @param hashtags all possible vector size for hashtags
    * @param thresholds all possible probability thresholds for classifier
    * @param regParams all possible regularization parameters for logistic regression classifier
    * @param elasticParams all possible elastic net regularization parameters for logistic regression classifier
    * @return Best pipeline model
    */
  def trainAndReport(df: DataFrame, text: List[Int], user: List[Int], nGramText: Map[String, List[Int]],
                     nGramUser: Map[String, List[Int]], hashtags: List[Int],
                     thresholds: List[Double], regParams: List[Double], elasticParams: List[Double]): PipelineProcessor = {

    val count = new AtomicInteger()

    def getBestAndReport(text: Int, user: Int, hashtags: Int,
                         nGramText: Int, nGramTextVecSize: Int, nGramUser: Int, nGramUserVecSize: Int): (PipelineProcessor, ModelSummary) = {

      val filename = reportBase + count.incrementAndGet().toString + ".txt"
      val header = headerShape.format(text.toString, user.toString, hashtags.toString, nGramText.toString,
        nGramTextVecSize.toString, nGramUser.toString, nGramUserVecSize.toString)

      val pipeline = NGrams.withWordCounter(df, text, user, hashtags, nGramText, nGramUser, nGramTextVecSize, nGramUserVecSize)

      val parameterOptimization = trainWithPreprocess(pipeline)

      new PrintWriter(filename) {
        write(header + "\n" + parameterOptimization.report((model: ModelSummary) => model.fMeasure));
        close
      }

      val best = parameterOptimization.getKBest((model: ModelSummary) => model.fMeasure).head

      (pipeline, best)

    }

    def trainWithPreprocess(pipelineProcessor: PipelineProcessor): ParameterOptimization = {

      val processedDF = pipelineProcessor.processAll(df)

      val splitedDF = processedDF.randomSplit(Conf.Train.trainSplit, Conf.Train.splitSeed)
      val trainDF = splitedDF(0).cache()
      val validationDF = splitedDF(1).cache()

      LogisticRegressionTweet.makeModels(trainDF, validationDF, thresholds, regParams, elasticParams)

    }

    val parametersPreproc = models.generator(List(text, user, hashtags,
      nGramText("ngram"), nGramText("size"), nGramUser("ngram"), nGramUser("size")))

    val besties = parametersPreproc.map(parameters => getBestAndReport(parameters.head, parameters(1), parameters(2),
      parameters(3), parameters(4), parameters(5), parameters(6))
    )

    println("\n\nBESTIES"+besties)

    val bestOne = besties.maxBy { case (_, ms) => ms.fMeasure }

    new PipelineProcessor(bestOne._1.pipelines ++ List(bestOne._2.classificationModel))

  }
}

