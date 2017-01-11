package io.smartcat.twicas.preprocessing.pipeline

import org.apache.spark.sql.DataFrame

trait Pipeline extends Serializable {
  def process(df: DataFrame): DataFrame
}


class PipelineProcessor extends Serializable {

  def processAll(df: DataFrame, pipelines: List[Pipeline]): DataFrame = pipelines match {
    case Nil => df
    case head :: tail => processAll(head.process(df), tail)
  }

}

