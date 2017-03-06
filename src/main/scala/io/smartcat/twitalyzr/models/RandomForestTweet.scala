package io.smartcat.twitalyzr.models

import io.smartcat.twitalyzr.summary.{ModelSummary, ParameterOptimization}
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.udf

class RandomForestTweet(randomForestModel: RandomForestClassificationModel) extends ClassificationModel {
  override val name: String = "RANDOM FOREST"
  private val stringFormat = "Logistic Regression\nThreshold : %s\nSubset Strategy : %s\nImpurity : %s\n" +
    "Seed : %s\nSubsampling Rate : %s\nMax Bin : %s\nMax Depth : %s\nNum Trees : %s\n"

  override def classify(df: DataFrame): DataFrame = randomForestModel.transform(df)

  override def toString: String = {
    "\n" + "*" * 10 + "\nMeasurement\n" +
      stringFormat.format(randomForestModel.getThresholds(0).toString,
        randomForestModel.getFeatureSubsetStrategy,
        randomForestModel.getImpurity.toString,
        randomForestModel.getSubsamplingRate.toString,
        randomForestModel.getMaxBins.toString,
        randomForestModel.getNumTrees.toString) + "\n" + "*" * 10
  }
}

object RandomForestTweet extends Serializable {

  val subsetStrategy : String = Conf.Train.RandomForest.subsetStrategy
  val impurity : String = Conf.Train.RandomForest.impurity
  val numClasses : Int = Conf.Train.RandomForest.numClasses
  val seed : Int = Conf.Train.RandomForest.seed
  val subsamplingRate : Double = Conf.Train.RandomForest.subsamplingRate



  val featureColumn : String = Conf.Train.featuresColumn
  val labelColumn : String = Conf.Train.labelColumn
  val predictionColumn : String = Conf.Train.predictionColumn
  val probabilityColumn : String = Conf.Train.probabilityColumn



  /**
    * Train model based on combination of parameters
    *
    * @param trainSet      DataFrame represents train set
    * @param validationSet DataFrame represents validation set
    * @param thresholds    list of train parameters for prediction
    * @param maxBins       List of train parameters, max number of category per feature
    * @param maxDepths     List of train parameters, max depth of tree
    * @param numTrees      List of train parameters, number of trees that will be trained
    * @return ParameterOptimization containing trained models and their summaries on validation set
    */
  def makeModels(trainSet: DataFrame, validationSet: DataFrame,
                 thresholds: List[Double], maxBins: List[Int], maxDepths: List[Int], numTrees: List[Int]): ParameterOptimization = {

    def toDouble(l: List[Int]): List[Double] = l.map(_.toDouble)

    val parameters = generator(List(thresholds, toDouble(maxBins), toDouble(maxDepths), toDouble(numTrees)))

    val models = parameters.map { case (threshold :: maxBin :: maxDepth :: numTree :: _) =>
      train(trainSet, threshold, maxBin.toInt, maxDepth.toInt, numTree.toInt)
    }

    ParameterOptimization(models map (ModelSummary.validation(validationSet, _)))

  }

  /**
    * Train RandomForestClassification from Spark
    *
    * @param df        DataFrame represents train set
    * @param threshold train parameter for prediction
    * @param maxBin    train parameter, max number of category per feature
    * @param maxDepth  train parameter, max depth of tree
    * @param numTrees  train parameter, number of trees that will be trained
    * @return RandomForestTweet containing Spark's RandomForestClassificationModel
    */
  def train(df: DataFrame, threshold: Double, maxBin: Int, maxDepth: Int, numTrees: Int): RandomForestTweet = {

    val toDouble = udf[Double, Long](_.toDouble)

    val castedLabels = df.withColumn(labelColumn, toDouble(df(labelColumn)))

    val model = new RandomForestClassifier()
      .setPredictionCol(predictionColumn)
      .setLabelCol(labelColumn)
      .setProbabilityCol(probabilityColumn)
      .setFeaturesCol(featureColumn)
      .setCacheNodeIds(true)
      .setFeatureSubsetStrategy(subsetStrategy)
      .setImpurity(impurity)
      .setMaxBins(maxBin)
      .setMaxDepth(maxDepth)
      .setNumTrees(numTrees)
      .setSeed(seed)
      .setSubsamplingRate(subsamplingRate)
      .setThresholds(Array(threshold, 1 - threshold))
      .fit(castedLabels)

    new RandomForestTweet(model)
  }

}
