package io.smartcat.twicas.pipeline

import org.apache.spark.sql.DataFrame


class PipelineProcessor(pipelines: List[Pipeline]) extends Serializable {

  def processAll(df: DataFrame): DataFrame = pipelines.foldLeft(df)((dff, pipeline) => pipeline.process(dff))

}

object PipelineProcessor extends Serializable{

  def loadFromFile(filepath:String):PipelineProcessor = ???

  def saveToFile(filepath:String, pipelineProcessor: PipelineProcessor):Unit = ???

}

