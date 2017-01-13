package io.smartcat.twicas.pipeline

import org.apache.spark.sql.DataFrame

trait Pipeline extends Serializable {
  def process(df: DataFrame): DataFrame
}


