package io.smartcat.twitalyzr.pipeline

import java.io._

import org.apache.spark.sql.DataFrame


case class PipelineProcessor(pipelines: List[Pipeline]) extends Serializable {

  def processAll(df: DataFrame): DataFrame = pipelines.foldLeft(df)((dff, pipeline) => pipeline.process(dff))

}

object PipelineProcessor extends Serializable {

  def loadFromFile(filepath: String): PipelineProcessor = {
    val loader = Thread.currentThread().getContextClassLoader
    val objIn = new ObjectInputStream(new FileInputStream(filepath)) {
      override def resolveClass(desc: ObjectStreamClass): Class[_] =
        Class.forName(desc.getName, false, loader)
    }
    objIn.readObject.asInstanceOf[PipelineProcessor]
  }

  def saveToFile(filepath: String, pipelineProcessor: PipelineProcessor): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(filepath))
    oos.writeObject(pipelineProcessor)
    oos.close()

  }
}

