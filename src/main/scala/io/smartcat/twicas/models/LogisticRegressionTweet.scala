package io.smartcat.twicas.models
import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.sql.DataFrame

class LogisticRegressionTweet(logisticRegression: LogisticRegressionModel) extends ClassificationModel{
  val name:String = "Logistic Regression"

  override def classify(df: DataFrame): DataFrame = logisticRegression.transform(df)

  def params:Map[String,Double] = Map(
    "threshold" -> logisticRegression.getThreshold,
    "regularization" -> logisticRegression.getRegParam,
    "elasticNet" -> logisticRegression.getElasticNetParam
  )
}

object LogisticRegressionTweet extends Serializable{
  val featureColumn = "features"
  val labelColumn = "label"
  val predictionColumn = "prediction"
  val probabilityColumn = "probability"

  def train(df:DataFrame, threshold:Double = 0.5, regParam:Double = 0.0, elasticNet:Double = 0.0):LogisticRegressionTweet = {
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
