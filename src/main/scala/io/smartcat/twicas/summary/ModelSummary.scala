package io.smartcat.twicas.summary

import io.smartcat.twicas.models.ClassificationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

case class ModelSummary(tp:Int, fp:Int, tn:Int, fn:Int, classificationModel: ClassificationModel) extends Serializable{
  val total:Int = tp + fp + tn + fn

  def precision:Double = tp / (1.0 * (tp + fp))

  def recall:Double = tp / (1.0 * (tp + fn))

  def accuracy:Double = (tp + tn)/(1.0 * total)

  def fMeasure:Double = 2*(precision + recall)/(1.0 * (precision + recall))

  lazy val results:Map[String,Double] =
    Map(
      "precision" -> precision,
      "recall" -> recall,
      "accuracy" -> accuracy,
      "F Measure" -> fMeasure
    )

  lazy val report:String = {

    val filler = "\n-------------------------\n"
    val resultsStr = results map { case (key, value) => "%s : %s" format (key, value) } mkString ("\n","\n", "\n")
    val parametersStr = classificationModel.params map {case (key, value) => "%s : %s" format(key, value)} mkString("\n", "\n", "\n")

    List(filler, "MESUREMENT\n", classificationModel.name, "\n\nPARAMETERS:", parametersStr, "\nRESULTS:",resultsStr, filler).mkString

  }

}

object ModelSummary extends Serializable{
  val label = "label"
  val prediction = "prediction"

  def validation(df:DataFrame, classificationModel: ClassificationModel):ModelSummary = {

    val classified = classificationModel.classify(df)
    val toLong    = udf[Long, Double]( _.toLong)

    val converted = classified.withColumn(prediction, toLong(classified(prediction)))


    val tp = converted.filter((converted(label) === converted(prediction)) && (converted(prediction) === 1)).count.toInt
    val fp = converted.filter(not(converted(label) === converted(prediction)) && (converted(prediction) === 1)).count.toInt
    val tn = converted.filter((converted(label) === converted(prediction)) && (converted(prediction) === 0)).count.toInt
    val fn = converted.filter(not(converted(label) === converted(prediction)) && (converted(prediction) === 0)).count.toInt

    new ModelSummary(tp,fp,tn,fn,classificationModel)
  }

}
