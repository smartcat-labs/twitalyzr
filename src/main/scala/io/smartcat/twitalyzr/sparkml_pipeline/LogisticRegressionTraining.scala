package io.smartcat.twitalyzr.sparkml_pipeline

import io.smartcat.twitalyzr.tweet.DatasetLoader
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession

object LogisticRegressionTraining {

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      System.err.println("One parameter must be model path")
      System.exit(1)
    }

    val modelPath = args(0)

    val spark = SparkSession.builder()
      .appName("twitter_trainer")
      .getOrCreate()

    val df = DatasetLoader.load(Conf.Train.dataset, spark).randomSplit(Conf.Train.trainSplit, Conf.Train.splitSeed)
    val trainDF = df(0)
    val testDF = df(1)

    trainDF.cache().count
    testDF.cache().count

    val textCleaner = new ClearTextTransformer()
      .setInputCol(Conf.textColumn)
      .setOutputCol(Conf.textColumn + Conf.Preprocessing.Sufix.afterClean)


    val userCleaner = new ClearTextTransformer()
      .setInputCol(Conf.userDescriptionColumn)
      .setOutputCol(Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterClean)


    val textToknizer = new RegexTokenizer()
      .setInputCol(textCleaner.getOutputCol)
      .setOutputCol(textCleaner.getOutputCol + Conf.Preprocessing.Sufix.afterTokenizer)
      .setPattern("""\s+""")

    val userTokenizer = new RegexTokenizer()
      .setInputCol(userCleaner.getOutputCol)
      .setOutputCol(userCleaner.getOutputCol + Conf.Preprocessing.Sufix.afterTokenizer)
      .setPattern("""\s+""")

    val textStopWordRemover = new StopWordsRemover()
      .setInputCol(textToknizer.getOutputCol)
      .setOutputCol(textToknizer.getOutputCol + Conf.Preprocessing.Sufix.afterStopWord)

    val userStopWordRemover = new StopWordsRemover()
      .setInputCol(userTokenizer.getOutputCol)
      .setOutputCol(userTokenizer.getOutputCol + Conf.Preprocessing.Sufix.afterStopWord)

    val textNGram = new NGram()
      .setInputCol(textStopWordRemover.getOutputCol)
      .setOutputCol(textStopWordRemover.getOutputCol + Conf.Preprocessing.Sufix.afterNGram)


    val userNGram = new NGram()
      .setInputCol(userStopWordRemover.getOutputCol)
      .setOutputCol(userStopWordRemover.getOutputCol + Conf.Preprocessing.Sufix.afterNGram)


    val textNGramCV = new CountVectorizer()
      .setMinTF(Conf.Preprocessing.CountVectorizer.minTermFrequency)
      .setMinDF(Conf.Preprocessing.CountVectorizer.minDocumentFrequency)
      .setInputCol(textNGram.getOutputCol)
      .setOutputCol(textNGram.getOutputCol + Conf.Preprocessing.Sufix.afterCountVectorizer)

    val userNGramCV = new CountVectorizer()
      .setMinTF(Conf.Preprocessing.CountVectorizer.minTermFrequency)
      .setMinDF(Conf.Preprocessing.CountVectorizer.minDocumentFrequency)
      .setInputCol(userNGram.getOutputCol)
      .setOutputCol(userNGram.getOutputCol + Conf.Preprocessing.Sufix.afterCountVectorizer)

    val textCV = new CountVectorizer()
      .setMinTF(Conf.Preprocessing.CountVectorizer.minTermFrequency)
      .setMinDF(Conf.Preprocessing.CountVectorizer.minDocumentFrequency)
      .setInputCol(textStopWordRemover.getOutputCol)
      .setOutputCol(textStopWordRemover.getOutputCol + Conf.Preprocessing.Sufix.afterCountVectorizer)

    val userCV = new CountVectorizer()
      .setMinTF(Conf.Preprocessing.CountVectorizer.minTermFrequency)
      .setMinDF(Conf.Preprocessing.CountVectorizer.minDocumentFrequency)
      .setInputCol(userStopWordRemover.getOutputCol)
      .setOutputCol(userStopWordRemover.getOutputCol + Conf.Preprocessing.Sufix.afterCountVectorizer)

    val assembler = new VectorAssembler()
      .setInputCols(Array(textNGramCV.getOutputCol, userNGramCV.getOutputCol, textCV.getOutputCol, userCV.getOutputCol))
      .setOutputCol(Conf.Train.featuresColumn)

    //
    val logReg = new LogisticRegression()
      .setFeaturesCol(Conf.Train.featuresColumn)
      .setPredictionCol(Conf.Train.predictionColumn)
      .setProbabilityCol(Conf.Train.probabilityColumn)
      .setLabelCol(Conf.Train.labelColumn)


    val pipeline = new Pipeline().setStages(Array(textCleaner, userCleaner, textToknizer,
      userTokenizer, textStopWordRemover, userStopWordRemover, textNGram, userNGram,
      textCV, userCV, textNGramCV, userNGramCV, assembler, logReg))

    val paramMaps = new ParamGridBuilder()
      .addGrid(textNGram.n, Conf.Preprocessing.NGram.text)
      .addGrid(userNGram.n, Conf.Preprocessing.NGram.userDescription)
      .addGrid(textCV.vocabSize, Conf.Preprocessing.CountVectorizer.text)
      .addGrid(userCV.vocabSize, Conf.Preprocessing.CountVectorizer.userDescription)
      .addGrid(textNGramCV.vocabSize, Conf.Preprocessing.NGram.textVectorLength)
      .addGrid(userNGramCV.vocabSize, Conf.Preprocessing.NGram.userDescriptionVectorLength)
      .addGrid(logReg.regParam, Conf.Train.LogReg.regParams)
      .addGrid(logReg.threshold, Conf.Train.thresholds)
      .addGrid(logReg.elasticNetParam, Conf.Train.LogReg.elasticNet)
      .build()

    val evaluator = new BinaryClassificationEvaluator()

    val cv = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(evaluator)
      .setNumFolds(Conf.Train.numFold)
      .setEstimatorParamMaps(paramMaps)

    val cvModel = cv.fit(trainDF)

    val bestPipelineModel = cvModel.bestModel.asInstanceOf[PipelineModel]

    bestPipelineModel.write.overwrite().save(modelPath)

    //new PrintWriter(Conf.Train.reportDir+"report.txt") { write(PipelineSummary.validate(testDF, bestPipelineModel).toString); close }

  }

}
