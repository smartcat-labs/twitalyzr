package io.smartcat.twitalyzr.models

import io.smartcat.twitalyzr.pipeline.Pipeline
import org.apache.spark.sql.DataFrame

abstract class ClassificationModel extends Pipeline {
  val name: String

  override def process(df: DataFrame): DataFrame = classify(df)

  def classify(df: DataFrame): DataFrame

}
