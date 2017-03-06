package io.smartcat.twitalyzr.pipeline

import org.apache.spark.sql.DataFrame

trait Pipeline extends Serializable {
  def process(df: DataFrame): DataFrame
}


