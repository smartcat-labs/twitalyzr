package io.smartcat.twicas.models

import io.smartcat.twicas.summary.{ModelSummary, ParameterOptimization}
import io.smartcat.twicas.util.Conf
import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.sql.DataFrame

class LogisticRegressionTweet(logisticRegression: LogisticRegressionModel) extends ClassificationModel {
  val name: String = "Logistic Regression"
 private val stringFormat = "Logistic Regression\nThreshold : %s\nRegularization : %s\nElastic Net : %s\n"

  override def classify(df: DataFrame): DataFrame = logisticRegression.transform(df)

  override def toString: String = {
    "\n" + "*" * 10 + "\nMeasurement\n" +
      stringFormat.format(logisticRegression.getThreshold.toString,
        logisticRegression.getRegParam.toString,
        logisticRegression.getElasticNetParam.toString) + "\n" + "*" * 10
  }
}

object LogisticRegressionTweet extends Serializable {
  val featureColumn : String = Conf.Train.featuresColumn
  val labelColumn : String = Conf.Train.labelColumn
  val predictionColumn : String = Conf.Train.predictionColumn
  val probabilityColumn : String = Conf.Train.probabilityColumn

  /**
    * This function makes models for every combination of parameters and then validates them on given validation set.
    * For example:
    * thresholds = [0.5, 0.6], regParams = [0.0, 0.1], elasticNet = [0.0]
    * It will make 4 models with parameters like (0.5, 0.0, 0.0), (0.5, 0.1, 0.0), (0.6, 0.0, 0.0), (0.6, 0.1, 0.0)
    *
    * @param trainSet      DataFrame - train data set
    * @param validationSet DataFrame - validation data set
    * @param thresholds    - list of possible thresholds
    * @param regParams     - list of possible regularization parameters
    * @param elasticNet    - list of possible elastic net parameters
    * @return ParameterOptimization - List of ModelSummary for each combination of parameters
    */
  def makeModels(trainSet: DataFrame, validationSet: DataFrame,
                 thresholds: List[Double], regParams: List[Double], elasticNet: List[Double]): ParameterOptimization = {

    val parameters = generator(List(thresholds, regParams, elasticNet))

    val models = parameters.map { case (threshold :: regParam :: elastic :: _) =>
      train(trainSet, threshold, regParam, elastic)
    }

    ParameterOptimization(models map (ModelSummary.validation(validationSet, _)))

  }

  /**
    * This function train model for given train data set and parameters
    *
    * @param df         DataFrame - train data set
    * @param threshold  - classifier probability threshold, prob above that value will be classified as 1
    * @param regParam   - regularization parameter. Default 0.0 -> No regularization
    * @param elasticNet - regularization parameter Default 0.0 -> No regularization
    * @return LogisticRegressionTweet (ClassificationModel) trained model on data set df, and given parameters
    */
  def train(df: DataFrame, threshold: Double = 0.5, regParam: Double = 0.0, elasticNet: Double = 0.0): LogisticRegressionTweet = {
    val model = new LogisticRegression()
      .setFeaturesCol(featureColumn)
      .setLabelCol(labelColumn)
      .setPredictionCol(predictionColumn)
      .setProbabilityCol(probabilityColumn)
      .setThreshold(threshold)
      .setRegParam(regParam)
      .setElasticNetParam(elasticNet)
      .fit(df)
    new LogisticRegressionTweet(model)
  }

}
