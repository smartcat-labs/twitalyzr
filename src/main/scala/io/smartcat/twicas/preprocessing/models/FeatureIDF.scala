package io.smartcat.twicas.preprocessing.models

import io.smartcat.twicas.preprocessing.pipeline.Pipeline
import org.apache.spark.ml.feature.{IDF, IDFModel}
import org.apache.spark.sql.DataFrame

class FeatureIDF(idfModels: List[IDFModel]) extends Pipeline {
  override def process(df: DataFrame): DataFrame = idfModels.foldLeft(df)((dff, model) => model.transform(dff))
}

object FeatureIDF extends Serializable {
  val afterIDF = "_idf"


  def make(df: DataFrame, columnNames: List[String]): FeatureIDF = {
    new FeatureIDF(columnNames.map {
      column =>
        new IDF()
          .setInputCol(column)
          .setOutputCol(column + afterIDF)
          .fit(df)
    })
  }

}
