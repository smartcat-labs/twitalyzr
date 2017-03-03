package io.smartcat.twitalyzr.sparkml_pipeline

import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{not, udf}

case class PipelineSummary(tp: Int, fp: Int, tn: Int, fn: Int, pipelineModel: PipelineModel) {

  private lazy val format = {
    "RESULTS:\n" +
      "True positive : %d%n" +
      "False positive: %d%n" +
      "True negative : %d%n" +
      "False negative: %d%n" +
      "Accuracy : %.3f%n" +
      "Precision : %.3f%n" +
      "Recall : %.3f%n" +
      "FMeasure : %.3f%n" +
      "PARAMETERS:\n" +
      pipelineModel.stages.map(_.explainParams()).mkString("\n")
  }
  val total: Int = tp + fp + tn + fn

  //negative predictive value
  def negativePredictiveValue = tn / (1.0 * (tn + fn))

  //false positive rate
  def falsePositiveRate = 1 - trueNegativeRate

  //true negative rate
  def trueNegativeRate = tn / (1.0 * (fp + tn))

  override def toString: String = {
    format.format(tp, fp, tn, fn, accuracy, precision, recall, fMeasure)
  }

  def accuracy: Double = (tp + tn) / (1.0 * total)

  def fMeasure: Double = 2 * tp / (2.0 * tp + fp + fn)

  //positive predictive value
  def precision: Double = tp / (1.0 * (tp + fp))

  //true positive rate
  def recall: Double = tp / (1.0 * (tp + fn))

}

object PipelineSummary {

  private val prediction = Conf.Train.predictionColumn
  private val label = Conf.Train.labelColumn

  def validate(df: DataFrame, pipelineModel: PipelineModel): PipelineSummary = {

    val classified = pipelineModel.transform(df)
    val toLong = udf[Long, Double](_.toLong)

    val converted = classified.withColumn(Conf.Train.predictionColumn, toLong(classified(prediction)))

    val tp = converted.filter((converted(label) === converted(prediction)) && (converted(prediction) === 1))
    val fp = converted.filter(not(converted(label) === converted(prediction)) && (converted(prediction) === 1))

    val tn = converted.filter((converted(label) === converted(prediction)) && (converted(prediction) === 0))
    val fn = converted.filter(not(converted(label) === converted(prediction)) && (converted(prediction) === 0))

    PipelineSummary(tp.count.toInt, fp.count.toInt, tn.count.toInt, fn.count.toInt, pipelineModel)
  }

}
