package io.smartcat.twicas.pipeline

import org.apache.spark.sql.DataFrame

trait Pipeline extends Serializable {
  def process(df: DataFrame): DataFrame
}


class PipelineProcessor(pipelines: List[Pipeline]) extends Serializable {

  def processAll(df: DataFrame): DataFrame = pipelines.foldLeft(df)((dff,pipeline) => pipeline.process(dff))

}

