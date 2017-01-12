package io.smartcat.twicas.summary

import io.smartcat.twicas.models.ClassificationModel
import org.apache.spark.sql.DataFrame

case class ModelSummary(tp:Int, fp:Int, tn:Int, fn:Int, classificationModel: ClassificationModel) extends Serializable{
  val total = tp + fp + tn + fn

  def precision:Double = tp / (1.0 * (tp + fp))

  def recall:Double = tp / (1.0 * (tp + fn))

  def accuracy:Double = (tp + tn)/(1.0 * total)

}

object ModelSummary extends Serializable{
  val label = "label"
  val prediction = "prediction"

  def crossValidation(df:DataFrame, classificationModel: ClassificationModel):ModelSummary = {

    val classified = classificationModel.classify(df)

    val tp = classified.filter(classified(label) === classified(prediction) === 1).count.toInt
    val fp = classified.filter(classified(label) =!= (classified(prediction) === 1)).count.toInt
    val tn = classified.filter(classified(label) === classified(prediction) === 0).count.toInt
    val fn = classified.filter(classified(label) =!= (classified(prediction) === 0)).count.toInt

    new ModelSummary(tp,fp,tn,fn,classificationModel)
  }

}
