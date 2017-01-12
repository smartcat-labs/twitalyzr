package io.smartcat.twicas.models

import io.smartcat.twicas.pipeline.Pipeline
import org.apache.spark.sql.DataFrame

abstract class ClassificationModel extends Pipeline{

  override def process(df: DataFrame): DataFrame = classify(df)

  def classify(df:DataFrame):DataFrame

  def params:Map[String,Double]

}
