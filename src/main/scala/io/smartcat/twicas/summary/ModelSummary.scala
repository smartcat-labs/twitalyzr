package io.smartcat.twicas.summary

import io.smartcat.twicas.models.ClassificationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

case class ModelSummary(tp:Int, fp:Int, tn:Int, fn:Int, classificationModel: ClassificationModel) extends Serializable{
  val total:Int = tp + fp + tn + fn

  val precision:Double = tp / (1.0 * (tp + fp))

  val recall:Double = tp / (1.0 * (tp + fn))

  val accuracy:Double = (tp + tn)/(1.0 * total)

  val fMeasure:Double = 2*(precision + recall)/(1.0 * (precision + recall))

}

object ModelSummary extends Serializable{
  val label = "label"
  val prediction = "prediction"

  def crossValidation(df:DataFrame, classificationModel: ClassificationModel):ModelSummary = {

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
