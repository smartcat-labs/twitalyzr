package io.smartcat.twicas.models

import io.smartcat.twicas.summary.{ModelSummary, ParameterOptimization}
import io.smartcat.twicas.util.Conf
import org.apache.spark.ml.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.sql.DataFrame

class NaiveBayesTweet(naiveBayesModel: NaiveBayesModel) extends ClassificationModel {
  override val name: String = "Naive Bayes"
  private val stringFormat = "Logistic Regression\nThreshold : %s\nSmoothing : %s\nModel Type : %s\n"

  override def classify(df: DataFrame): DataFrame = naiveBayesModel.transform(df)

  override def toString: String = {
    "\n" + "*" * 10 + "\nMeasurement\n" +
      stringFormat.format(naiveBayesModel.getThresholds(0).toString,
        naiveBayesModel.getSmoothing.toString,
        naiveBayesModel.getModelType) + "\n" + "*" * 10
  }
}

object NaiveBayesTweet extends Serializable {
  val featureColumn : String = Conf.Train.featuresColumn
  val labelColumn : String = Conf.Train.labelColumn
  val predictionColumn : String = Conf.Train.predictionColumn
  val probabilityColumn : String = Conf.Train.probabilityColumn

  /**
    * Runs training on different model made from combinations of parameters
    *
    * @param trainSet      DataFrame of train set
    * @param validationSet DataFrame of validation set
    * @param smoothings    List of smoothing parameters
    * @param thresholds    List of threshold parameters
    * @return ParameterOptimization which contains trained models and their summary on validation set
    */
  def makeModels(trainSet: DataFrame, validationSet: DataFrame,
                 smoothings: List[Double], thresholds: List[Double]): ParameterOptimization = {

    val parameters = generator(List(smoothings, thresholds))

    val models = parameters.map { case (smoothing :: threshold :: _) =>
      train(trainSet, threshold, smoothing)
    }

    ParameterOptimization(models map (ModelSummary.validation(validationSet, _)))

  }

  /**
    * This method calls Spark's NaiveBayes training algorithm
    *
    * @param df        Train data set
    * @param smoothing parameter for training
    * @param threshold parameter for training
    * @return NaiveBayesTweet trained model
    */
  def train(df: DataFrame, smoothing: Double, threshold: Double): NaiveBayesTweet = {

    val model = new NaiveBayes()
      .setFeaturesCol(featureColumn)
      .setLabelCol(labelColumn)
      .setPredictionCol(predictionColumn)
      .setProbabilityCol(probabilityColumn)
      .setSmoothing(smoothing)
      .setThresholds(Array(threshold))
      .fit(df)

    new NaiveBayesTweet(model)

  }
}
